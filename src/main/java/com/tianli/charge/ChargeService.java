package com.tianli.charge;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.tianli.address.AddressService;
import com.tianli.address.controller.AddressWebhooksDTO;
import com.tianli.address.mapper.Address;
import com.tianli.agent.AgentService;
import com.tianli.agent.mapper.Agent;
import com.tianli.chain.service.LineTransactionDetail;
import com.tianli.charge.controller.ChargeWebhooksDTO;
import com.tianli.charge.controller.WithdrawDTO;
import com.tianli.charge.dto.StatChargeAmount;
import com.tianli.charge.mapper.Charge;
import com.tianli.charge.mapper.ChargeMapper;
import com.tianli.charge.mapper.ChargeStatus;
import com.tianli.common.CommonFunction;
import com.tianli.common.Constants;
import com.tianli.common.HttpUtils;
import com.tianli.common.async.AsyncService;
import com.tianli.common.blockchain.BFBscContract;
import com.tianli.common.blockchain.TronTriggerContract;
import com.tianli.common.blockchain.UsdtBscContract;
import com.tianli.common.blockchain.UsdtEthContract;
import com.tianli.common.init.RequestInitService;
import com.tianli.common.log.LoggerHandle;
import com.tianli.currency.*;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.deposit.ChargeDepositService;
import com.tianli.deposit.mapper.ChargeDeposit;
import com.tianli.deposit.mapper.ChargeDepositStatus;
import com.tianli.deposit.mapper.ChargeDepositType;
import com.tianli.dividends.settlement.ChargeSettlementService;
import com.tianli.dividends.settlement.mapper.ChargeSettlement;
import com.tianli.dividends.settlement.mapper.ChargeSettlementStatus;
import com.tianli.dividends.settlement.mapper.ChargeSettlementType;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.kyc.KycService;
import com.tianli.kyc.mapper.Kyc;
import com.tianli.management.currency.CurrencyLogVO;
import com.tianli.management.fundmanagement.WithdrawalManageBhvPO;
import com.tianli.management.fundmanagement.WithdrawalManagePO;
import com.tianli.management.platformfinance.FeeDTO;
import com.tianli.management.platformfinance.FinanceExhibitionDetailDTO;
import com.tianli.management.ruleconfig.ConfigConstants;
import com.tianli.mconfig.ConfigService;
import com.tianli.role.annotation.GrcCheckModular;
import com.tianli.tool.MapBuilder;
import com.tianli.tool.MapTool;
import com.tianli.tool.WebSocketUtils;
import com.tianli.tool.crypto.Crypto;
import com.tianli.tool.http.HttpHandler;
import com.tianli.tool.http.HttpRequest;
import com.tianli.user.logs.UserIpLogService;
import com.tianli.user.logs.mapper.UserIpLogMapper;
import com.tianli.user.userinfo.UserInfoService;
import com.tianli.user.userinfo.mapper.UserInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.crypto.util.DigestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author wangqiyun
 * @Date 2020/3/31 11:25
 */

@Service
public class ChargeService extends ServiceImpl<ChargeMapper, Charge> {

    public static final BigInteger ONE_HUNDRED = new BigInteger("100");
    public static final BigInteger TEN_BILLION = new BigInteger("10000000000");

    @Transactional
    public void withdrawWebhooks(ChargeWebhooksDTO chargeWebhooksDTO) {
        String sn = chargeWebhooksDTO.getSn();
        if(StringUtils.isBlank(sn)){
            return;
        }
        CurrencyTypeEnum type;
        if(sn.startsWith("CD")){
            type = CurrencyTypeEnum.deposit;
        } else if(sn.startsWith("CS")){
            type = CurrencyTypeEnum.settlement;
        } else if(sn.startsWith("C")){
            type = CurrencyTypeEnum.normal;
        } else {
            return;
        }
        boolean update;
        TokenCurrencyType currencyType;
        Long uid;
        //链上交易成功
        if ("success".equals(chargeWebhooksDTO.getStatus())) {
            BigInteger real_amount;
            BigInteger fee;
            CurrencyLogDes logDes;
            switch (type) {
                //用户的提现
                case normal: {
                    Charge charge = chargeMapper.getBySn(sn);
                    if (charge == null) return;
                    BigInteger minerFee = chargeWebhooksDTO.getMiner_fee();
                    TokenCurrencyType minerFeeType = chargeWebhooksDTO.getMiner_fee_type();
                    currencyType = charge.getCurrency_type();
                    real_amount = charge.getReal_amount();
                    fee = charge.getFee();
                    logDes = CurrencyLogDes.提现;
                    uid = charge.getUid();
                    //修改提现表的状态
                    update = chargeMapper.success(LocalDateTime.now(), ChargeStatus.chain_success, charge.getId(), ChargeStatus.chaining, minerFee, minerFeeType) > 0;
                    break;
                }
                //没用
                case deposit:
                    ChargeDeposit chargeDeposit = chargeDepositService.getOne(new LambdaQueryWrapper<ChargeDeposit>().eq(ChargeDeposit::getSn, sn));
                    if (chargeDeposit == null) return;
                    currencyType = chargeDeposit.getCurrency_type();
                    real_amount = chargeDeposit.getReal_amount();
                    fee = chargeDeposit.getFee();
                    logDes = CurrencyLogDes.撤回;
                    uid = chargeDeposit.getUid();
                    update = chargeDepositService.success(LocalDateTime.now(), ChargeDepositStatus.transaction_success, chargeWebhooksDTO.getTxid(), chargeDeposit.getId(), ChargeDepositStatus.transacting);
                    break;
                //没用
                case settlement: {
                    ChargeSettlement chargeSettlement = chargeSettlementService.getOne(new LambdaQueryWrapper<ChargeSettlement>().eq(ChargeSettlement::getSn, sn));
                    if (chargeSettlement == null) return;
                    BigInteger minerFee = chargeWebhooksDTO.getMiner_fee();
                    TokenCurrencyType minerFeeType = chargeWebhooksDTO.getMiner_fee_type();
                    currencyType = chargeSettlement.getCurrency_type();
                    real_amount = chargeSettlement.getReal_amount();
                    fee = chargeSettlement.getFee();
                    logDes = CurrencyLogDes.结算;
                    uid = chargeSettlement.getUid();
                    update = chargeSettlementService.success(LocalDateTime.now(), ChargeSettlementStatus.transaction_success, chargeSettlement.getId(), ChargeSettlementStatus.transacting, minerFee, minerFeeType);
                    break;
                }
                default:
                    return;
            }
            if (update) {
                //成功扣款逻辑
                if (Objects.equals(currencyType, TokenCurrencyType.usdt_bep20)
                        || Objects.equals(currencyType, TokenCurrencyType.usdc_bep20)
                ) {
                    CurrencyTokenEnum token = CurrencyTokenEnum.valueOf(currencyType.name());
                    currencyService.reduce(uid, type, token, real_amount.divide(TEN_BILLION), sn, logDes.name());
                    currencyService.reduce(uid, type, token, fee.divide(TEN_BILLION), sn, CurrencyLogDes.提现手续费.name());
                } else if (Objects.equals(currencyType, TokenCurrencyType.BF_bep20)) {
                    currencyService.reduce(uid, type, real_amount, sn, logDes.name());
                    currencyService.reduce(uid, type, fee, sn, CurrencyLogDes.提现手续费.name());
                }else if (Objects.equals(currencyType, TokenCurrencyType.usdt_trc20)
                        || Objects.equals(currencyType, TokenCurrencyType.usdt_erc20)
                        || Objects.equals(currencyType, TokenCurrencyType.usdc_trc20)
                        || Objects.equals(currencyType, TokenCurrencyType.usdc_erc20)
                ) {
                    CurrencyTokenEnum token = CurrencyTokenEnum.valueOf(currencyType.name());
                    currencyService.reduce(uid, type, token, real_amount.multiply(ONE_HUNDRED), sn, logDes.name());
                    currencyService.reduce(uid, type, token, fee.multiply(ONE_HUNDRED), sn, CurrencyLogDes.提现手续费.name());
                }
            }
        }
        //链上交易失败
        else if ("fail".equals(chargeWebhooksDTO.getStatus())) {
            BigInteger amount;
            CurrencyLogDes logDes;
            switch (type) {
                //修改订单状态
                case normal:
                    Charge charge = chargeMapper.getBySn(sn);
                    if (charge == null) return;
                    currencyType = charge.getCurrency_type();
                    amount = charge.getAmount();
                    logDes = CurrencyLogDes.提现;
                    uid = charge.getUid();
                    update = chargeMapper.fail(LocalDateTime.now(), ChargeStatus.chain_fail, charge.getId(), ChargeStatus.chaining, chargeWebhooksDTO.getMiner_fee(), chargeWebhooksDTO.getMiner_fee_type()) > 0;
                    break;
                //没用
                case deposit:
                    ChargeDeposit chargeDeposit = chargeDepositService.getOne(new LambdaQueryWrapper<ChargeDeposit>().eq(ChargeDeposit::getSn, sn));
                    if (chargeDeposit == null) return;
                    currencyType = chargeDeposit.getCurrency_type();
                    amount = chargeDeposit.getAmount();
                    logDes = CurrencyLogDes.撤回;
                    uid = chargeDeposit.getUid();
                    update = chargeDepositService.fail(LocalDateTime.now(), ChargeDepositStatus.transaction_fail, chargeDeposit.getId(), ChargeDepositStatus.transacting);
                    break;
                //没用 代理商结算用的
                case settlement:
                    ChargeSettlement chargeSettlement = chargeSettlementService.getOne(new LambdaQueryWrapper<ChargeSettlement>().eq(ChargeSettlement::getSn, sn));
                    if (chargeSettlement == null) return;
                    currencyType = chargeSettlement.getCurrency_type();
                    amount = chargeSettlement.getAmount();
                    logDes = CurrencyLogDes.结算;
                    uid = chargeSettlement.getUid();
                    update = chargeSettlementService.fail(LocalDateTime.now(), ChargeSettlementStatus.transaction_fail, chargeSettlement.getId(), ChargeSettlementStatus.transacting, chargeWebhooksDTO.getMiner_fee(), chargeWebhooksDTO.getMiner_fee_type());
                    break;
                default:
                    return;
            }
            //返还冻结余额
            if (update) {
                if (Objects.equals(currencyType, TokenCurrencyType.usdt_bep20)
                        || Objects.equals(currencyType, TokenCurrencyType.usdc_bep20)
                ) {
                    CurrencyTokenEnum token = CurrencyTokenEnum.valueOf(currencyType.name());
                    currencyService.unfreeze(uid, type, token, amount.divide(TEN_BILLION), sn, logDes.name());
                } else if (Objects.equals(currencyType, TokenCurrencyType.usdt_trc20)
                        || Objects.equals(currencyType, TokenCurrencyType.usdt_erc20)
                        || Objects.equals(currencyType, TokenCurrencyType.usdc_trc20)
                        || Objects.equals(currencyType, TokenCurrencyType.usdc_erc20)
                ) {
                    CurrencyTokenEnum token = CurrencyTokenEnum.valueOf(currencyType.name());
                    currencyService.unfreeze(uid, type, token, amount.multiply(ONE_HUNDRED), sn, logDes.name());
                } else if(Objects.equals(currencyType, TokenCurrencyType.BF_bep20)){
                    currencyService.unfreeze(uid, type, amount, sn, logDes.name());
                }
            }
        }
    }

    private LineTransactionDetail getTransactionDetail(String txid) {
        LineTransactionDetail detail;
        Map<String, String> params = Maps.newHashMap();
        params.put("txid", txid);
        try {
            HttpResponse httpResponse = HttpUtils.doGet(Constants.TRANSACTION_DETAILS_HOST, Constants.TRANSACTION_DETAILS_PATH, "GET", Maps.newHashMap(), params);
            String dataJsonString = EntityUtils.toString(httpResponse.getEntity());
            detail = new Gson().fromJson(dataJsonString, LineTransactionDetail.class);
        } catch (Exception e) {
            return null;
        }
        return detail;
    }


    @Transactional
    public void withdraw(WithdrawDTO withdrawDTO) {
        Long uid = requestInitService.uid();
        String from_address = null;
        TokenCurrencyType tokenCurrencyType = withdrawDTO.getTokenCurrencyType();
        //根据提现币种 选择对应的链的主钱包地址
        switch (tokenCurrencyType) {
            case usdt_trc20:
            case usdc_trc20:
                from_address = configService.get(ConfigConstants.TRON_MAIN_WALLET_ADDRESS);
                break;
            case usdt_erc20:
            case usdc_erc20:
                from_address = configService.get(ConfigConstants.ETH_MAIN_WALLET_ADDRESS);
                break;
            case BF_bep20:
            case usdt_bep20:
            case bsc:
            case usdc_bep20:
                from_address = configService.get(ConfigConstants.BSC_MAIN_WALLET_ADDRESS);
                break;
        }
        if (from_address == null) ErrorCodeEnum.SYSTEM_ERROR.throwException();
        long id = CommonFunction.generalId();
        CurrencyTokenEnum token = withdrawDTO.getToken();
//        if (Objects.equals(tokenCurrencyType, TokenCurrencyType.BF_bep20)) {
//            token = CurrencyTokenEnum.BF_bep20;
//        } else {
//            token = CurrencyTokenEnum.usdt_omni;
//        }
        if(!Objects.equals(tokenCurrencyType, TokenCurrencyType.bsc) && !Objects.equals(tokenCurrencyType, TokenCurrencyType.BF_bep20) && Objects.equals(token, CurrencyTokenEnum.BF_bep20)){
            ErrorCodeEnum.throwException("BF代币提现链错误");
        }
        // 最小提现金额
        String withdraw_min_amount = configService._get(tokenCurrencyType.name() + "_withdraw_min_amount");
        BigInteger amount = tokenCurrencyType.amount(withdrawDTO.getAmount());
        if (amount.compareTo(new BigInteger(withdraw_min_amount)) < 0){
            ErrorCodeEnum.throwException("提现usdt数额过小");
        }

        //计算手续费  实际手续费 = 提现数额*手续费率 + 固定手续费数额
        String rate = configService._get(tokenCurrencyType.name() + "_withdraw_rate");
        String fixedAmount = configService._get(tokenCurrencyType.name() + "_withdraw_fixed_amount");
        BigInteger fee = (new BigDecimal(amount).multiply(new BigDecimal(StringUtils.isNotBlank(rate) ? rate : "0"))).toBigInteger()
                .add(new BigInteger(StringUtils.isNotBlank(fixedAmount) ? fixedAmount : "0"));
        //实际到账数额 = 提现数额 - 手续费
        BigInteger real_amount = amount.subtract(fee);
        if (fee.compareTo(BigInteger.ZERO) < 0)
            ErrorCodeEnum.FEE_LT_ZERO_ERROR.throwException();
        if (real_amount.compareTo(BigInteger.ZERO) < 0)
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
        }else{
            BigDecimal totalAmount = chargeMapper.getTotalAmount(uid, ChargeType.withdraw, TokenCurrencyType.usdt_bep20);
            BigDecimal totalAmount1 = chargeMapper.getTotalAmount(uid, ChargeType.withdraw, TokenCurrencyType.usdt_erc20);
            BigDecimal totalAmount2 = chargeMapper.getTotalAmount(uid, ChargeType.withdraw, TokenCurrencyType.usdt_trc20);
            BigDecimal totalAmount3 = chargeMapper.getTotalAmount(uid, ChargeType.withdraw, TokenCurrencyType.usdc_bep20);
            BigDecimal totalAmount4 = chargeMapper.getTotalAmount(uid, ChargeType.withdraw, TokenCurrencyType.usdc_erc20);
            BigDecimal totalAmount5 = chargeMapper.getTotalAmount(uid, ChargeType.withdraw, TokenCurrencyType.usdc_trc20);
            double totalAmountDouble = TokenCurrencyType.usdt_bep20.money(totalAmount.toBigInteger())
                    + TokenCurrencyType.usdt_erc20.money(totalAmount1.toBigInteger())
                    + TokenCurrencyType.usdt_trc20.money(totalAmount2.toBigInteger())
                    + TokenCurrencyType.usdc_bep20.money(totalAmount3.toBigInteger())
                    + TokenCurrencyType.usdc_erc20.money(totalAmount4.toBigInteger())
                    + TokenCurrencyType.usdc_trc20.money(totalAmount5.toBigInteger())
                    ;
            String orDefault = configService.getOrDefault(ConfigConstants.KYC_TRIGGER_AMOUNT, "10000");
            if(totalAmountDouble + withdrawDTO.getAmount() >= Double.parseDouble(orDefault)){
                ErrorCodeEnum.KYC_TRIGGER_ERROR.throwException();
            }
        }
        //创建提现订单
        Charge charge = Charge.builder()
                .id(id).create_time(LocalDateTime.now())
                .status(ChargeStatus.created)
                .uid(uid)
                .uid_avatar(userInfo.getAvatar())
                .uid_nick(userInfo.getNick())
                .uid_username(userInfo.getUsername())
                .sn("C" + CommonFunction.generalSn(id))
                .currency_type(tokenCurrencyType)
                .charge_type(ChargeType.withdraw)
                .amount(amount)
                .fee(fee)
                .real_amount(real_amount)
                .from_address(from_address).to_address(withdrawDTO.getAddress())
                .token(token)
                .build();
        if (chargeMapper.insert(charge) <= 0) ErrorCodeEnum.SYSTEM_ERROR.throwException();
        userIpLogService.updateBehaviorId(GrcCheckModular.提现, charge.getId());
        //转换u后面的0的个数
        if (Objects.equals(tokenCurrencyType, TokenCurrencyType.usdt_erc20)
                || Objects.equals(tokenCurrencyType, TokenCurrencyType.usdt_trc20)
                || Objects.equals(tokenCurrencyType, TokenCurrencyType.usdc_erc20)
                || Objects.equals(tokenCurrencyType, TokenCurrencyType.usdc_trc20)) {
            amount = amount.multiply(ONE_HUNDRED);
        } else if(Objects.equals(tokenCurrencyType, TokenCurrencyType.usdt_bep20)
                || Objects.equals(tokenCurrencyType, TokenCurrencyType.usdc_bep20)
        ) {
            amount = amount.divide(TEN_BILLION);
        }
        //冻结提现数额
        currencyService.freeze(uid, CurrencyTypeEnum.normal, token, amount, charge.getSn(), CurrencyLogDes.提现.name());
    }

    @Transactional
    public void receive(AddressWebhooksDTO addressWebhooksDTO) {
        Address address = null;
        TokenCurrencyType tokenCurrencyType = addressWebhooksDTO.getType();
        BigInteger amount = addressWebhooksDTO.getValue();
        //不同链的usdt后面的0个数不一样  需要做一个对齐处理 目前是后面8个0为1个u
        BigInteger finalAmount;
        CurrencyTokenEnum token;
        switch (tokenCurrencyType) {
//            case usdt_omni:
//                address = addressService.getByBtc(addressWebhooksDTO.getTo_address());
//                token = CurrencyTokenEnum.usdt_omni;
//                finalAmount = amount;
//                break;
            case usdt_erc20:
                address = addressService.getByEth(addressWebhooksDTO.getTo_address());
                token = CurrencyTokenEnum.usdt_omni;
                finalAmount = amount.multiply(ONE_HUNDRED);
                break;
            case BF_bep20:
                address = addressService.getByBsc(addressWebhooksDTO.getTo_address());
                token = CurrencyTokenEnum.BF_bep20;
                finalAmount = amount;
                break;
            case usdt_bep20:
                address = addressService.getByBsc(addressWebhooksDTO.getTo_address());
                token = CurrencyTokenEnum.usdt_bep20;
                finalAmount = amount.divide(TEN_BILLION);
                break;
            case usdt_trc20:
                address = addressService.getByTron(addressWebhooksDTO.getTo_address());
                token = CurrencyTokenEnum.usdt_trc20;
                finalAmount = amount.multiply(ONE_HUNDRED);
                break;
            case usdc_erc20:
                address = addressService.getByEth(addressWebhooksDTO.getTo_address());
                token = CurrencyTokenEnum.usdc_erc20;
                finalAmount = amount.multiply(ONE_HUNDRED);
                break;
            case usdc_bep20:
                address = addressService.getByBsc(addressWebhooksDTO.getTo_address());
                token = CurrencyTokenEnum.usdc_bep20;
                finalAmount = amount.divide(TEN_BILLION);
                break;
            case usdc_trc20:
                address = addressService.getByTron(addressWebhooksDTO.getTo_address());
                token = CurrencyTokenEnum.usdc_trc20;
                finalAmount = amount.multiply(ONE_HUNDRED);
                break;
            default: return;
        }
        final CurrencyTypeEnum type = address.getType();
        Long uid = address.getUid();

        boolean receiveFlag = false;
        //Charge表中加一条充值记录
        switch (type) {
            case normal:
                UserInfo byId = userInfoService.getOrSaveById(uid);
                if (Objects.isNull(byId)) {
                    byId = UserInfo.builder().build();
                }
                if (chargeMapper.getBySn(addressWebhooksDTO.getSn()) == null) {
                    receiveFlag = normalReceive(addressWebhooksDTO, byId, amount, amount, token);
                }
                break;
            //没用
            case deposit:
                Agent depositAgent = agentService.getById(uid);
                ChargeDeposit depositServiceOne = chargeDepositService.getOne(new LambdaQueryWrapper<ChargeDeposit>()
                        .eq(ChargeDeposit::getSn, addressWebhooksDTO.getSn()));
                if (depositServiceOne == null) {
                    receiveFlag = depositReceive(addressWebhooksDTO, depositAgent, amount, amount);
                }
                break;
            //没用
            case settlement:
                Agent settlementAgent = agentService.getById(uid);
                ChargeSettlement settlementServiceOne = chargeSettlementService.getOne(new LambdaQueryWrapper<ChargeSettlement>()
                        .eq(ChargeSettlement::getSn, addressWebhooksDTO.getSn()));
                if (settlementServiceOne == null) {
                    receiveFlag = settlementReceive(addressWebhooksDTO, settlementAgent, amount, amount);
                }
                break;
        }
        if (receiveFlag) {
            //加钱 余额在Currency这个表
            currencyService.increase(uid, type, token, finalAmount, addressWebhooksDTO.getSn(), CurrencyLogDes.充值.name());
            CurrencyLogVO currencyLogVO = new CurrencyLogVO();
            currencyLogVO.setUid(uid);
            WebSocketUtils.convertAndSendAdmin(currencyLogVO);
//            if (TokenCurrencyType.usdt_omni.equals(tokenCurrencyType)) {
//                currencyService.increase(uid, type, amount, addressWebhooksDTO.getSn(), CurrencyLogDes.充值.name());
//                Currency currency = currencyService.get(uid, type);
//                remain = currency.getRemain();
//            } else if (TokenCurrencyType.usdt_erc20.equals(tokenCurrencyType)) {
//                DigitalCurrency digitalCurrency = new DigitalCurrency(tokenCurrencyType, amount).toOther(TokenCurrencyType.usdt_omni);
//                currencyService.increase(uid, type, digitalCurrency.getAmount(), addressWebhooksDTO.getSn(), CurrencyLogDes.充值.name());
//                Currency currency = currencyService.get(uid, type);
//                remain = currency.getRemain();
//            } else {
//                currencyService.increase(uid, type, amount.multiply(new BigInteger("100")), addressWebhooksDTO.getSn(), CurrencyLogDes.充值.name());
//                Currency currency = currencyService.get(uid, type);
//                remain = currency.getRemain();
//            }
//            final BigInteger remainFinal = remain;
            // 商定分红比例 -> 实际的分红比例
//            if (type.equals(CurrencyTypeEnum.deposit)) {
//                asyncService.asyncSuccessRequest(() -> agentService.updateRealDividends(uid, remainFinal));
//            }
        }
//        final Address address_ = address;
//        asyncService.asyncSuccessRequest(() -> chainService.update(address_.getId(), address_, tokenCurrencyType, false));

    }

    /**
     * settlement充值回调处理
     */
    private boolean settlementReceive(AddressWebhooksDTO addressWebhooksDTO, Agent agent,
                                      BigInteger amount, BigInteger real_amount) {
        LocalDateTime now = LocalDateTime.now();
        ChargeSettlement charge = ChargeSettlement.builder().id(CommonFunction.generalId()).create_time(now)
                .complete_time(now).status(ChargeSettlementStatus.transaction_success).uid(agent.getId())
                .uid_username(agent.getUsername()).uid_nick(agent.getNick())
                .sn(addressWebhooksDTO.getSn())
                .currency_type(addressWebhooksDTO.getType()).charge_type(ChargeSettlementType.recharge)
                .amount(amount).fee(BigInteger.ZERO).real_amount(real_amount).from_address(addressWebhooksDTO.getFrom_address())
                .to_address(addressWebhooksDTO.getTo_address()).txid(addressWebhooksDTO.getTxid())
//                    .block(addressWebhooksDTO.getBlock())
                .build();
        return chargeSettlementService.save(charge);
    }

    /**
     * deposit充值回调处理
     */
    private boolean depositReceive(AddressWebhooksDTO addressWebhooksDTO, Agent agent,
                                   BigInteger amount, BigInteger real_amount) {
        LocalDateTime now = LocalDateTime.now();
        ChargeDeposit charge = ChargeDeposit.builder().id(CommonFunction.generalId()).create_time(now)
                .complete_time(now).status(ChargeDepositStatus.transaction_success).uid(agent.getId())
                .uid_username(agent.getUsername()).uid_nick(agent.getNick())
                .sn(addressWebhooksDTO.getSn())
                .currency_type(addressWebhooksDTO.getType()).charge_type(ChargeDepositType.recharge).amount(amount).fee(BigInteger.ZERO)
                .real_amount(real_amount).from_address(addressWebhooksDTO.getFrom_address())
                .to_address(addressWebhooksDTO.getTo_address()).txid(addressWebhooksDTO.getTxid())
//                    .block(addressWebhooksDTO.getBlock())
                .build();
        return chargeDepositService.save(charge);
    }

    /**
     * normal充值回调处理
     */
    private boolean normalReceive(AddressWebhooksDTO addressWebhooksDTO, UserInfo userInfo,
                                  BigInteger amount, BigInteger real_amount, CurrencyTokenEnum token) {
        LocalDateTime now = LocalDateTime.now();
        Charge charge = Charge.builder().id(CommonFunction.generalId()).create_time(now)
                .complete_time(now).status(ChargeStatus.chain_success).uid(userInfo.getId())
                .uid_username(userInfo.getUsername()).uid_nick(userInfo.getNick()).uid_avatar(userInfo.getAvatar())
                .sn(addressWebhooksDTO.getSn())
                .currency_type(addressWebhooksDTO.getType())
                .charge_type(ChargeType.recharge)
                .amount(amount)
                .fee(BigInteger.ZERO)
                .real_amount(real_amount).from_address(addressWebhooksDTO.getFrom_address())
                .to_address(addressWebhooksDTO.getTo_address()).txid(addressWebhooksDTO.getTxid())
                .token(token)
                .build();
        long insert = chargeMapper.insert(charge);
        return insert > 0;
    }

    @Resource
    private ChargeService chargeService;

    @Resource
    private BFBscContract bFBscContract;

    @Resource
    private UsdtBscContract usdtBscContract;

    @Resource
    private UsdtEthContract usdtEthContract;

    public void uploadChain(Charge charge) {
        chargeService.uploadChain(charge.getCurrency_type(), charge.getReal_amount(), charge.getSn(), charge.getFrom_address(), charge.getTo_address());
    }

    /**
     * 提现
     */
    public void uploadChain2(Charge charge) {
        // todo transfer 代币到对应地址
        CurrencyTokenEnum token = charge.getToken();
        TokenCurrencyType currency_type = charge.getCurrency_type();
        String txid = null;
        //BSC链的交易
        if(Objects.equals(currency_type, TokenCurrencyType.usdt_bep20) || TokenCurrencyType.BF_bep20.equals(currency_type) || TokenCurrencyType.usdc_bep20.equals(currency_type)){
            Result result = null;
            switch (token){
                case usdt_bep20:
                    result = usdtBscContract.transfer(charge.getTo_address(), charge.getReal_amount());
                    break;
                //这个币没用了
                case BF_bep20:
                    result = bFBscContract.transfer(charge.getTo_address(), charge.getReal_amount());
                    break;
                case usdc_bep20:
                    result = usdtBscContract.transferUsdc(charge.getTo_address(), charge.getReal_amount());
                    break;
            }
            if(Objects.isNull(result) || !Objects.equals(result.getCode(), "0")){
                ErrorCodeEnum.throwException("上链失败, 请稍后重试");
            }
            txid = result.getData().toString();
        }
        //波场链 提现
        else if(TokenCurrencyType.usdt_trc20.equals(currency_type) || TokenCurrencyType.usdc_trc20.equals(currency_type)) {
            try {
                switch (token){
                    case usdt_trc20:
                        txid = tronTriggerContract.transferUsdt(charge.getTo_address(), charge.getReal_amount());
                        break;
                    case usdc_trc20:
                        txid = tronTriggerContract.transferUsdc(charge.getTo_address(), charge.getReal_amount());
                        break;
                }
            } catch (Exception e) {
                ErrorCodeEnum.throwException("上链失败, 请稍后重试");
            }
        }
        //以太坊链提现
        else if(TokenCurrencyType.usdt_erc20.equals(currency_type) ||  TokenCurrencyType.usdc_erc20.equals(currency_type)) {
            Result result = null;
            try {
                switch (token){
                    case usdt_erc20:
                        result = usdtEthContract.transfer(charge.getTo_address(), charge.getReal_amount());
                        break;
                    case usdc_erc20:
                        result = usdtEthContract.transferUsdc(charge.getTo_address(), charge.getReal_amount());
                        break;
                }
            } catch (Exception e) {
                ErrorCodeEnum.throwException("上链失败, 请稍后重试");
            }
            if (Objects.isNull(result) || !Objects.equals(result.getCode(), "0")) {
                ErrorCodeEnum.throwException("上链失败, 请稍后重试");
            }
            txid = result.getData().toString();
        } else {
            ErrorCodeEnum.throwException("不支持的提现类型");
        }

        // 更新txid
        boolean update = false;
        while (!update){
            update = chargeService.update(Wrappers.<Charge>lambdaUpdate()
                    .set(Charge::getTxid, txid)
                    .eq(Charge::getId, charge.getId())
            );
        }

    }

    /**
     * 结算提现
     */
    public void uploadChain2(ChargeSettlement cs) {
        // todo transfer 代币到对应地址
        CurrencyTokenEnum token = cs.getToken();
        TokenCurrencyType currency_type = cs.getCurrency_type();
        String txid = null;
        if(Objects.equals(currency_type, TokenCurrencyType.usdt_bep20) || Objects.equals(currency_type, TokenCurrencyType.BF_bep20)){
            Result result = null;
            switch (token){
                case usdt_bep20:
                    result = usdtBscContract.transfer(cs.getTo_address(), cs.getReal_amount());
                    break;
                case BF_bep20:
                    result = bFBscContract.transfer(cs.getTo_address(), cs.getReal_amount());
                    break;
            }
            if(Objects.isNull(result) || !Objects.equals(result.getCode(), "0")){
                ErrorCodeEnum.throwException("上链失败, 请稍后重试");
            }
            txid = result.getData().toString();
        } else if (Objects.equals(TokenCurrencyType.usdt_trc20, currency_type) && Objects.equals(CurrencyTokenEnum.usdt_omni, token)){
            try {
                txid = tronTriggerContract.transferUsdt(cs.getTo_address(), cs.getReal_amount());
            } catch (Exception e) {
                ErrorCodeEnum.throwException("上链失败, 请稍后重试");
            }
        } else if (TokenCurrencyType.usdt_erc20.equals(currency_type) && Objects.equals(CurrencyTokenEnum.usdt_omni, token)) {
            Result result;
            try {
                result = usdtEthContract.transfer(cs.getTo_address(), cs.getReal_amount());
            } catch (Exception e) {
                result = null;
                ErrorCodeEnum.throwException("上链失败, 请稍后重试");
            }
            if (Objects.isNull(result) || !Objects.equals(result.getCode(), "0")) {
                ErrorCodeEnum.throwException("上链失败, 请稍后重试");
            }
            txid = result.getData().toString();
        } else {
            ErrorCodeEnum.throwException("不支持的提现类型");
        }
        // 更新txid
        boolean update = false;
        while (!update){
            update = chargeSettlementService.update(Wrappers.<ChargeSettlement>lambdaUpdate()
                    .set(ChargeSettlement::getTxid, txid)
                    .eq(ChargeSettlement::getId, cs.getId())
            );
        }

    }

    @Transactional
    public void uploadChain(TokenCurrencyType currency_type, BigInteger real_amount, String sn, String from_address, String to_address) {
        asyncService.asyncSuccessRequest(() -> {
            String wallet_app_key = configService.get("wallet_app_key");
            String wallet_app_secret = configService.get("wallet_app_secret");
            String wallet_url = configService.getOrDefault("wallet_url", "https://www.twallet.pro/api");
            String url = configService.get("url");
            MapTool paramMap = MapTool.Map().put("sn", sn).put("amount", real_amount.toString()).put("from_address",
                    from_address).put("to_address", to_address).put("type", currency_type.toString())
                    .put("notify_url", url + "/charge/webhooks");
            String param = new Gson().toJson(paramMap);
            System.out.println("提现审核上链参数 ==> " + param);
            String stringResult = HttpHandler.execute(new HttpRequest().setMethod(HttpRequest.Method.POST).setUrl(wallet_url + "/order/loan/create")
                    .setRequestHeader(MapBuilder.Map().put("AppKey", wallet_app_key)
                            .put("Sign", Crypto.hmacToString(DigestFactory.createSHA256(), wallet_app_secret, param)).build())
                    .setJsonString(param)).getStringResult();
            System.out.println("提现审核上链结果 ==> " + stringResult);
            loggerHandle.log(MapTool.Map().put("loan_result", stringResult));
        });
    }

    public List<Charge> withdrawApplyPage(Long uid, ChargeStatus status, Integer page, Integer size) {
        return selectPage(uid, status, ChargeType.withdraw, null, null, null, null, page, size);
    }

    public List<Charge> selectPage(Long uid,
                                   ChargeStatus status,
                                   ChargeType type,
                                   String phone,
                                   String txid,
                                   String startTime,
                                   String endTime,
                                   Integer page,
                                   Integer size) {
        return chargeMapper.selectPage(uid, status, type, phone, txid, startTime, endTime, Math.max((page - 1) * size, 0), size);
    }

    public int selectCount(Long uid,
                           ChargeStatus status,
                           ChargeType type,
                           String phone,
                           String txid,
                           String startTime,
                           String endTime) {
        return chargeMapper.selectCount(uid, status, type, phone, txid, startTime, endTime);
    }

    public int selectNewCount(String ip,
                              String equipment,
                              Boolean grc_result,
                              Boolean otherSec,
                              Long uid,
                              ChargeStatus status,
                              ChargeType type,
                              String phone,
                              String txid,
                              String startTime,
                              String endTime) {
        return chargeMapper.selectNewCount(ip, equipment, grc_result, otherSec, uid, status, type, phone, txid, startTime, endTime);
    }

    public Map<String, BigDecimal> sumChargeAmount(ChargeType recharge, String phone, String txid, String startTime, String endTime) {
        return chargeMapper.selectSumChargeAmount(recharge, phone, txid, startTime, endTime);
    }

//    public BigInteger sumChargeBFAmount(ChargeType recharge, String phone, String txid, String startTime, String endTime) {
//        return chargeMapper.selectSumChargeAmount(recharge, phone, txid, startTime, endTime, CurrencyTokenEnum.BF_bep20);
//    }

    public Charge getById(long id) {
        return chargeMapper.getById(id);
    }

    public List<FeeDTO> getDailySumFee(LocalDate startDay, LocalDate endDay) {
        return chargeMapper.getDailySumFee2(startDay, endDay);
    }

    public List<FinanceExhibitionDetailDTO> getDailyFeeDetails(LocalDate startDay, LocalDate endDay) {
        return chargeMapper.getDailyFeeDetails(startDay, endDay);
    }

    public double totalWithdrawAmount() {
        List<StatChargeAmount> statCollectAmounts = chargeMapper.totalWithdrawAmount();
        return statCollectAmounts.stream().map(e -> new DigitalCurrency(e.getCurrency_type(), e.getTotal_amount()).toOther(TokenCurrencyType.usdt_omni).getMoney()).reduce(Double::sum).orElse(0.0);
    }

    @Resource
    private LoggerHandle loggerHandle;
    @Resource
    private AsyncService asyncService;
    @Resource
    private ConfigService configService;
    @Resource
    private RequestInitService requestInitService;
    @Resource
    private CurrencyService currencyService;
    @Resource
    private UserIpLogService userIpLogService;
    @Resource
    private ChargeMapper chargeMapper;
    @Resource
    private ChargeDepositService chargeDepositService;
    @Resource
    private ChargeSettlementService chargeSettlementService;
    @Resource
    private AddressService addressService;
    @Resource
    private UserInfoService userInfoService;
    @Resource
    private AgentService agentService;
    @Resource
    private TronTriggerContract tronTriggerContract;
    @Resource
    private UserIpLogMapper userIpLogMapper;
    @Resource
    private KycService kycService;

    public WithdrawalManageBhvPO getInfoById(long id) {
        WithdrawalManageBhvPO po = chargeMapper.selectInfoById(id);
        // 计算汇总数据
        Map<String, BigDecimal> map = userIpLogMapper.selectKindCount(po.getUid_username());
        Map<String, Long> map2 = userIpLogMapper.selectIpOtherCount(po.getIp(), po.getUid_username());
        Map<String, Long> map3 = userIpLogMapper.selectEquipmentOtherCount(po.getEquipment(), po.getUid_username());
        po.fillProperties(map, map2, map3);
        return po;
    }

    public List<WithdrawalManagePO> selectNewPage(String ip, String equipment, Boolean grc_result, Boolean otherSec, Long uid, ChargeStatus status, ChargeType withdraw, String phone, String txid, String startTime, String endTime, Integer page, Integer size) {
        return chargeMapper.selectNewPage(ip, equipment, grc_result, otherSec, uid, status, withdraw, phone, txid, startTime, endTime, Math.max((page - 1) * size, 0), size);
    }

    public List<Map<String,Object>> totalAmount(String ip, String equipment, Boolean grc_result, Boolean otherSec, Long uid, ChargeStatus status, ChargeType withdraw, String phone, String txid, String startTime, String endTime) {
        return chargeMapper.totalAmount(ip,equipment,grc_result,otherSec,uid,status,withdraw,phone,txid,startTime,endTime);
    }
}
