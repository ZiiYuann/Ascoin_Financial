package com.tianli.currency_token;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tianli.charge.ChargeType;
import com.tianli.charge.mapper.ChargeStatus;
import com.tianli.common.CommonFunction;
import com.tianli.common.init.RequestInitService;
import com.tianli.currency.CurrencyTypeEnum;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.currency_token.dto.WithdrawDTO;
import com.tianli.currency_token.mapper.ChainType;
import com.tianli.currency_token.mapper.CurrencyCoinEnum;
import com.tianli.currency_token.transfer.mapper.TokenContract;
import com.tianli.currency_token.transfer.service.TokenContractService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.kyc.KycService;
import com.tianli.kyc.mapper.Kyc;
import com.tianli.management.ruleconfig.ConfigConstants;
import com.tianli.management.spot.entity.SGCharge;
import com.tianli.management.spot.service.SGChargeService;
import com.tianli.mconfig.ConfigService;
import com.tianli.role.annotation.GrcCheckModular;
import com.tianli.user.logs.UserIpLogService;
import com.tianli.user.userinfo.UserInfoService;
import com.tianli.user.userinfo.mapper.UserInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class WithdrawService {

    @Transactional
    public void withdraw(WithdrawDTO withdrawDTO) {
        Long uid = requestInitService.uid();
        String from_address = null;
        CurrencyCoinEnum token = withdrawDTO.getToken();
        ChainType chain = withdrawDTO.getChain();
        BigDecimal amount = withdrawDTO.getAmount();
        //根据提现币种 选择对应的链的主钱包地址
        switch (chain) {
            case trc20:
                from_address = configService.get(ConfigConstants.TRON_MAIN_WALLET_ADDRESS);
                break;
            case erc20:
                from_address = configService.get(ConfigConstants.ETH_MAIN_WALLET_ADDRESS);
                break;
            case bep20:
                from_address = configService.get(ConfigConstants.BSC_MAIN_WALLET_ADDRESS);
                break;
        }
        if (from_address == null) ErrorCodeEnum.SYSTEM_ERROR.throwException();
        long id = CommonFunction.generalId();

        TokenContract tokenContract = tokenContractService.getOne(new LambdaQueryWrapper<TokenContract>()
                .eq(TokenContract::getToken, withdrawDTO.getToken())
                .eq(TokenContract::getChain, withdrawDTO.getChain())
        );

        if(withdrawDTO.getAmount().compareTo(tokenContract.getWithdraw_min_amount()) < 0)
            ErrorCodeEnum.WITHDRAWAL_AMOUNT_LT_MIN_AMOUNT_ERROR.throwException();

        //计算手续费  实际手续费 = 提现数额*手续费率 + 固定手续费数额
//        String rate = configService.get(ConfigConstants.ACTUAL_WITHDRAW_RATE);
//        String fixedAmount = configService.get(ConfigConstants.ACTUAL_WITHDRAW_FIXED_AMOUNT);
        BigDecimal fee = amount.multiply(tokenContract.getWithdraw_rate())
                .add(tokenContract.getWithdraw_fixed_amount());

        //实际到账数额 = 提现数额 - 手续费
        BigDecimal real_amount = amount.subtract(fee);
        if (fee.compareTo(BigDecimal.ZERO) < 0)
            ErrorCodeEnum.FEE_LT_ZERO_ERROR.throwException();
        if (real_amount.compareTo(BigDecimal.ZERO) < 0)
            ErrorCodeEnum.WITHDRAWAL_AMOUNT_LT_FEE_ERROR.throwException();
        UserInfo userInfo = userInfoService.getOrSaveById(uid);
        Kyc one = kycService.getOne(Wrappers.lambdaQuery(Kyc.class).eq(Kyc::getUid, uid));

        if(Objects.nonNull(one)){
            Integer status = one.getStatus();
            if (status == -1){
                ErrorCodeEnum.KYC_TRIGGER_ERROR.throwException();
            }else if (status == 0){
                ErrorCodeEnum.throwException("KYC认证中,请稍后再试");
            }
        } else {
            ErrorCodeEnum.KYC_TRIGGER_ERROR.throwException();
        }
        //创建提现订单
        SGCharge sgCharge = SGCharge.builder()
                .id(id).create_time(LocalDateTime.now())
                .status(ChargeStatus.created)
                .uid(uid)
                .uid_avatar(userInfo.getAvatar())
                .uid_nick(userInfo.getNick())
                .uid_username(userInfo.getUsername())
                .sn("C" + CommonFunction.generalSn(id))
                .currency_type(chain.name())
                .charge_type(ChargeType.withdraw)
                .amount(amount)
                .fee(fee)
                .real_amount(real_amount)
                .from_address(from_address).to_address(withdrawDTO.getAddress())
                .token(token.name())
                .build();
        sgChargeService.save(sgCharge);
        userIpLogService.updateBehaviorId(GrcCheckModular.提现, sgCharge.getId());
        //冻结提现数额
        currencyTokenService.freeze(uid, CurrencyTypeEnum.actual, token, amount, sgCharge.getSn(), CurrencyLogDes.提现);
    }

    @Resource
    private RequestInitService requestInitService;
    @Resource
    private ConfigService configService;
    @Resource
    private UserInfoService userInfoService;
    @Resource
    private KycService kycService;
    @Resource
    private SGChargeService sgChargeService;
    @Resource
    private UserIpLogService userIpLogService;
    @Resource
    private CurrencyTokenService currencyTokenService;
    @Resource
    private TokenContractService tokenContractService;
}
