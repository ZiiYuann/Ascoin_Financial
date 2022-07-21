package com.tianli.charge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.account.enums.AccountChangeType;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.address.AddressService;
import com.tianli.address.mapper.Address;
import com.tianli.address.query.RechargeCallbackQuery;
import com.tianli.charge.converter.ChargeConverter;
import com.tianli.charge.entity.Order;
import com.tianli.charge.entity.OrderChargeInfo;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.mapper.OrderMapper;
import com.tianli.charge.query.RedeemQuery;
import com.tianli.charge.query.WithdrawQuery;
import com.tianli.charge.vo.*;
import com.tianli.common.CommonFunction;
import com.tianli.common.ConfigConstants;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.currency.enums.CurrencyAdaptType;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.financial.entity.FinancialRecord;
import com.tianli.financial.enums.ProductType;
import com.tianli.financial.enums.RecordStatus;
import com.tianli.financial.service.FinancialRecordService;
import com.tianli.management.query.FinancialChargeQuery;
import com.tianli.mconfig.ConfigService;
import com.tianli.sso.init.RequestInitService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * @author wangqiyun
 * @since 2020/3/31 11:25
 */

@Slf4j
@Service
public class ChargeService extends ServiceImpl<OrderMapper, Order> {

    public static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    /**
     * 充值回调:添加用户余额和记录
     *
     * @param query 充值信息
     */
    @Transactional
    public void rechargeCallback(RechargeCallbackQuery query) {
        Address address = getAddress(query);
        Long uid = address.getUid();
        BigDecimal finalAmount = query.getType().moneyBigDecimal(query.getValue());

        if (orderService.getOrderChargeByTxid(query.getTxId()) != null) {
            log.error("txid {} 已经存在充值订单", query.getTxId());
        }
        String orderNo = insertRechargeOrder(query, finalAmount, query.getValue());
        accountBalanceService.increase(uid, ChargeType.recharge, query.getType(), finalAmount, orderNo
                , CurrencyLogDes.充值.name());
    }

    @Transactional
    public void withdraw(Long uid,WithdrawQuery query) {
        CurrencyAdaptType currencyAdaptType = query.getCurrencyAdaptType();

        String fromAddress = getMainWalletAddressUrl(currencyAdaptType);
        // 计算手续费  实际手续费 = 提现数额 * 手续费率 + 固定手续费数额
        // 最小提现金额
        String withdrawMinAmount = configService._get(currencyAdaptType.name() + "_withdraw_min_amount");
        // 手续费率
        String rate = configService._get(currencyAdaptType.name() + "_withdraw_rate");
        // 固定手续费数额
        String fixedAmount = configService._get(currencyAdaptType.name() + "_withdraw_fixed_amount");

        // 提现数额
        BigDecimal withdrawAmount = currencyAdaptType.moneyBigDecimal(new BigInteger("" + query.getAmount()));
        if (withdrawAmount.compareTo(new BigDecimal(withdrawMinAmount)) < 0) ErrorCodeEnum.throwException("提现usdt数额过小");

        // 手续费
        BigDecimal serviceAmount = (withdrawAmount.multiply(new BigDecimal(StringUtils.isNotBlank(rate) ? rate : "0")))
                .add(new BigDecimal(StringUtils.isNotBlank(fixedAmount) ? fixedAmount : "0"));
        BigDecimal realAmount = withdrawAmount.subtract(serviceAmount);

        if (serviceAmount.compareTo(BigDecimal.ZERO) < 0) ErrorCodeEnum.FEE_LT_ZERO_ERROR.throwException();
        if (realAmount.compareTo(BigDecimal.ZERO) < 0) ErrorCodeEnum.WITHDRAWAL_AMOUNT_LT_FEE_ERROR.throwException();

        LocalDateTime now = requestInitService.now();

        // 链信息
        OrderChargeInfo orderChargeInfo = OrderChargeInfo.builder()
                .id(CommonFunction.generalId())
                .txid(null)
                .coin(currencyAdaptType.getCurrencyCoin())
                .network(currencyAdaptType.getCurrencyNetworkType())
                .fee(withdrawAmount)
                .realFee(realAmount)
                .serviceFee(BigDecimal.ZERO)
                .fromAddress(fromAddress)
                .createTime(now)
                .toAddress(query.getAddress()).build();
        orderService.insert(orderChargeInfo);

        //创建提现订单(提币申请)
        long id = CommonFunction.generalId();
        Order order = new Order();
        order.setUid(uid);
        order.setAmount(realAmount);
        order.setOrderNo(AccountChangeType.normal.getPrefix() + CommonFunction.generalSn(id));
        order.setStatus(ChargeStatus.created);
        order.setType(ChargeType.withdraw);
        order.setCoin(currencyAdaptType.getCurrencyCoin());
        order.setCreateTime(now);
        orderService.saveOrder(order);

        //转换u后面的0的个数
        withdrawAmount = withdrawAmount.multiply(ONE_HUNDRED);

        //冻结提现数额
        accountBalanceService.freeze(uid, ChargeType.withdraw, currencyAdaptType, withdrawAmount
                , order.getOrderNo(), CurrencyLogDes.提现.name());
    }

    @Transactional
    public String redeem(Long uid,RedeemQuery query){
        // todo 计算利息的时候不允许进行赎回操
        Long recordId = query.getRecordId();
        FinancialRecord record = Optional.ofNullable(financialRecordService.getOne(new LambdaQueryWrapper<FinancialRecord>()
                        .eq(FinancialRecord::getUid, uid).eq(FinancialRecord::getId, recordId)))
                .orElseThrow(ErrorCodeEnum.ARGUEMENT_ERROR::generalException);

        if(RecordStatus.SUCCESS.equals(record.getStatus())){
            log.info("recordId:{},已经处于完成状态，请校验是否有误",recordId);
            ErrorCodeEnum.TRADE_FAIL.throwException();
        }

        if(query.getRedeemAmount().compareTo(record.getHoldAmount()) > 0 ){
            log.info("赎回金额 {}  大于持有金额 {}",query.getRedeemAmount(),record.getHoldAmount());
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }

        //创建赎回订单  没有审核操作，在一个事物里无需操作
        LocalDateTime now = LocalDateTime.now();
        long id = CommonFunction.generalId();
        Order order = new Order();
        order.setId(id);
        order.setUid(uid);
        order.setAmount(query.getRedeemAmount());
        order.setOrderNo(AccountChangeType.financial.getPrefix() + CommonFunction.generalSn(id));
        order.setStatus(ChargeStatus.chain_success);
        order.setType(ChargeType.redeem);
        order.setRelatedId(recordId);
        order.setCoin(record.getCoin());
        order.setCreateTime(now);
        order.setCompleteTime(now);
        orderService.saveOrder(order);

        // 扣除持有金额
        financialRecordService.redeem(recordId,query.getRedeemAmount());
        // 解冻余额
        accountBalanceService.unfreeze(uid,ChargeType.redeem,query.getRedeemAmount(),order.getOrderNo(),CurrencyLogDes.赎回.name());

        return order.getOrderNo();
    }

    /**
     * 结算列表
     */
    public IPage<OrderSettleRecordVO> settleOrderPage(IPage<OrderSettleRecordVO> page, Long uid, ProductType productType) {
        return orderService.OrderSettleInfoVOPage(page, uid, productType);
    }

    /**
     * 充值列表
     */
    public IPage<OrderChargeInfoVO> selectOrderChargeInfoVOPage(IPage<OrderChargeInfoVO> page, FinancialChargeQuery query) {
        return orderService.selectOrderChargeInfoVOPage(page, query);
    }

    /**
     * 充值总金额
     */
    public BigDecimal orderChargeSummaryAmount(FinancialChargeQuery query) {
        return orderService.orderChargeSummaryAmount(query);
    }

    public OrderChargeInfoVO chargeOrderDetails(Long uid, String orderNo) {

        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getUid, uid)
                .eq(Order::getOrderNo, orderNo);

        Order order = Optional.ofNullable(orderService.getOne(queryWrapper)).orElseThrow(ErrorCodeEnum.ARGUEMENT_ERROR :: generalException);
        if (!ChargeType.recharge.equals(order.getType()) && !ChargeType.withdraw.equals(order.getType())) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }

        OrderChargeInfo orderChargeInfo = orderChargeInfoService.getById(order.getRelatedId());
        log.info("get orderChargeInfo by id:{},orderNo{}", order.getRelatedId(), order.getOrderNo());
        orderChargeInfo = Optional.ofNullable(orderChargeInfo).orElseThrow(ErrorCodeEnum.ARGUEMENT_ERROR::generalException);
        if (!orderChargeInfo.getOrderNo().equals(order.getOrderNo())) {
            log.error("orderChargeInfo表 id :{} , 记录 orderNo{} 与  order 表 orderNo{} 不一致", orderChargeInfo.getId()
                    , orderChargeInfo.getOrderNo(), order.getOrderNo());
            ErrorCodeEnum.SYSTEM_ERROR.throwException();
        }
        OrderChargeInfoVO orderChargeInfoVO = chargeConverter.toVO(order);
        orderChargeInfoVO.setFromAddress(orderChargeInfo.getFromAddress());
        orderChargeInfoVO.setToAddress(orderChargeInfo.getToAddress());
        orderChargeInfoVO.setTxid(orderChargeInfo.getTxid());
        orderChargeInfoVO.setCreateTime(orderChargeInfo.getCreateTime());
        orderChargeInfoVO.setCreateTime(orderChargeInfo.getCreateTime());
        // todo 设置logo
        orderChargeInfoVO.setLogo("");
        return orderChargeInfoVO;
    }

    public OrderBaseVO orderDetails(Long uid, String orderNo) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getUid, uid)
                .eq(Order::getOrderNo, orderNo);
        Order order = Optional.ofNullable(orderService.getOne(queryWrapper)).orElseThrow(ErrorCodeEnum.ARGUEMENT_ERROR :: generalException);
        if (!ChargeType.recharge.equals(order.getType()) && !ChargeType.redeem.equals(order.getType()) && !ChargeType.transfer.equals(order.getType())){
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }

        FinancialRecord record = financialRecordService.selectById(order.getRelatedId(), uid);
        OrderBaseVO orderBaseVO = getOrderBaseVO(order, record);
        orderBaseVO.setChargeStatus(order.getStatus());
        orderBaseVO.setChargeType(order.getType());
        orderBaseVO.setOrderNo(order.getOrderNo());
        orderBaseVO.setAmount(order.getAmount());
        return orderBaseVO;
    }

    private OrderBaseVO getOrderBaseVO(Order order, FinancialRecord record) {
        switch (order.getType()){
            case recharge:
                var orderRechargeDetailsVo = chargeConverter.toOrderRechargeDetailsVo(record);
                orderRechargeDetailsVo.setPurchaseTime(record.getPurchaseTime());
                orderRechargeDetailsVo.setExpectIncome(record.getHoldAmount().multiply(record.getRate()).
                        multiply(BigDecimal.valueOf(record.getProductTerm().getDay())));
                return orderRechargeDetailsVo;
            case redeem:
                var orderRedeemDetailsVO = chargeConverter.toOrderRedeemDetailsVO(record);
                orderRedeemDetailsVO.setRedeemTime(order.getCreateTime());
                orderRedeemDetailsVO.setRedeemEndTime(order.getCreateTime());
                return orderRedeemDetailsVO;
            default:
                return chargeConverter.toOrderBaseVO(record);
        }
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
            default:
                break;
        }
        if (address == null) {
            throw ErrorCodeEnum.CURRENCY_NOT_SUPPORT.generalException();
        }
        return address;
    }

    /**
     * 理财充值记录添加
     */
    private String insertRechargeOrder(RechargeCallbackQuery query, BigDecimal amount, BigDecimal realAmount) {
        Long uid = requestInitService.get().getUid();
        // 链信息
        OrderChargeInfo orderChargeInfo = OrderChargeInfo.builder()
                .id(CommonFunction.generalId())
                .txid(query.getTxId())
                .coin(query.getType().getCurrencyCoin())
                .network(query.getType().getCurrencyNetworkType())
                .fee(amount)
                .realFee(realAmount)
                .serviceFee(BigDecimal.ZERO)
                .fromAddress(query.getFromAddress())
                .createTime(query.getCreateTime())
                .toAddress(query.getToAddress()).build();
        orderService.insert(orderChargeInfo);

        // 订单信息
        LocalDateTime now = LocalDateTime.now();
        Order order = Order.builder()
                .uid(uid)
                .orderNo(AccountChangeType.normal.getPrefix() + CommonFunction.generalSn(CommonFunction.generalId()))
                .completeTime(now)
                .amount(realAmount)
                .status(ChargeStatus.chain_success)
                .type(ChargeType.recharge)
                .coin(query.getType().getCurrencyCoin())
                .createTime(now)
                .relatedId(orderChargeInfo.getId())
                .build();

        orderService.saveOrder(order);
        return order.getOrderNo();
    }

    /**
     * 获取分页数据
     */
    public IPage<OrderChargeInfoVO> pageByChargeType(Long uid, CurrencyCoin currencyCoin, ChargeType chargeType, Page<Order> page) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getUid, uid)
                .eq(Order::getCoin, currencyCoin);
        if (Objects.nonNull(chargeType)) {
            wrapper = wrapper.eq(Order::getType, chargeType);
        }
        Page<Order> charges = this.page(page, wrapper);
        return charges.convert(chargeConverter::toVO);
    }

    /**
     * 获取主钱包地址
     */
    public String getMainWalletAddressUrl(CurrencyAdaptType currencyAdaptType) {
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
    @Resource
    private OrderService orderService;
    @Resource
    private OrderChargeInfoService orderChargeInfoService;
    @Resource
    private FinancialRecordService financialRecordService;

}
