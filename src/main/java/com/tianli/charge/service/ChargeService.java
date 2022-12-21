package com.tianli.charge.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.account.enums.AccountChangeType;
import com.tianli.account.query.AccountDetailsQuery;
import com.tianli.account.service.impl.AccountBalanceServiceImpl;
import com.tianli.account.vo.TransactionGroupTypeVO;
import com.tianli.account.vo.TransactionTypeVO;
import com.tianli.address.AddressService;
import com.tianli.address.mapper.Address;
import com.tianli.chain.dto.CallbackPathDTO;
import com.tianli.chain.dto.TRONTokenReq;
import com.tianli.chain.entity.Coin;
import com.tianli.chain.entity.CoinBase;
import com.tianli.chain.enums.ChainType;
import com.tianli.chain.service.ChainService;
import com.tianli.chain.service.CoinBaseService;
import com.tianli.chain.service.CoinService;
import com.tianli.chain.service.WalletImputationService;
import com.tianli.chain.service.contract.ContractAdapter;
import com.tianli.chain.service.contract.ContractOperation;
import com.tianli.charge.converter.ChargeConverter;
import com.tianli.charge.entity.Order;
import com.tianli.charge.entity.OrderChargeInfo;
import com.tianli.charge.enums.*;
import com.tianli.charge.mapper.OrderMapper;
import com.tianli.charge.query.OrderReviewQuery;
import com.tianli.charge.query.RedeemQuery;
import com.tianli.charge.query.WithdrawQuery;
import com.tianli.charge.vo.OrderBaseVO;
import com.tianli.charge.vo.OrderChargeInfoVO;
import com.tianli.charge.vo.OrderRechargeDetailsVo;
import com.tianli.charge.vo.OrderSettleRecordVO;
import com.tianli.common.CommonFunction;
import com.tianli.common.ConfigConstants;
import com.tianli.common.RedisConstants;
import com.tianli.common.RedisService;
import com.tianli.common.async.AsyncService;
import com.tianli.common.blockchain.NetworkType;
import com.tianli.common.webhook.WebHookService;
import com.tianli.common.webhook.WebHookTemplate;
import com.tianli.common.webhook.WebHookToken;
import com.tianli.currency.enums.TokenAdapter;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.financial.entity.FinancialProduct;
import com.tianli.financial.entity.FinancialRecord;
import com.tianli.financial.enums.ProductType;
import com.tianli.financial.enums.RecordStatus;
import com.tianli.financial.service.FinancialProductService;
import com.tianli.financial.service.FinancialRecordService;
import com.tianli.financial.vo.ExpectIncomeVO;
import com.tianli.management.query.FinancialChargeQuery;
import com.tianli.management.service.IWalletAgentService;
import com.tianli.mconfig.ConfigService;
import com.tianli.openapi.service.OrderRewardRecordService;
import com.tianli.sso.init.RequestInitService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author wangqiyun
 * @since 2020/3/31 11:25
 */

@Slf4j
@Service
public class ChargeService extends ServiceImpl<OrderMapper, Order> {

    /**
     * 预加载数据
     */
    @PostConstruct
    private List<TransactionGroupTypeVO> preloading() {
        List<TransactionGroupTypeVO> list = new ArrayList<>(2);
        for (ChargeGroup chargeGroup : ChargeGroup.values()) {
            TransactionGroupTypeVO transactionGroupTypeVO = new TransactionGroupTypeVO();
            transactionGroupTypeVO.setGroup(chargeGroup);

            List<TransactionTypeVO> transactionTypeVOS = chargeGroup.getChargeTypes().stream().map(chargeType -> {
                TransactionTypeVO transactionTypeVO = new TransactionTypeVO();
                transactionTypeVO.setType(chargeType);
                transactionTypeVO.setName(chargeType.getNameZn());
                transactionTypeVO.setNameEn(chargeType.getNameEn());
                return transactionTypeVO;
            }).collect(Collectors.toList());
            transactionGroupTypeVO.setTypes(transactionTypeVOS);
            list.add(transactionGroupTypeVO);
        }

        redisService.set(RedisConstants.ACCOUNT_TRANSACTION_TYPE, list, 10L, TimeUnit.DAYS);
        return list;
    }

    @SuppressWarnings("unchecked")
    public List<TransactionGroupTypeVO> listTransactionGroupType(Long uid) {
        List<TransactionGroupTypeVO> result;
        Object o = redisService.get(RedisConstants.ACCOUNT_TRANSACTION_TYPE);
        if (Objects.nonNull(o)) {
            result = (List<TransactionGroupTypeVO>) o;
        } else {
            result = preloading();
        }

        boolean agent = walletAgentService.isAgent(uid);
        if (agent) {
            return result;
        }
        List<ChargeType> filterType =
                List.of(ChargeType.agent_fund_sale, ChargeType.agent_fund_interest, ChargeType.agent_fund_redeem);

        result.forEach(group -> {
            List<TransactionTypeVO> types = group.getTypes();
            List<TransactionTypeVO> newTypes = types.stream()
                    .filter(type -> !filterType.contains(type.getType()))
                    .collect(Collectors.toList());
            group.setTypes(newTypes);
        });

        return result;
    }


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
            Coin coin = mainToken ? coinService.mainToken(chainType.getMainToken())
                    : coinService.getByContract(req.getContractAddress());
            Address address = getAddress(coin.getNetwork(), req.getTo());
            Long uid = address.getUid();
            BigDecimal finalAmount = TokenAdapter.alignment(req.getValue(), coin.getDecimals());

            if (orderChargeInfoService.getOrderChargeByTxid(uid, req.getHash()) != null) {
                log.error("txid {} 已经存在充值订单", req.getHash());
                ErrorCodeEnum.TRADE_FAIL.throwException();
            }
            // 生成订单数据
            String orderNo = insertRechargeOrder(uid, req, coin, finalAmount, req.getValue());

            // 操作余额信息
            accountBalanceServiceImpl.increase(uid, ChargeType.recharge, coin.getName()
                    , coin.getNetwork(), finalAmount, orderNo, CurrencyLogDes.充值.name());
            // 操作归集信息
            walletImputationService.insert(uid, address, coin, req, finalAmount);

            asyncService.async(() -> orderAdvanceService.handlerRechargeEvent(uid, req, finalAmount, coin));

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
            String to = req.getTo();
            Address address = addressService.getByChain(chainType, to);
            // 存在提现的地址是云钱包的情况
            OrderChargeInfo orderChargeInfo = Objects.isNull(address) ? orderChargeInfoService.getByTxid(req.getHash())
                    : orderChargeInfoService.getByTxidExcludeUid(address.getUid(), req.getHash());

            if (Objects.isNull(orderChargeInfo)) {
                return;
            }

            Order order = Objects.isNull(address) ? orderService.getOrderByHash(req.getHash(), ChargeType.withdraw)
                    : orderService.getOrderByHashExcludeUid(address.getUid(), req.getHash(), ChargeType.withdraw);
            orderReviewService.withdrawSuccess(order, orderChargeInfo);
        }
    }

    /**
     * 提现申请
     */
    @Transactional
    public void withdrawApply(Long uid, WithdrawQuery query) {
        Coin coin = coinService.getByNameAndNetwork(query.getCoin(), query.getNetwork());

        Address address = addressService.get(uid);

        if (NetworkType.trc20.equals(query.getNetwork()) && address.getTron().equals(query.getTo())) {
            ErrorCodeEnum.FINANCIAL_TO_ERROR.throwException();
        }

        if ((NetworkType.bep20.equals(query.getNetwork()) || NetworkType.erc20.equals(query.getNetwork()))
                && address.getEth().equals(query.getTo())) {
            ErrorCodeEnum.FINANCIAL_TO_ERROR.throwException();
        }

        boolean validAddress = contractAdapter.getOne(coin.getNetwork()).isValidAddress(query.getTo());
        if (!validAddress) {
            ErrorCodeEnum.throwException("地址校验失败");
        }


        // 计算手续费  实际手续费 = 提现数额 * 手续费率 + 固定手续费数额
        // 最小提现金额
        BigDecimal withdrawMinAmount = coin.getWithdrawMin();
        // 手续费率
        String rate = "0";
        // 固定手续费数额
        BigDecimal fixedAmount = coin.getWithdrawFixedAmount();

        // 提现数额
        BigDecimal withdrawAmount = BigDecimal.valueOf(query.getAmount());
        if (BigDecimal.valueOf(query.getAmount()).compareTo(withdrawMinAmount) < 0)
            ErrorCodeEnum.throwException("提现数额过小");

        // 手续费
        BigDecimal serviceAmount = (withdrawAmount.multiply(new BigDecimal(StringUtils.isNotBlank(rate) ? rate : "0")))
                .add(fixedAmount);
        BigDecimal realWithdrawAmount = withdrawAmount.subtract(serviceAmount);

        if (serviceAmount.compareTo(BigDecimal.ZERO) < 0) ErrorCodeEnum.FEE_LT_ZERO_ERROR.throwException();
        if (withdrawAmount.compareTo(serviceAmount) <= 0)
            ErrorCodeEnum.WITHDRAWAL_AMOUNT_LT_FEE_ERROR.throwException();

        LocalDateTime now = requestInitService.now();

        String fromAddress = getMainWalletAddressUrl(coin.getNetwork());

        // 链信息
        OrderChargeInfo orderChargeInfo = OrderChargeInfo.builder()
                .id(CommonFunction.generalId())
                .txid(null)
                .uid(uid)
                .coin(coin.getName())
                .network(coin.getNetwork())
                .fee(withdrawAmount) // 用户提币金额
                .serviceFee(serviceAmount) // 手续费金额
                .realFee(TokenAdapter.restoreBigInteger(realWithdrawAmount, coin.getDecimals())) // 真的需要转账的金额
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
        order.setCoin(coin.getName());
        order.setCreateTime(now);
        order.setRelatedId(orderChargeInfo.getId());
        orderService.save(order);

        //冻结提现数额
        accountBalanceServiceImpl.freeze(uid, ChargeType.withdraw, coin.getName(), coin.getNetwork()
                , withdrawAmount, order.getOrderNo(), CurrencyLogDes.提现.name());

        OrderReviewStrategy strategy = withdrawReviewStrategy.getStrategy(order, orderChargeInfo, true);
        log.info("当前提现策略是 ： " + strategy.name());
        if (!OrderReviewStrategy.AUTO_REVIEW_AUTO_TRANSFER.equals(strategy)) {
            String msg = WebHookTemplate.withdrawApply(query.getAmount(), query.getCoin());
            webHookService.dingTalkSend(msg, WebHookToken.FINANCIAL_PRODUCT);
        }

        // 自动打币
        if (OrderReviewStrategy.AUTO_REVIEW_AUTO_TRANSFER.equals(strategy)) {
            OrderReviewQuery reviewQuery = OrderReviewQuery.builder()
                    .orderNo(order.getOrderNo())
                    .remarks("自动审核通过")
                    .rid(0L)
                    .reviewBy("系统自动")
                    .pass(true).build();
            orderReviewService.review(reviewQuery);
        }

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
        Coin coin = coinService.getByNameAndNetwork(orderChargeInfo.getCoin(), orderChargeInfo.getNetwork());
        BigInteger amount = TokenAdapter.restoreBigInteger(order.getAmount().subtract(order.getServiceAmount()), coin.getDecimals());
        Result result = null;

        /* 注册监听回调接口
         * {@link com.tianli.charge.controller.ChargeController#withdrawCallback(ChainType, String, String, String)}
         */
        chainService.pushWithdrawCondition(orderChargeInfo.getNetwork(), orderChargeInfo.getCoin()
                , new CallbackPathDTO("/api/charge/withdraw"), orderChargeInfo.getToAddress());

        try {
            result = contractService.transfer(orderChargeInfo.getToAddress(), amount, coin);
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
        accountBalanceServiceImpl.increase(uid, ChargeType.redeem, record.getCoin(), query.getRedeemAmount(), order.getOrderNo(), CurrencyLogDes.赎回.name());

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

    public OrderChargeInfoVO chargeOrderDetails(Long orderId) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getId, orderId);
        Order order = orderService.getOne(queryWrapper);

        return getOrderChargeInfoVO(order);
    }

    public OrderChargeInfoVO chargeOrderDetails(Long uid, String orderNo) {

        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getUid, uid)
                .eq(Order::getOrderNo, orderNo);
        Order order = orderService.getOne(queryWrapper);

        return getOrderChargeInfoVO(order);
    }

    private OrderChargeInfoVO getOrderChargeInfoVO(Order order) {
        order = Optional.ofNullable(order).orElseThrow(ErrorCodeEnum.ARGUEMENT_ERROR::generalException);
        if (!ChargeType.recharge.equals(order.getType()) && !ChargeType.withdraw.equals(order.getType())) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }
        CoinBase coinBase = coinBaseService.getByName(order.getCoin());

        OrderChargeInfo orderChargeInfo = orderChargeInfoService.getById(order.getRelatedId());
        log.info("get orderChargeInfo by id:{},orderNo{}", order.getRelatedId(), order.getOrderNo());
        orderChargeInfo = Optional.ofNullable(orderChargeInfo).orElse(new OrderChargeInfo());

        OrderChargeInfoVO orderChargeInfoVO = chargeConverter.toVO(order);
        orderChargeInfoVO.setFromAddress(orderChargeInfo.getFromAddress());
        orderChargeInfoVO.setToAddress(orderChargeInfo.getToAddress());
        orderChargeInfoVO.setTxid(orderChargeInfo.getTxid());
        orderChargeInfoVO.setCreateTime(orderChargeInfo.getCreateTime());
        orderChargeInfoVO.setLogo(coinBase.getLogo());
        orderChargeInfoVO.setNetworkType(orderChargeInfo.getNetwork());
        orderChargeInfoVO.setRealAmount(order.getAmount().subtract(order.getServiceAmount()));
        return orderChargeInfoVO;
    }

    public OrderBaseVO orderDetails(Long uid, String orderNo) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getUid, uid)
                .eq(Order::getOrderNo, orderNo);
        Order order = Optional.ofNullable(orderService.getOne(queryWrapper)).orElseThrow(ErrorCodeEnum.ARGUEMENT_ERROR::generalException);
        if (!ChargeType.purchase.equals(order.getType()) && !ChargeType.redeem.equals(order.getType())
                && !ChargeType.transfer.equals(order.getType())) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }

        FinancialRecord record = financialRecordService.selectById(order.getRelatedId(), uid);
        OrderBaseVO orderBaseVO = getOrderBaseVO(order, record);
        orderBaseVO.setChargeStatus(order.getStatus());
        orderBaseVO.setChargeType(order.getType());
        orderBaseVO.setOrderNo(order.getOrderNo());
        orderBaseVO.setAmount(order.getAmount());
        orderBaseVO.setProductId(record.getProductId());

        // 对于活期记录来说，因为持有是累加的，导致持有记录表中的申购时间是不对的，需要取订单表
        if (orderBaseVO instanceof OrderRechargeDetailsVo) {
            OrderRechargeDetailsVo orderBase = (OrderRechargeDetailsVo) orderBaseVO;
            orderBase.setPurchaseTime(order.getCreateTime());
        }
        return orderBaseVO;
    }

    private OrderBaseVO getOrderBaseVO(Order order, FinancialRecord record) {
        FinancialProduct product = financialProductService.getById(record.getProductId());
        switch (order.getType()) {
            case purchase:
            case transfer:
                var orderRechargeDetailsVo = chargeConverter.toOrderRechargeDetailsVo(record);
                orderRechargeDetailsVo.setPurchaseTime(record.getPurchaseTime());
                ExpectIncomeVO expectIncomeVO = financialProductService.expectIncome(record.getProductId(), order.getAmount());
                orderRechargeDetailsVo.setExpectIncome(expectIncomeVO.getExpectIncome());
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
    public String insertRechargeOrder(Long uid, TRONTokenReq query, Coin coin
            , BigDecimal amount, BigDecimal realAmount) {
        // 链信息
        OrderChargeInfo orderChargeInfo = OrderChargeInfo.builder()
                .id(CommonFunction.generalId())
                .txid(query.getHash())
                .uid(uid)
                .coin(coin.getName())
                .network(coin.getNetwork())
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
                .coin(coin.getName())
                .createTime(now)
                .relatedId(orderChargeInfo.getId())
                .build();

        orderService.save(order);
        return order.getOrderNo();
    }

    /**
     * 获取分页数据
     */
    public IPage<OrderChargeInfoVO> pageByChargeGroup(Long uid, AccountDetailsQuery query, Page<Order> page) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getUid, uid)
                .orderByDesc(Order::getCreateTime)
                .orderByDesc(Order::getId)
                .eq(false, Order::getStatus, ChargeStatus.chain_fail);

        Set<ChargeType> types = new HashSet<>();

        if (Objects.nonNull(query.getChargeGroup())) {
            types.addAll(query.getChargeGroup().getChargeTypes());
        }

        if (CollectionUtils.isNotEmpty(query.getChargeGroups())) {
            query.getChargeGroups().forEach(group -> types.addAll(group.getChargeTypes()));
        }

        if (CollectionUtils.isNotEmpty(query.getChargeTypes())) {
            types.addAll(query.getChargeTypes());
        }

        if (CollectionUtils.isNotEmpty(types)) {
            wrapper = wrapper.in(Order::getType, types);
        }

        if (Objects.nonNull(query.getChargeType())) {
            wrapper = wrapper.eq(Order::getType, query.getChargeType());
        }

        if (StringUtils.isNotBlank(query.getCoin())) {
            wrapper = wrapper.eq(Order::getCoin, query.getCoin());
        }

        if (Objects.nonNull(query.getStartTime()) && Objects.nonNull(query.getEndTime())) {
            wrapper.between(Order::getCreateTime, query.getStartTime(), query.getEndTime());
        }
        if (Objects.nonNull(query.getStartTime()) && Objects.isNull(query.getEndTime())) {
            wrapper.gt(Order::getCreateTime, query.getStartTime());
        }
        if (Objects.isNull(query.getStartTime()) && Objects.nonNull(query.getEndTime())) {
            wrapper.lt(Order::getCreateTime, query.getEndTime());
        }

        Page<Order> charges = this.page(page, wrapper);

        return charges.convert(charge -> {
            OrderChargeInfoVO orderChargeInfoVO = chargeConverter.toVO(charge);
            if (ChargeType.transaction_reward.equals(orderChargeInfoVO.getType())) {
                int i = orderRewardRecordService.recordCountThisHour(charge.getUid(), charge.getCreateTime());
                orderChargeInfoVO.setRemarks("已发放" + i + "笔");
                orderChargeInfoVO.setRemarksEn(i + " Receiving Record(s)");
            }

            if (!ChargeType.transaction_reward.equals(orderChargeInfoVO.getType())) {
                ChargeRemarks remarks = ChargeRemarks.getInstance(charge.getType(), charge.getStatus());
                orderChargeInfoVO.setRemarks(remarks.getRemarks());
                orderChargeInfoVO.setRemarksEn(remarks.getRemarksEn());
            }
            return orderChargeInfoVO;
        });
    }


    /**
     * 获取主钱包地址
     */
    public String getMainWalletAddressUrl(NetworkType networkType) {
        String fromAddress = null;
        switch (networkType) {
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
    private AccountBalanceServiceImpl accountBalanceServiceImpl;
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
    private AsyncService asyncService;
    @Resource
    private OrderReviewService orderReviewService;
    @Resource
    private WebHookService webHookService;
    @Resource
    private RedisService redisService;
    @Resource
    private IWalletAgentService walletAgentService;
    @Resource
    private OrderRewardRecordService orderRewardRecordService;
    @Resource
    private CoinBaseService coinBaseService;
    @Resource
    private CoinService coinService;
    @Resource
    private WithdrawReviewStrategy withdrawReviewStrategy;
}
