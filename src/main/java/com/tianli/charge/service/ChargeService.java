package com.tianli.charge.service;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.account.enums.AccountChangeType;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.address.AddressService;
import com.tianli.address.mapper.Address;
import com.tianli.chain.dto.CallbackPathDTO;
import com.tianli.chain.dto.TRONTokenReq;
import com.tianli.chain.enums.ChainType;
import com.tianli.chain.service.ChainService;
import com.tianli.chain.service.WalletImputationService;
import com.tianli.chain.service.contract.ContractAdapter;
import com.tianli.chain.service.contract.ContractOperation;
import com.tianli.charge.converter.ChargeConverter;
import com.tianli.charge.entity.Order;
import com.tianli.charge.entity.OrderChargeInfo;
import com.tianli.charge.enums.ChargeGroup;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.mapper.OrderMapper;
import com.tianli.charge.query.RedeemQuery;
import com.tianli.charge.query.WithdrawQuery;
import com.tianli.charge.vo.OrderBaseVO;
import com.tianli.charge.vo.OrderChargeInfoVO;
import com.tianli.charge.vo.OrderSettleRecordVO;
import com.tianli.common.CommonFunction;
import com.tianli.common.ConfigConstants;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.common.blockchain.NetworkType;
import com.tianli.currency.enums.TokenAdapter;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.financial.convert.FinancialConverter;
import com.tianli.financial.entity.FinancialProduct;
import com.tianli.financial.entity.FinancialRecord;
import com.tianli.financial.enums.ProductType;
import com.tianli.financial.enums.RecordStatus;
import com.tianli.financial.service.FinancialProductLadderRateService;
import com.tianli.financial.service.FinancialProductService;
import com.tianli.financial.service.FinancialRecordService;
import com.tianli.management.query.FinancialChargeQuery;
import com.tianli.mconfig.ConfigService;
import com.tianli.sso.init.RequestInitService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author wangqiyun
 * @since 2020/3/31 11:25
 */

@Slf4j
@Service
public class ChargeService extends ServiceImpl<OrderMapper, Order> {

    /**
     * 充值回调:添加用户余额和记录
     *
     * @param str 充值信息
     */
    @Transactional
    public void rechargeCallback(ChainType chainType, String str) {
        var jsonArray = JSONUtil.parseObj(str).getJSONArray("token");
        var standardCurrencyArray = JSONUtil.parseObj(str).getJSONArray("standardCurrency");

        List<TRONTokenReq> tokenReqs = JSONUtil.toList(jsonArray, TRONTokenReq.class);
        List<TRONTokenReq> mainTokenReqs = JSONUtil.toList(standardCurrencyArray, TRONTokenReq.class);
        rechargeOperation(tokenReqs, chainType, false);
        rechargeOperation(mainTokenReqs, chainType, true);

    }

    private void rechargeOperation(List<TRONTokenReq> tronTokenReqs, ChainType chainType, boolean mainToken) {
        if (CollectionUtils.isEmpty(tronTokenReqs)) {
            return;
        }
        for (TRONTokenReq req : tronTokenReqs) {
            TokenAdapter tokenAdapter = mainToken ? ChainType.getTokenAdapter(chainType) : TokenAdapter.getToken(req.getContractAddress());
            Address address = getAddress(tokenAdapter.getNetwork(), req.getTo());
            Long uid = address.getUid();
            BigDecimal finalAmount = tokenAdapter.alignment(req.getValue());

            if (orderService.getOrderChargeByTxid(req.getHash()) != null) {
                log.error("txid {} 已经存在充值订单", req.getHash());
                ErrorCodeEnum.TRADE_FAIL.throwException();
            }
            // 生成订单数据
            String orderNo = insertRechargeOrder(uid, req, tokenAdapter, finalAmount, req.getValue());

            // 操作余额信息
            accountBalanceService.increase(uid, ChargeType.recharge, tokenAdapter.getCurrencyCoin()
                    , tokenAdapter.getNetwork(), finalAmount, orderNo, CurrencyLogDes.充值.name());
            // 操作归集信息
            walletImputationService.insert(uid, address, tokenAdapter, req, finalAmount);

            // 判断是否有预先订单需要处理
            try {
                orderAdvanceService.handlerRechargeEvent(uid, req, finalAmount, tokenAdapter);
            } catch (Exception e) {
                log.error("申购订单失败");
                log.error(ExceptionUtil.getMessage(e));
            }
        }

    }

    /**
     * 提现回调
     *
     * @param str 提现信息
     */
    @Transactional
    public void withdrawCallback(ChainType chainType, String str) {
        var jsonArray = JSONUtil.parseObj(str).getJSONArray("token");
        var standardCurrencyArray = JSONUtil.parseObj(str).getJSONArray("standardCurrency");

        List<TRONTokenReq> tokenReqs = JSONUtil.toList(jsonArray, TRONTokenReq.class);
        List<TRONTokenReq> mainTokenReqs = JSONUtil.toList(standardCurrencyArray, TRONTokenReq.class);
        withdrawOperation(tokenReqs, chainType, false);
        withdrawOperation(mainTokenReqs, chainType, true);
    }

    private void withdrawOperation(List<TRONTokenReq> tronTokenReqs, ChainType chainType, boolean mainToken) {
        if (CollectionUtils.isEmpty(tronTokenReqs)) {
            return;
        }
        for (TRONTokenReq req : tronTokenReqs) {
            TokenAdapter tokenAdapter = mainToken ? ChainType.getTokenAdapter(chainType) : TokenAdapter.getToken(req.getContractAddress());
            OrderChargeInfo orderChargeInfo = orderChargeInfoService.getByTxid(req.getHash());
            Long uid = orderChargeInfo.getUid();
            Order order = orderService.getOrderByHash(req.getHash());

            // 操作余额信息
            accountBalanceService.reduce(uid, ChargeType.withdraw, tokenAdapter.getCurrencyCoin()
                    , tokenAdapter.getNetwork(), orderChargeInfo.getFee(), order.getOrderNo(), "提现成功扣除");
            order.setStatus(ChargeStatus.chain_success);

            order.setCompleteTime(LocalDateTime.now());
            orderService.saveOrUpdate(order);
        }
    }

    /**
     * 提现申请
     */
    @Transactional
    public void withdrawApply(Long uid, WithdrawQuery query) {
        TokenAdapter tokenAdapter = TokenAdapter.get(query.getCoin(), query.getNetwork());

        boolean validAddress = contractAdapter.getOne(tokenAdapter.getNetwork()).isValidAddress(query.getTo());
        if (!validAddress) {
            ErrorCodeEnum.throwException("地址校验失败");
        }
        // 计算手续费  实际手续费 = 提现数额 * 手续费率 + 固定手续费数额
        // 最小提现金额
        String withdrawMinAmount = configService.get(tokenAdapter.name() + "_withdraw_min_amount");
        // 手续费率
        String rate = configService.get(tokenAdapter.name() + "_withdraw_rate");
        // 固定手续费数额
        String fixedAmount = configService.get(tokenAdapter.name() + "_withdraw_fixed_amount");

        // 提现数额
        BigDecimal withdrawAmount = BigDecimal.valueOf(query.getAmount());
        if (BigDecimal.valueOf(query.getAmount()).compareTo(new BigDecimal(withdrawMinAmount)) < 0)
            ErrorCodeEnum.throwException("提现数额过小");

        // 手续费
        BigDecimal serviceAmount = (withdrawAmount.multiply(new BigDecimal(StringUtils.isNotBlank(rate) ? rate : "0")))
                .add(new BigDecimal(StringUtils.isNotBlank(fixedAmount) ? fixedAmount : "0"));
        BigDecimal realWithdrawAmount = withdrawAmount.subtract(serviceAmount);

        if (serviceAmount.compareTo(BigDecimal.ZERO) < 0) ErrorCodeEnum.FEE_LT_ZERO_ERROR.throwException();
        if (withdrawAmount.compareTo(serviceAmount) <= 0)
            ErrorCodeEnum.WITHDRAWAL_AMOUNT_LT_FEE_ERROR.throwException();

        LocalDateTime now = requestInitService.now();

        String fromAddress = getMainWalletAddressUrl(tokenAdapter);

        // 链信息
        OrderChargeInfo orderChargeInfo = OrderChargeInfo.builder()
                .id(CommonFunction.generalId())
                .txid(null)
                .uid(uid)
                .coin(tokenAdapter.getCurrencyCoin())
                .network(tokenAdapter.getNetwork())
                .fee(withdrawAmount) // 用户提币金额
                .serviceFee(serviceAmount) // 手续费金额
                .realFee(tokenAdapter.restore(realWithdrawAmount)) // 真的需要转账的金额
                .minerFee(BigDecimal.ZERO)
                .fromAddress(fromAddress) // 系统热钱包
                .toAddress(query.getTo()) // 用户提现地址
                .createTime(now)
                .build();
        orderService.insert(orderChargeInfo);

        //创建提现订单(提币申请)
        long id = CommonFunction.generalId();
        Order order = new Order();
        order.setUid(uid);
        order.setAmount(BigDecimal.valueOf(query.getAmount()));
        order.setServiceAmount(serviceAmount);
        order.setOrderNo(AccountChangeType.withdraw.getPrefix() + CommonFunction.generalSn(id));
        order.setStatus(ChargeStatus.created);
        order.setType(ChargeType.withdraw);
        order.setCoin(tokenAdapter.getCurrencyCoin());
        order.setCreateTime(now);
        order.setRelatedId(orderChargeInfo.getId());
        orderService.save(order);

        //冻结提现数额
        accountBalanceService.freeze(uid, ChargeType.withdraw, tokenAdapter.getCurrencyCoin()
                , tokenAdapter.getNetwork(), withdrawAmount, order.getOrderNo(), CurrencyLogDes.提现.name());
    }

    /**
     * 提现上链
     */
    @Transactional
    public void withdrawChain(Order order) {
        if (Objects.isNull(order)) {
            throw ErrorCodeEnum.ARGUEMENT_ERROR.generalException("订单为null");
        }
        if (!ChargeType.withdraw.equals(order.getType())) {
            throw ErrorCodeEnum.ARGUEMENT_ERROR.generalException("仅有提现订单能操作");
        }
        if (!ChargeStatus.created.equals(order.getStatus())) {
            throw ErrorCodeEnum.ARGUEMENT_ERROR.generalException("当前提现订单状态异常");
        }
        Long relatedId = order.getRelatedId();
        OrderChargeInfo orderChargeInfo = orderChargeInfoService.getById(relatedId);
        if (Objects.nonNull(orderChargeInfo.getTxid())) {
            throw ErrorCodeEnum.ARGUEMENT_ERROR.generalException(String.format(
                    "当前订单：[%s]已经在：[%s] 网络存在交易hash：[%s]", order.getOrderNo(), orderChargeInfo.getNetwork(), orderChargeInfo.getTxid()));
        }

        ContractOperation contractService = contractAdapter.getOne(orderChargeInfo.getNetwork());
        TokenAdapter tokenAdapter = TokenAdapter.get(orderChargeInfo.getCoin(), orderChargeInfo.getNetwork());
        BigInteger amount = tokenAdapter.restore(order.getAmount().subtract(order.getServiceAmount()));
        Result result = null;

        /* 注册监听回调接口
         * {@link com.tianli.charge.controller.ChargeController#withdrawCallback(ChainType, String, String, String)}
         */
        chainService.pushWithdrawCondition(orderChargeInfo.getNetwork(), orderChargeInfo.getCoin()
                , new CallbackPathDTO("/api/charge/withdraw"), orderChargeInfo.getToAddress());

        try {
            result = contractService.transfer(orderChargeInfo.getToAddress(), amount, tokenAdapter);
        } catch (Exception e) {
            log.info("上链失败");
            e.printStackTrace();
            ErrorCodeEnum.throwException("上链失败");
        }
        if (Objects.isNull(result) || Objects.isNull(result.getData())) {
            ErrorCodeEnum.throwException("上链失败");
        }


        String txid = (String) result.getData();
        orderChargeInfo.setTxid(txid);
        orderChargeInfoService.updateById(orderChargeInfo);
    }

    @Transactional
    public String redeem(Long uid, RedeemQuery query) {
        // todo 计算利息的时候不允许进行赎回操
        Long recordId = query.getRecordId();
        FinancialRecord record = financialRecordService.selectById(recordId, uid);

        if (RecordStatus.SUCCESS.equals(record.getStatus())) {
            log.info("recordId:{},已经处于完成状态，请校验是否有误", recordId);
            ErrorCodeEnum.TRADE_FAIL.throwException();
        }

        if (query.getRedeemAmount().compareTo(record.getHoldAmount()) > 0) {
            log.info("赎回金额 {}  大于持有金额 {}", query.getRedeemAmount(), record.getHoldAmount());
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }

        //创建赎回订单  没有审核操作，在一个事物里无需操作
        LocalDateTime now = LocalDateTime.now();
        long id = CommonFunction.generalId();
        Order order = new Order();
        order.setId(id);
        order.setUid(uid);
        order.setAmount(query.getRedeemAmount());
        order.setOrderNo(AccountChangeType.redeem.getPrefix() + CommonFunction.generalSn(id));
        order.setStatus(ChargeStatus.chain_success);
        order.setType(ChargeType.redeem);
        order.setRelatedId(recordId);
        order.setCoin(record.getCoin());
        order.setCreateTime(now);
        order.setCompleteTime(now);
        orderService.save(order);

        // 增加
        accountBalanceService.increase(uid, ChargeType.redeem, record.getCoin(), query.getRedeemAmount(), order.getOrderNo(), CurrencyLogDes.赎回.name());

        // 减少产品持有
        financialRecordService.redeem(record.getId(), query.getRedeemAmount(), record.getHoldAmount());

        // 更新记录状态
        FinancialRecord recordLatest = financialRecordService.selectById(recordId, uid);
        if (recordLatest.getHoldAmount().compareTo(BigDecimal.ZERO) == 0) {
            recordLatest.setStatus(RecordStatus.SUCCESS);
            recordLatest.setUpdateTime(LocalDateTime.now());
        }
        financialRecordService.updateById(recordLatest);

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
    public BigDecimal orderAmountSum(FinancialChargeQuery query) {
        return orderService.orderAmountDollarSum(query);
    }

    public OrderChargeInfoVO chargeOrderDetails(Long uid, String orderNo) {

        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getUid, uid)
                .eq(Order::getOrderNo, orderNo);

        Order order = Optional.ofNullable(orderService.getOne(queryWrapper)).orElseThrow(ErrorCodeEnum.ARGUEMENT_ERROR::generalException);
        if (!ChargeType.recharge.equals(order.getType()) && !ChargeType.withdraw.equals(order.getType())) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }

        OrderChargeInfo orderChargeInfo = orderChargeInfoService.getById(order.getRelatedId());
        log.info("get orderChargeInfo by id:{},orderNo{}", order.getRelatedId(), order.getOrderNo());
        orderChargeInfo = Optional.ofNullable(orderChargeInfo).orElseThrow(ErrorCodeEnum.ARGUEMENT_ERROR::generalException);

        OrderChargeInfoVO orderChargeInfoVO = chargeConverter.toVO(order);
        orderChargeInfoVO.setFromAddress(orderChargeInfo.getFromAddress());
        orderChargeInfoVO.setToAddress(orderChargeInfo.getToAddress());
        orderChargeInfoVO.setTxid(orderChargeInfo.getTxid());
        orderChargeInfoVO.setCreateTime(orderChargeInfo.getCreateTime());
        orderChargeInfoVO.setLogo(order.getCoin().getLogoPath());
        orderChargeInfoVO.setNetworkType(orderChargeInfo.getNetwork());
        orderChargeInfoVO.setRealAmount(order.getAmount().subtract(order.getServiceAmount()));
        return orderChargeInfoVO;
    }

    public OrderBaseVO orderDetails(Long uid, String orderNo) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getUid, uid)
                .eq(Order::getOrderNo, orderNo);
        Order order = Optional.ofNullable(orderService.getOne(queryWrapper)).orElseThrow(ErrorCodeEnum.ARGUEMENT_ERROR::generalException);
        if (!ChargeType.purchase.equals(order.getType()) && !ChargeType.redeem.equals(order.getType()) && !ChargeType.transfer.equals(order.getType())) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }

        FinancialRecord record = financialRecordService.selectById(order.getRelatedId(), uid);
        OrderBaseVO orderBaseVO = getOrderBaseVO(order, record);
        orderBaseVO.setChargeStatus(order.getStatus());
        orderBaseVO.setChargeType(order.getType());
        orderBaseVO.setOrderNo(order.getOrderNo());
        orderBaseVO.setAmount(order.getAmount());
        orderBaseVO.setProductId(record.getProductId());
        return orderBaseVO;
    }

    private OrderBaseVO getOrderBaseVO(Order order, FinancialRecord record) {
        FinancialProduct product = financialProductService.getById(record.getProductId());
        switch (order.getType()) {
            case purchase:
            case transfer:
                var orderRechargeDetailsVo = chargeConverter.toOrderRechargeDetailsVo(record);
                orderRechargeDetailsVo.setPurchaseTime(record.getPurchaseTime());
                orderRechargeDetailsVo.setExpectIncome(record.getHoldAmount().multiply(record.getRate())
                        .multiply(BigDecimal.valueOf(record.getProductTerm().getDay()))
                        .divide(BigDecimal.valueOf(365), 8, RoundingMode.DOWN));
                orderRechargeDetailsVo.setRateType(product.getRateType());
                orderRechargeDetailsVo.setMaxRate(product.getMaxRate());
                orderRechargeDetailsVo.setMinRate(product.getMinRate());
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
    private Address getAddress(NetworkType network, String addressStr) {
        Address address = null;
        switch (network) {
            case erc20:
                address = addressService.getByEth(addressStr);
                break;
            case bep20:
                address = addressService.getByBsc(addressStr);
                break;
            case trc20:
                address = addressService.getByTron(addressStr);
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
    @Transactional
    public String insertRechargeOrder(Long uid, TRONTokenReq query, TokenAdapter tokenAdapter
            , BigDecimal amount, BigDecimal realAmount) {
        // 链信息
        OrderChargeInfo orderChargeInfo = OrderChargeInfo.builder()
                .id(CommonFunction.generalId())
                .txid(query.getHash())
                .uid(uid)
                .coin(tokenAdapter.getCurrencyCoin())
                .network(tokenAdapter.getNetwork())
                // 格式化后的费用
                .fee(amount)
                // 交易真实的费用
                .realFee(realAmount.toBigInteger())
                // 手续费
                .serviceFee(BigDecimal.ZERO)
                .fromAddress(query.getFrom())
                .createTime(query.getCreateTime())
                .toAddress(query.getTo()).build();
        orderService.insert(orderChargeInfo);

        LocalDateTime now = LocalDateTime.now();
        // 订单信息
        Order order = Order.builder()
                .uid(uid)
                .orderNo(AccountChangeType.recharge.getPrefix() + CommonFunction.generalSn(CommonFunction.generalId()))
                .completeTime(now)
                .amount(amount)
                .status(ChargeStatus.chain_success)
                .type(ChargeType.recharge)
                .coin(tokenAdapter.getCurrencyCoin())
                .createTime(now)
                .relatedId(orderChargeInfo.getId())
                .build();

        orderService.save(order);
        return order.getOrderNo();
    }

    /**
     * 获取分页数据
     */
    public IPage<OrderChargeInfoVO> pageByChargeGroup(Long uid, CurrencyCoin currencyCoin, ChargeGroup chargeGroup, Page<Order> page) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getUid, uid)
                .eq(Order::getCoin, currencyCoin)
                .orderByDesc(Order::getCreateTime)
                .eq(false, Order::getStatus, ChargeStatus.chain_fail);
        if (Objects.nonNull(chargeGroup)) {
            wrapper = wrapper.in(Order::getType, chargeGroup.getChargeTypes());
        }
        Page<Order> charges = this.page(page, wrapper);
        return charges.convert(chargeConverter::toVO);
    }

    /**
     * 获取主钱包地址
     */
    public String getMainWalletAddressUrl(TokenAdapter tokenAdapter) {
        String fromAddress = null;
        switch (tokenAdapter.getNetwork()) {
            case trc20:
                fromAddress = configService.get(ConfigConstants.TRON_MAIN_WALLET_ADDRESS);
                break;
            case erc20:
                fromAddress = configService.get(ConfigConstants.ETH_MAIN_WALLET_ADDRESS);
                break;
            case bep20:
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
    @Resource
    private FinancialProductService financialProductService;
    @Resource
    private WalletImputationService walletImputationService;
    @Resource
    private ContractAdapter contractAdapter;
    @Resource
    private ChainService chainService;
    @Resource
    private OrderAdvanceService orderAdvanceService;
    @Resource
    private FinancialProductLadderRateService financialProductLadderRateService;
    @Resource
    private FinancialConverter financialConverter;

}
