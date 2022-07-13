package com.tianli.charge;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.JsonObject;
import com.tianli.account.entity.AccountBalance;
import com.tianli.account.enums.AccountChangeType;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.address.AddressService;
import com.tianli.address.mapper.Address;
import com.tianli.address.query.RechargeCallbackQuery;
import com.tianli.charge.controller.ChargeWebhooksDTO;
import com.tianli.charge.converter.ChargeConverter;
import com.tianli.charge.dto.StatChargeAmount;
import com.tianli.charge.entity.Charge;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.mapper.ChargeMapper;
import com.tianli.charge.query.WithdrawQuery;
import com.tianli.charge.vo.ChargeVO;
import com.tianli.common.CommonFunction;
import com.tianli.common.ConfigConstants;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.currency.DigitalCurrency;
import com.tianli.currency.enums.CurrencyAdaptType;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.mconfig.ConfigService;
import com.tianli.sso.init.RequestInitService;
import com.tianli.sso.init.SignUserInfo;
import com.tianli.tool.judge.JsonObjectTool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author wangqiyun
 * @since 2020/3/31 11:25
 */

@Slf4j
@Service
public class ChargeService extends ServiceImpl<ChargeMapper, Charge> {

    public static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    public static final BigDecimal TEN_BILLION = new BigDecimal("10000000000");

    @Resource
    private ChargeMapper chargeMapper;
    @Resource
    private ConfigService configService;
    @Resource
    private RequestInitService requestInitService;
    @Resource
    private AccountBalanceService accountBalanceService;
    @Resource
    private AddressService addressService;
    @Resource
    private ChargeConverter chargeConverter;

    /**
     * 充值回调:添加用户余额和记录
     *
     * @param query 充值信息
     */
    @Transactional
    public void rechargeCallback(RechargeCallbackQuery query) {
        Address address  = getAddress(query);
        Long uid = address.getUid();
        AccountChangeType type  = AccountChangeType.getInstanceBySn(query.getSn());
        BigDecimal finalAmount = query.getType().moneyBigDecimal(query.getValue());

        boolean receiveFlag = false;
        if (chargeMapper.getBySn(query.getSn()) == null) {
            receiveFlag = financialRechargeReceive(query, finalAmount, query.getValue());
        }

        if (receiveFlag) {
            accountBalanceService.increase(uid,type, query.getType(), finalAmount, query.getSn(), CurrencyLogDes.充值.name());
        }

    }

    @Transactional
    public void withdrawWebhooks(ChargeWebhooksDTO chargeWebhooksDTO) {
        String sn = chargeWebhooksDTO.getSn();

        boolean update = false;
        CurrencyAdaptType currencyAdaptType = null;
        Long uid = null;
        //链上交易成功
        if ("success".equals(chargeWebhooksDTO.getStatus())) {
            BigDecimal realAmount;
            BigDecimal fee;
            CurrencyLogDes logDes;
            //用户的提现
            Charge charge = chargeMapper.getBySn(sn);
            if (charge == null) return;
            BigInteger minerFee = chargeWebhooksDTO.getMinerFee();
            CurrencyAdaptType minerFeeType = chargeWebhooksDTO.getMinerFeeType();
            currencyAdaptType = charge.getCurrencyAdaptType();
            realAmount = charge.getRealFee();
            fee = charge.getServiceFee();
            logDes = CurrencyLogDes.提现;
            uid = charge.getUid();
            //修改提现表的状态
            update = chargeMapper.success(LocalDateTime.now(), ChargeStatus.chain_success, charge.getId(), ChargeStatus.chaining, minerFee, minerFeeType) > 0;

            if (update) {
                realAmount = currencyAdaptType.moneyBigDecimal(realAmount.toBigInteger());
                fee = currencyAdaptType.moneyBigDecimal(fee.toBigInteger());

                accountBalanceService.reduce(uid, AccountChangeType.normal, currencyAdaptType, realAmount, sn, logDes.name());
                accountBalanceService.reduce(uid, AccountChangeType.normal, currencyAdaptType, fee, sn, CurrencyLogDes.提现手续费.name());
            }
        }
        //链上交易失败
        if ("fail".equals(chargeWebhooksDTO.getStatus())) {
            BigDecimal amount;
            CurrencyLogDes logDes;
            //修改订单状态
            Charge charge = chargeMapper.getBySn(sn);
            if (charge == null) return;
            currencyAdaptType = charge.getCurrencyAdaptType();
            amount = charge.getFee();
            logDes = CurrencyLogDes.提现;
            uid = charge.getUid();
            update = chargeMapper.fail(LocalDateTime.now(), ChargeStatus.chain_fail, charge.getId(), ChargeStatus.chaining, chargeWebhooksDTO.getMinerFee(), chargeWebhooksDTO.getMinerFeeType()) > 0;

            //返还冻结余额
            if (update) {
                amount = currencyAdaptType.moneyBigDecimal(amount.toBigInteger());
                accountBalanceService.unfreeze(uid, AccountChangeType.normal, currencyAdaptType, amount.multiply(ONE_HUNDRED), sn, logDes.name());
            }
        }
    }

    @Transactional
    public void withdraw(WithdrawQuery withdrawDTO) {
        Long uid = requestInitService.uid();
        CurrencyAdaptType currencyAdaptType = withdrawDTO.getCurrencyAdaptType();

        String fromAddress = getMainWalletAddressUrl(currencyAdaptType);
        // 计算手续费  实际手续费 = 提现数额 * 手续费率 + 固定手续费数额
        // 最小提现金额
        String withdrawMinAmount = configService._get(currencyAdaptType.name() + "_withdraw_min_amount");
        // 手续费率
        String rate = configService._get(currencyAdaptType.name() + "_withdraw_rate");
        // 固定手续费数额
        String fixedAmount = configService._get(currencyAdaptType.name() + "_withdraw_fixed_amount");

        // 提现数额
        BigDecimal withdrawAmount = currencyAdaptType.moneyBigDecimal(new BigInteger("" + withdrawDTO.getAmount()));
        if (withdrawAmount.compareTo(new BigDecimal(withdrawMinAmount)) < 0) ErrorCodeEnum.throwException("提现usdt数额过小");

        // 手续费
        BigDecimal serviceAmount = (withdrawAmount.multiply(new BigDecimal(StringUtils.isNotBlank(rate) ? rate : "0")))
                .add(new BigDecimal(StringUtils.isNotBlank(fixedAmount) ? fixedAmount : "0"));
        BigDecimal realAmount = withdrawAmount.subtract(serviceAmount);

        if (serviceAmount.compareTo(BigDecimal.ZERO) < 0)   ErrorCodeEnum.FEE_LT_ZERO_ERROR.throwException();
        if (realAmount.compareTo(BigDecimal.ZERO) < 0)  ErrorCodeEnum.WITHDRAWAL_AMOUNT_LT_FEE_ERROR.throwException();

        SignUserInfo userInfo = requestInitService.get().getUserInfo();
        AccountBalance accountBalance = accountBalanceService.getAndInit(uid, currencyAdaptType);

        //创建提现订单(提币申请)
        long id = CommonFunction.generalId();
        Charge charge = baseChargeBuilder(uid,userInfo,withdrawAmount,serviceAmount,realAmount,fromAddress,withdrawDTO.getAddress())
                .id(id)
                .sn(AccountChangeType.normal.getPrefix()+ CommonFunction.generalSn(id) )
                .status(ChargeStatus.created)
                .chargeType(ChargeType.withdraw)
                .currencyAdaptType(currencyAdaptType)
                .accountBalanceId(accountBalance.getId())
                .build();
        if (chargeMapper.insert(charge) <= 0) ErrorCodeEnum.SYSTEM_ERROR.throwException();
        //转换u后面的0的个数
        withdrawAmount = withdrawAmount.multiply(ONE_HUNDRED);

        //冻结提现数额
        accountBalanceService.freeze(uid, AccountChangeType.normal, currencyAdaptType, withdrawAmount, charge.getSn(), CurrencyLogDes.提现.name());
    }

    /**
     * 获取充值DTO数据 不同链的usdt后面的0个数不一样  需要做一个对齐处理 目前是后面8个0为1个u
     */
    private Address getAddress(RechargeCallbackQuery query) {
        String addressQuery = query.getFromAddress();
        Address address = null;
        switch (query.getType()) {
            case usdt_erc20:
            case usdc_erc20:
                address = addressService.getByEth(addressQuery);
                break;
            case usdt_bep20:
            case usdc_bep20:
                address = addressService.getByBsc(addressQuery);
                break;
            case usdt_trc20:
            case usdc_trc20:
                address = addressService.getByTron(addressQuery);
                break;
            default: break;
        }
        if(address == null ){
           throw ErrorCodeEnum.CURRENCY_NOT_SUPPORT.generalException();
        }
        return address;
    }

    /**
     * 理财充值记录添加
     */
    private boolean financialRechargeReceive(RechargeCallbackQuery query,BigDecimal amount, BigDecimal realAmount) {
        var userInfo = requestInitService.get().getUserInfo();
        Long uid = requestInitService.get().getUid();
        AccountBalance accountBalance = accountBalanceService.getAndInit(uid, query.getType());

        LocalDateTime now = LocalDateTime.now();
        Charge charge = baseChargeBuilder(uid,userInfo,amount,BigDecimal.ZERO,realAmount,query.getFromAddress(),query.getToAddress())
                .sn(query.getSn())
                .completeTime(now)
                .status(ChargeStatus.chain_success)
                .currencyAdaptType(query.getType())
                .chargeType(ChargeType.recharge)
                .txid(query.getTxId())
                .accountBalanceId(accountBalance.getId())
                .build();
        long insert = chargeMapper.insert(charge);
        return insert > 0;
    }

    /**
     * 获取分页数据
     */
    public List<ChargeVO> pageByChargeType(Long uid, CurrencyCoin currencyCoin, ChargeType chargeType, Page<Charge> page) {
        AccountBalance accountBalance = accountBalanceService.get(uid, currencyCoin);
        LambdaQueryWrapper<Charge> wrapper = new LambdaQueryWrapper<Charge>()
                .eq(Charge::getUid, uid)
                .eq(Charge::getAccountBalanceId,accountBalance.getId());
        if(Objects.nonNull(chargeType)){
            wrapper = wrapper.eq(Charge::getChargeType, chargeType);
        }
        Page<Charge> charges = this.page(page,wrapper);
        return charges.getRecords().stream().map(chargeConverter :: toVO).collect(Collectors.toList());
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
                              Boolean grcResult,
                              Boolean otherSec,
                              Long uid,
                              ChargeStatus status,
                              ChargeType type,
                              String phone,
                              String txid,
                              String startTime,
                              String endTime) {
        return chargeMapper.selectNewCount(ip, equipment, grcResult, otherSec, uid, status, type, phone, txid, startTime, endTime);
    }

    public Charge getById(long id) {
        return chargeMapper.getById(id);
    }

    public double totalWithdrawAmount() {
        List<StatChargeAmount> statCollectAmounts = chargeMapper.totalWithdrawAmount();
        return statCollectAmounts.stream().map(e -> new DigitalCurrency(e.getCurrency_type(), e.getTotal_amount()).toOther(CurrencyAdaptType.usdt_omni).getMoney()).reduce(Double::sum).orElse(0.0);
    }

    public List<Charge> withdrawApplyPage(Long uid, ChargeStatus status, Integer page, Integer size) {
        return selectPage(uid, status, ChargeType.withdraw, null, null, null, null, page, size);
    }

    /**
     * 获取主钱包地址
     */
    public String getMainWalletAddressUrl(CurrencyAdaptType currencyAdaptType){
        String fromAddress = null;
        switch (currencyAdaptType) {
            case usdt_trc20:
            case usdc_trc20:
                fromAddress = configService.get(ConfigConstants.TRON_MAIN_WALLET_ADDRESS);
                break;
            case usdt_erc20:
            case usdc_erc20:
                fromAddress = configService.get(ConfigConstants.ETH_MAIN_WALLET_ADDRESS);
                break;
            case BF_bep20:
            case usdt_bep20:
            case bsc:
            case usdc_bep20:
                fromAddress = configService.get(ConfigConstants.BSC_MAIN_WALLET_ADDRESS);
                break;
            default:
                break;
        }
        if (fromAddress == null) ErrorCodeEnum.SYSTEM_ERROR.throwException();
        return fromAddress;
    }

    /**
     * 基础的订单对象创建方法
     */
    private Charge.ChargeBuilder baseChargeBuilder(Long uid,SignUserInfo userInfo,BigDecimal fee,BigDecimal serviceFee
            ,BigDecimal realFee,String fromAddress,String toAddress){
        return Charge.builder()
                .id(CommonFunction.generalId())
                .uid(uid)
                .uidUsername(userInfo.getAddress())
                .createTime(LocalDateTime.now())
                .fee(fee).realFee(realFee).serviceFee(serviceFee)
                .fromAddress(fromAddress).toAddress(toAddress);
    }
}
