package com.tianli.currency_token.transfer;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tianli.address.AddressService;
import com.tianli.address.mapper.Address;
import com.tianli.captcha.email.EmailSendFactory;
import com.tianli.captcha.email.enums.EmailSendEnum;
import com.tianli.charge.ChargeType;
import com.tianli.charge.mapper.ChargeStatus;
import com.tianli.common.CommonFunction;
import com.tianli.common.blockchain.TronTriggerContract;
import com.tianli.common.blockchain.UsdtBscContract;
import com.tianli.common.blockchain.UsdtEthContract;
import com.tianli.currency.CurrencyTypeEnum;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.currency_token.CurrencyTokenService;
import com.tianli.currency_token.mapper.ChainType;
import com.tianli.currency_token.mapper.CurrencyCoinEnum;
import com.tianli.currency_token.transfer.dto.TransferDTO;
import com.tianli.currency_token.transfer.mapper.TokenContract;
import com.tianli.currency_token.transfer.service.TokenContractService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.loan.entity.LoanAddress;
import com.tianli.loan.service.ILoanAddressService;
import com.tianli.loan.service.ILoanService;
import com.tianli.management.ruleconfig.ConfigConstants;
import com.tianli.management.spot.entity.SGCharge;
import com.tianli.management.spot.service.SGChargeService;
import com.tianli.mconfig.ConfigService;
import com.tianli.user.UserService;
import com.tianli.user.mapper.User;
import com.tianli.user.userinfo.UserInfoService;
import com.tianli.user.userinfo.mapper.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TokenTransferService {

    @Transactional(rollbackFor = Exception.class)
    public void syncBscTransfer(Long block) {
        Map<CurrencyCoinEnum, TokenContract> tokenContractMap = tokenContractService.findByChainType(ChainType.bep20).stream().collect(Collectors.toMap(TokenContract::getToken, Function.identity()));
        List<String> contracts = tokenContractMap.values().stream().map(TokenContract::getContract_address).collect(Collectors.toList());
        List<TransferDTO> result = transferRequestService.getTransferListByBlock(ChainType.bep20, block, contracts);
        TokenContract usdtContract = tokenContractMap.get(CurrencyCoinEnum.usdt);
        for (TransferDTO item : result) {
            if (StrUtil.equalsIgnoreCase(item.getContractAddress(), usdtContract.getContract_address())) {
                loanQuery(item, ChainType.bep20, usdtContract);
            }
            String toAddress = item.getTo();
            Address address = addressService.getByBsc(toAddress);
            if (address == null) continue;
            User user = userService._get(address.getUid());
            UserInfo userInfo = userInfoService.getById(address.getUid());
            if (user == null || userInfo == null) continue;
            TokenContract tokenContract = tokenContractService.getOne(new LambdaQueryWrapper<TokenContract>()
                    .eq(TokenContract::getChain, ChainType.bep20)
                    .eq(TokenContract::getContract_address, StringUtils.isBlank(item.getContractAddress()) ? "0x000000" : item.getContractAddress())
            );
            if (tokenContract == null) continue;
            BigDecimal amount = new BigDecimal(item.getValue()).movePointLeft(tokenContract.getDecimals());
            SGCharge sgCharge = SGCharge.builder()
                    .id(CommonFunction.generalId())
                    .create_time(LocalDateTime.now())
                    .complete_time(LocalDateTime.now())
                    .status(ChargeStatus.chain_success)
                    .uid(user.getId())
                    .uid_username(user.getUsername())
                    .uid_nick(userInfo.getNick())
                    .sn(item.getHash())
                    .currency_type(ChainType.bep20.name())
                    .charge_type(ChargeType.recharge)
                    .amount(amount)
                    .fee(BigDecimal.ZERO)
                    .real_amount(new BigDecimal(item.getValue()).movePointLeft(tokenContract.getDecimals()))
                    .from_address(item.getFrom())
                    .to_address(item.getTo())
                    .txid(item.getHash())
                    .token(tokenContract.getToken().name())
                    .build();
            sgChargeService.save(sgCharge);
            currencyTokenService.increase(user.getId(), CurrencyTypeEnum.actual, tokenContract.getToken(), amount, sgCharge.getId().toString(), CurrencyLogDes.充值);
        }
        boolean cas = configService.cas(ConfigConstants.SYNC_TRANSFER_BSC_BLOCK, String.valueOf(block - 1), String.valueOf(block));
        if (!cas) ErrorCodeEnum.SYSTEM_ERROR.throwException();
    }

    private void loanQuery(TransferDTO item, ChainType chainType, TokenContract usdtContract) {
        String to = item.getTo();
        LoanAddress loanAddress = loanAddressService.findByAddress(to, chainType);
        if (ObjectUtil.isNull(loanAddress)) {
            return;
        }
        //还款
        loanService.repayment(loanAddress, item, usdtContract);
    }

    @Transactional(rollbackFor = Exception.class)
    public void syncTrcTransfer(Long block) {
        Map<CurrencyCoinEnum, TokenContract> tokenContractMap = tokenContractService.findByChainType(ChainType.trc20).stream().collect(Collectors.toMap(TokenContract::getToken, Function.identity()));
        List<String> contracts = tokenContractMap.values().stream().map(TokenContract::getContract_address).collect(Collectors.toList());
        List<TransferDTO> result = transferRequestService.getTransferListByBlock(ChainType.trc20, block, contracts);
        TokenContract usdtContract = tokenContractMap.get(CurrencyCoinEnum.usdt);
        for (TransferDTO item : result) {
            if (StrUtil.equalsIgnoreCase(item.getContractAddress(), usdtContract.getContract_address())) {
                loanQuery(item, ChainType.trc20, usdtContract);
            }
            String toAddress = item.getTo();
            Address address = addressService.getByTron(toAddress);
            if (address == null) continue;
            User user = userService._get(address.getUid());
            UserInfo userInfo = userInfoService.getById(address.getUid());
            if (user == null || userInfo == null) continue;
            TokenContract tokenContract = tokenContractService.getOne(new LambdaQueryWrapper<TokenContract>()
                    .eq(TokenContract::getChain, ChainType.trc20)
                    .eq(TokenContract::getContract_address, StringUtils.isBlank(item.getContractAddress()) ? "0x000000" : item.getContractAddress())
            );
            if (tokenContract == null) continue;
            BigDecimal amount = new BigDecimal(item.getValue()).movePointLeft(tokenContract.getDecimals());
            SGCharge sgCharge = SGCharge.builder()
                    .id(CommonFunction.generalId())
                    .create_time(LocalDateTime.now())
                    .complete_time(LocalDateTime.now())
                    .status(ChargeStatus.chain_success)
                    .uid(user.getId())
                    .uid_username(user.getUsername())
                    .uid_nick(userInfo.getNick())
                    .sn(item.getHash())
                    .currency_type(ChainType.trc20.name())
                    .charge_type(ChargeType.recharge)
                    .amount(amount)
                    .fee(BigDecimal.ZERO)
                    .real_amount(new BigDecimal(item.getValue()).movePointLeft(tokenContract.getDecimals()))
                    .from_address(item.getFrom())
                    .to_address(item.getTo())
                    .txid(item.getHash())
                    .token(tokenContract.getToken().name())
                    .build();
            sgChargeService.save(sgCharge);
            currencyTokenService.increase(user.getId(), CurrencyTypeEnum.actual, tokenContract.getToken(), amount, sgCharge.getId().toString(), CurrencyLogDes.充值);
//            boolean cas = configService.cas(ConfigConstants.SYNC_TRANSFER_TRC_BLOCK, String.valueOf(block - 1), String.valueOf(block));
//            if (!cas) ErrorCodeEnum.SYSTEM_ERROR.throwException();
        }
        boolean cas = configService.cas(ConfigConstants.SYNC_TRANSFER_TRC_BLOCK, String.valueOf(block - 1), String.valueOf(block));
        if (!cas) ErrorCodeEnum.SYSTEM_ERROR.throwException();
    }

    @Transactional(rollbackFor = Exception.class)
    public void syncErcTransfer(Long block) {
        Map<CurrencyCoinEnum, TokenContract> tokenContractMap = tokenContractService.findByChainType(ChainType.erc20).stream().collect(Collectors.toMap(TokenContract::getToken, Function.identity()));
        List<String> contracts = tokenContractMap.values().stream().map(TokenContract::getContract_address).collect(Collectors.toList());
        List<TransferDTO> result = transferRequestService.getTransferListByBlock(ChainType.erc20, block, contracts);
        TokenContract usdtContract = tokenContractMap.get(CurrencyCoinEnum.usdt);
        for (TransferDTO item : result) {
            if (StrUtil.equalsIgnoreCase(item.getContractAddress(), usdtContract.getContract_address())) {
                loanQuery(item, ChainType.erc20, usdtContract);
            }
            String toAddress = item.getTo();
            Address address = addressService.getByEth(toAddress);
            if (address == null) continue;
            User user = userService._get(address.getUid());
            UserInfo userInfo = userInfoService.getById(address.getUid());
            if (user == null || userInfo == null) continue;
            TokenContract tokenContract = tokenContractService.getOne(new LambdaQueryWrapper<TokenContract>()
                    .eq(TokenContract::getChain, ChainType.erc20)
                    .eq(TokenContract::getContract_address, StringUtils.isBlank(item.getContractAddress()) ? "0x000000" : item.getContractAddress())
            );
            if (tokenContract == null) continue;
            BigDecimal amount = new BigDecimal(item.getValue()).movePointLeft(tokenContract.getDecimals());
            SGCharge sgCharge = SGCharge.builder()
                    .id(CommonFunction.generalId())
                    .create_time(LocalDateTime.now())
                    .complete_time(LocalDateTime.now())
                    .status(ChargeStatus.chain_success)
                    .uid(user.getId())
                    .uid_username(user.getUsername())
                    .uid_nick(userInfo.getNick())
                    .sn(item.getHash())
                    .currency_type(ChainType.erc20.name())
                    .charge_type(ChargeType.recharge)
                    .amount(amount)
                    .fee(BigDecimal.ZERO)
                    .real_amount(new BigDecimal(item.getValue()).movePointLeft(tokenContract.getDecimals()))
                    .from_address(item.getFrom())
                    .to_address(item.getTo())
                    .txid(item.getHash())
                    .token(tokenContract.getToken().name())
                    .build();
            sgChargeService.save(sgCharge);
            currencyTokenService.increase(user.getId(), CurrencyTypeEnum.actual, tokenContract.getToken(), amount, sgCharge.getId().toString(), CurrencyLogDes.充值);
        }
        boolean cas = configService.cas(ConfigConstants.SYNC_TRANSFER_ERC_BLOCK, String.valueOf(block - 1), String.valueOf(block));
        if (!cas) ErrorCodeEnum.SYSTEM_ERROR.throwException();
    }

    // 上链
//    @Transactional(rollbackFor = Exception.class)
    public void uploadChain(SGCharge sgCharge) {
        CurrencyCoinEnum token = CurrencyCoinEnum.getCurrencyCoinEnum(sgCharge.getToken());
        ChainType chain = ChainType.valueOf(sgCharge.getCurrency_type());
        TokenContract tokenContract = tokenContractService.getOne(new LambdaQueryWrapper<TokenContract>()
                .eq(TokenContract::getChain, chain)
                .eq(TokenContract::getToken, token)
        );
        String txid = null;
        Result result = null;
        switch (chain) {
            case bep20:
                if (CurrencyCoinEnum.bnb.equals(token)) {
                    result = usdtBscContract.transferBNB(sgCharge.getTo_address(), sgCharge.getReal_amount().movePointRight(tokenContract.getDecimals()).toBigInteger());
                } else {
                    result = usdtBscContract.transferToken(sgCharge.getTo_address(), sgCharge.getReal_amount().movePointRight(tokenContract.getDecimals()).toBigInteger(), token);
                }
                if (Objects.isNull(result) || !Objects.equals(result.getCode(), "0")) {
                    ErrorCodeEnum.throwException("上链失败, 请稍后重试");
                }
                txid = result.getData().toString();
                break;
            case trc20:
                if (CurrencyCoinEnum.trx.equals(token)) {
                    txid = tronTriggerContract.transferTrx(sgCharge.getTo_address(), sgCharge.getReal_amount().movePointRight(tokenContract.getDecimals()).toBigInteger().longValue());
                } else {
                    txid = tronTriggerContract.transferToken(sgCharge.getTo_address(), sgCharge.getReal_amount().movePointRight(tokenContract.getDecimals()).toBigInteger(), token);
                }
                break;
            case erc20:
                if (CurrencyCoinEnum.eth.equals(token)) {
                    result = usdtEthContract.transferEth(sgCharge.getTo_address(), sgCharge.getReal_amount().movePointRight(tokenContract.getDecimals()).toBigInteger());
                } else {
                    result = usdtEthContract.transferToken(sgCharge.getTo_address(), sgCharge.getReal_amount().movePointRight(tokenContract.getDecimals()).toBigInteger(), token);
                }
                if (Objects.isNull(result) || !Objects.equals(result.getCode(), "0")) {
                    ErrorCodeEnum.throwException("上链失败, 请稍后重试");
                }
                txid = result.getData().toString();
                break;
        }
        boolean update = false;
        while (!update) {
            update = sgChargeService.update(Wrappers.<SGCharge>lambdaUpdate()
                    .set(SGCharge::getTxid, txid)
                    .eq(SGCharge::getId, sgCharge.getId())
            );
        }
        User user = userService._get(sgCharge.getUid());
        if (ObjectUtil.isNotNull(user)) {
            try {
                emailSendFactory.send(EmailSendEnum.WITHDRAWAL_SUCCESS, null, sgCharge, user.getUsername());
            } catch (Exception e) {
                log.error("提现成功邮件发送失败:", e);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void syncRechargeWithdraw() {
        List<SGCharge> list = sgChargeService.list(new LambdaQueryWrapper<SGCharge>().eq(SGCharge::getStatus, ChargeStatus.chaining));
        for (SGCharge sgCharge : list) {
            boolean update_result = false;
            TransferDTO result = transferRequestService.getTransferByTx(ChainType.valueOf(sgCharge.getCurrency_type()), sgCharge.getTxid());
            if (result == null) continue;
            System.out.println(result.getStatus());
            if (StringUtils.isBlank(result.getStatus()) || result.getStatus().equals("1")) {
                System.out.println("成功");
                sgCharge.setStatus(ChargeStatus.chain_success);
                update_result = sgChargeService.update(sgCharge, new LambdaUpdateWrapper<SGCharge>()
                        .eq(SGCharge::getId, sgCharge.getId()).eq(SGCharge::getStatus, ChargeStatus.chaining));
                if (update_result) {
                    currencyTokenService.reduce(sgCharge.getUid(), CurrencyTypeEnum.actual, CurrencyCoinEnum.getCurrencyCoinEnum(sgCharge.getToken()), sgCharge.getReal_amount(), sgCharge.getSn(), CurrencyLogDes.提现);
                    currencyTokenService.reduce(sgCharge.getUid(), CurrencyTypeEnum.actual, CurrencyCoinEnum.getCurrencyCoinEnum(sgCharge.getToken()), sgCharge.getFee(), sgCharge.getSn(), CurrencyLogDes.提现手续费);
                }
            }
            if (!StringUtils.isBlank(result.getStatus()) && !result.getStatus().equals("1")) {
                sgCharge.setStatus(ChargeStatus.chain_fail);
                update_result = sgChargeService.update(sgCharge, new LambdaUpdateWrapper<SGCharge>()
                        .eq(SGCharge::getId, sgCharge.getId()).eq(SGCharge::getStatus, ChargeStatus.chaining));
                if (update_result) {
                    currencyTokenService.unfreeze(sgCharge.getUid(), CurrencyTypeEnum.actual, CurrencyCoinEnum.getCurrencyCoinEnum(sgCharge.getToken()), sgCharge.getAmount(), sgCharge.getSn(), CurrencyLogDes.提现);
                }
            }
        }
    }


    @Resource
    private EmailSendFactory emailSendFactory;
    @Resource
    private TokenContractService tokenContractService;
    @Resource
    private TransferRequestService transferRequestService;
    @Resource
    private AddressService addressService;
    @Resource
    private UserService userService;
    @Resource
    private UserInfoService userInfoService;
    @Resource
    private SGChargeService sgChargeService;
    @Resource
    private CurrencyTokenService currencyTokenService;
    @Resource
    private ConfigService configService;
    @Resource
    private UsdtBscContract usdtBscContract;
    @Resource
    private UsdtEthContract usdtEthContract;
    @Resource
    private TronTriggerContract tronTriggerContract;

    @Resource
    ILoanService loanService;

    @Resource
    ILoanAddressService loanAddressService;
}
