package com.tianli.charge.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.MoreObjects;
import com.tianli.account.entity.AccountBalanceOperationLog;
import com.tianli.account.entity.AccountUserTransfer;
import com.tianli.account.enums.*;
import com.tianli.account.mapper.AccountBalanceOperationLogMapper;
import com.tianli.account.query.AccountDetailsNewQuery;
import com.tianli.account.query.AccountDetailsQuery;
import com.tianli.account.service.AccountBalanceOperationLogService;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.account.service.AccountUserTransferService;
import com.tianli.account.vo.AccountBalanceOperationLogVo;
import com.tianli.account.vo.OrderChargeTypeVO;
import com.tianli.account.vo.TransactionGroupTypeVO;
import com.tianli.account.vo.TransactionTypeVO;
import com.tianli.address.mapper.Address;
import com.tianli.address.mapper.OccasionalAddress;
import com.tianli.address.service.AddressService;
import com.tianli.address.service.OccasionalAddressService;
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
import com.tianli.charge.entity.OrderChargeType;
import com.tianli.charge.enums.*;
import com.tianli.charge.mapper.OrderMapper;
import com.tianli.charge.query.OrderReviewQuery;
import com.tianli.charge.query.WithdrawQuery;
import com.tianli.charge.vo.*;
import com.tianli.common.CommonFunction;
import com.tianli.common.ConfigConstants;
import com.tianli.common.RedisLockConstants;
import com.tianli.common.async.AsyncService;
import com.tianli.common.blockchain.NetworkType;
import com.tianli.common.webhook.WebHookService;
import com.tianli.common.webhook.WebHookTemplate;
import com.tianli.common.webhook.WebHookToken;
import com.tianli.currency.enums.TokenAdapter;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.management.query.FinancialChargeQuery;
import com.tianli.management.service.IWalletAgentService;
import com.tianli.mconfig.ConfigService;
import com.tianli.openapi.service.OrderRewardRecordService;
import com.tianli.product.afinancial.entity.FinancialProduct;
import com.tianli.product.afinancial.entity.FinancialRecord;
import com.tianli.product.afinancial.enums.ProductType;
import com.tianli.product.afinancial.service.FinancialRecordService;
import com.tianli.product.afinancial.vo.ExpectIncomeVO;
import com.tianli.product.service.FinancialProductService;
import com.tianli.sso.init.RequestInitService;
import com.tianli.tool.ApplicationContextTool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author wangqiyun
 * @since 2020/3/31 11:25
 */

@Slf4j
@Service
public class ChargeService extends ServiceImpl<OrderMapper, Order> {

    @Resource
    AccountBalanceOperationLogService logService;

    @Resource
    IOrderChargeTypeService iOrderChargeTypeService;

    @Resource
    AccountBalanceOperationLogMapper accountBalanceOperationLogMapper;

    private static final List<TransactionGroupTypeVO> transactionGroupTypeVOs = new ArrayList<>(2);

    static {
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
            transactionGroupTypeVOs.add(transactionGroupTypeVO);
        }
    }

    public List<TransactionGroupTypeVO> listTransactionGroupType(Long uid, List<ChargeGroup> groups) {
        boolean agent = walletAgentService.isAgent(uid);

        List<ChargeType> filterType =
                List.of(ChargeType.agent_fund_sale, ChargeType.agent_fund_interest, ChargeType.agent_fund_redeem);

        List<TransactionGroupTypeVO> result = JSONUtil.parseArray(transactionGroupTypeVOs).toList(TransactionGroupTypeVO.class);
        result = result.stream().filter(index -> groups.contains(index.getGroup())).collect(Collectors.toList());

        if (agent) {
            return result;
        }

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
            Coin coin = mainToken ? coinService.mainToken(chainType, chainType.getMainToken())
                    : coinService.getByContract(req.getContractAddress());
            if (coin == null) {
                continue;
            }
            Address address = getAddress(coin.getNetwork(), req.getTo());
            Long uid = address.getUid();
            BigDecimal finalAmount = TokenAdapter.alignment(coin, req.getValue());

            if (orderChargeInfoService.getOrderChargeByTxid(uid, req.getHash()) != null) {
                log.error("txid {} 已经存在充值订单", req.getHash());
                ErrorCodeEnum.TRADE_FAIL.throwException();
            }
            // 生成订单数据
            String orderNo = insertRechargeOrder(uid, req, coin, finalAmount, req.getValue());

            // 操作余额信息
            accountBalanceService.increase(uid, ChargeType.recharge, coin.getName()
                    , finalAmount, orderNo, coin.getNetwork());

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
    public void withdrawCallback(ChainType chainType, String str) {
        var jsonArray = JSONUtil.parseObj(str).getJSONArray("token");
        var standardCurrencyArray = JSONUtil.parseObj(str).getJSONArray("standardCurrency");

        List<TRONTokenReq> tokenReqs = JSONUtil.toList(jsonArray, TRONTokenReq.class);
        List<TRONTokenReq> mainTokenReqs = JSONUtil.toList(standardCurrencyArray, TRONTokenReq.class);
        withdrawOperation(tokenReqs, chainType);
        withdrawOperation(mainTokenReqs, chainType);
    }

    private void withdrawOperation(List<TRONTokenReq> tronTokenReqs, ChainType chainType) {
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

            RLock rLock = redissonClient.getLock(RedisLockConstants.PRODUCT_WITHDRAW + order.getUid() + ":" + order.getCoin()); // 提现回调锁

            try {
                boolean lock = rLock.tryLock(10, TimeUnit.SECONDS);
                if (lock) {
                    // 只有这步受到事务的限制
                    orderReviewService.withdrawSuccess(order, orderChargeInfo);
                } else {
                    webHookService.dingTalkSend("提现回调超时！！！！！" + orderChargeInfo.getTxid());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                webHookService.dingTalkSend("提现回调失败！！！！！" + orderChargeInfo.getTxid(), e);
            } finally {
                rLock.unlock();
            }

        }
    }

    /**
     * 提现申请
     */
    public Long withdrawApply(Long uid, WithdrawQuery query) {
        Coin coin = coinService.getByNameAndNetwork(query.getCoin(), query.getNetwork());

        Address address = addressService.get(uid);

        if (NetworkType.trc20.equals(query.getNetwork()) && address.getTron().equals(query.getTo())) {
            ErrorCodeEnum.FINANCIAL_TO_ERROR.throwException();
        }

        if ((NetworkType.bep20.equals(query.getNetwork()) || NetworkType.erc20.equals(query.getNetwork()))
                && address.getEth().equals(query.getTo())) {
            ErrorCodeEnum.FINANCIAL_TO_ERROR.throwException();
        }
        OccasionalAddress occasionalAddress = occasionalAddressService.get(query.getTo(), query.getNetwork().getChainType());
        if (occasionalAddress != null) {
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
        BigDecimal withdrawAmount = new BigDecimal(query.getAmount());
        if (new BigDecimal(query.getAmount()).compareTo(withdrawMinAmount) < 0)
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

        //创建提现订单(提币申请)
        long id = CommonFunction.generalId();
        Order order = new Order();
        order.setUid(uid);
        order.setAmount(new BigDecimal(query.getAmount()));
        order.setServiceAmount(serviceAmount);
        order.setOrderNo(AccountChangeType.withdraw.getPrefix() + CommonFunction.generalSn(id));
        order.setStatus(ChargeStatus.created);
        order.setType(ChargeType.withdraw);
        order.setCoin(coin.getName());
        order.setCreateTime(now);
        order.setRelatedId(orderChargeInfo.getId());

        ChargeService chargeService = ApplicationContextTool.getBean(ChargeService.class);
        chargeService = Optional.ofNullable(chargeService).orElseThrow(ErrorCodeEnum.SYSTEM_ERROR::generalException);
        chargeService.commitWithdrawTransaction(uid, coin, withdrawAmount, orderChargeInfo, order);


        OrderReviewStrategy strategy = withdrawReviewStrategy.getStrategy(order, orderChargeInfo, true);
        if (!OrderReviewStrategy.AUTO_REVIEW_AUTO_TRANSFER.equals(strategy)) {
            String msg = WebHookTemplate.withdrawApply(Double.parseDouble(query.getAmount()), query.getCoin());
            webHookService.dingTalkSend(msg, WebHookToken.FINANCIAL_PRODUCT);
        }

        // 自动打币
        if (OrderReviewStrategy.AUTO_REVIEW_AUTO_TRANSFER.equals(strategy)) {
            OrderReviewQuery reviewQuery = OrderReviewQuery.builder()
                    .orderNo(order.getOrderNo())
                    .remarks("自动审核通过")
                    .rid(0L)
                    .reviewBy("系统自动")
                    .autoPass(true)
                    .pass(true).build();

            orderReviewService.review(reviewQuery);
        }
        return order.getId();
    }


    /**
     * 提现操作订单和账号的事务提前提交，以免后续自动审核异常导致的回滚
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void commitWithdrawTransaction(Long uid, Coin coin, BigDecimal withdrawAmount, OrderChargeInfo orderChargeInfo, Order order) {
        orderService.insert(orderChargeInfo);
        orderService.save(order);
        //冻结提现数额
        accountBalanceService.freeze(uid, ChargeType.withdraw, coin.getName(), withdrawAmount
                , order.getOrderNo(), coin.getNetwork());
    }

    /**
     * 提现上链
     */
    @Transactional
    public String withdrawChain(Order order, OrderChargeInfo orderChargeInfo) {

        ContractOperation contractService = contractAdapter.getOne(orderChargeInfo.getNetwork());
        Coin coin = coinService.getByNameAndNetwork(orderChargeInfo.getCoin(), orderChargeInfo.getNetwork());
        BigInteger amount = TokenAdapter.restoreBigInteger(order.getAmount().subtract(order.getServiceAmount()), coin.getDecimals());
        String hash;

        /* 注册监听回调接口
         * {@link com.tianli.charge.controller.ChargeController#withdrawCallback(ChainType, String, String, String)}
         */
        if (orderChargeInfo.getNetwork().equals(NetworkType.erc20) || orderChargeInfo.getNetwork().equals(NetworkType.bep20) || orderChargeInfo.getNetwork().equals(NetworkType.trc20)) {
            chainService.pushWithdrawCondition(orderChargeInfo.getNetwork(), orderChargeInfo.getCoin()
                    , new CallbackPathDTO("/api/charge/withdraw"), orderChargeInfo.getToAddress());
        }

        try {
            hash = contractService.transfer(orderChargeInfo.getToAddress(), amount, coin);
        } catch (Exception e) {
            webHookService.dingTalkSend("上链失败", e);
            return null;
        }
        return hash;
    }

    /**
     * 结算列表
     */
    public IPage<OrderSettleRecordVO> settleOrderPage(IPage<OrderSettleRecordVO> page, Long uid, ProductType productType) {
        return orderService.orderSettleInfoVOPage(page, uid, productType);
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

        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<Order>();
        if (Objects.nonNull(uid)) {
            queryWrapper .eq(Order::getUid, uid);
        }
        queryWrapper .eq(Order::getOrderNo, orderNo);
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
        orderChargeInfo = Optional.ofNullable(orderChargeInfo).orElse(new OrderChargeInfo());

        OrderChargeInfoVO orderChargeInfoVO = chargeConverter.toVO(order);
        orderChargeInfoVO.setFromAddress(orderChargeInfo.getFromAddress());
        orderChargeInfoVO.setToAddress(orderChargeInfo.getToAddress());
        orderChargeInfoVO.setTxid(orderChargeInfo.getTxid());
        orderChargeInfoVO.setCreateTime(orderChargeInfo.getCreateTime());
        orderChargeInfoVO.setLogo(coinBase.getLogo());
        orderChargeInfoVO.setNetworkType(orderChargeInfo.getNetwork());
        orderChargeInfoVO.setRealAmount(order.getAmount().subtract(order.getServiceAmount()));
        if (orderChargeInfoVO.getType().equals(ChargeType.withdraw)&&orderChargeInfoVO.getStatus().equals(ChargeStatus.chain_success)){
            orderChargeInfoVO.setNewChargeType(NewChargeType.withdraw_success);
            orderChargeInfoVO.setNewChargeTypeName(NewChargeType.withdraw_success.getNameZn());
            orderChargeInfoVO.setNewChargeTypeNameEn(NewChargeType.withdraw_success.getNameEn());
        }
        if (orderChargeInfoVO.getType().equals(ChargeType.recharge)&&orderChargeInfoVO.getStatus().equals(ChargeStatus.chain_success)){
            orderChargeInfoVO.setNewChargeType(NewChargeType.recharge);
            orderChargeInfoVO.setNewChargeTypeName(NewChargeType.recharge.getNameZn());
            orderChargeInfoVO.setNewChargeTypeNameEn(NewChargeType.recharge.getNameEn());
        }
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

        FinancialRecord financialRecord = financialRecordService.selectById(order.getRelatedId(), uid);
        OrderBaseVO orderBaseVO = getOrderBaseVO(order, financialRecord);
        orderBaseVO.setChargeStatus(order.getStatus());
        orderBaseVO.setChargeType(order.getType());
        orderBaseVO.setOrderNo(order.getOrderNo());
        orderBaseVO.setAmount(order.getAmount());
        orderBaseVO.setProductId(financialRecord.getProductId());

        // 对于活期记录来说，因为持有是累加的，导致持有记录表中的申购时间是不对的，需要取订单表
        if (orderBaseVO instanceof OrderRechargeDetailsVo) {
            OrderRechargeDetailsVo orderBase = (OrderRechargeDetailsVo) orderBaseVO;
            orderBase.setPurchaseTime(order.getCreateTime());
        }
        return orderBaseVO;
    }

    private OrderBaseVO getOrderBaseVO(Order order, FinancialRecord financialRecord) {
        FinancialProduct product = financialProductService.getById(financialRecord.getProductId());
        switch (order.getType()) {
            case purchase:
            case transfer:
                var orderRechargeDetailsVo = chargeConverter.toOrderRechargeDetailsVo(financialRecord);
                orderRechargeDetailsVo.setPurchaseTime(financialRecord.getPurchaseTime());
                ExpectIncomeVO expectIncomeVO = financialProductService.expectIncome(financialRecord.getProductId(), order.getAmount());
                orderRechargeDetailsVo.setExpectIncome(expectIncomeVO.getExpectIncome());
                orderRechargeDetailsVo.setRateType(product.getRateType());
                orderRechargeDetailsVo.setMaxRate(product.getMaxRate());
                orderRechargeDetailsVo.setMinRate(product.getMinRate());
                return orderRechargeDetailsVo;
            case redeem:
                var orderRedeemDetailsVO = chargeConverter.toOrderRedeemDetailsVO(financialRecord);
                orderRedeemDetailsVO.setRedeemTime(order.getCreateTime());
                orderRedeemDetailsVO.setRedeemEndTime(order.getCreateTime());
                return orderRedeemDetailsVO;
            default:
                return chargeConverter.toOrderBaseVO(financialRecord);
        }
    }

    /**
     * 获取充值DTO数据 不同链的usdt后面的0个数不一样  需要做一个对齐处理 目前是后面8个0为1个u
     */
    private Address getAddress(NetworkType network, String addressStr) {
        Address address;
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
                OccasionalAddress occasionalAddress = occasionalAddressService.get(addressStr, network.getChainType());
                address = addressService.getById(occasionalAddress.getAddressId());
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


        if (CollectionUtils.isNotEmpty(query.chargeTypeSet())) {
            wrapper = wrapper.in(Order::getType, query.chargeTypeSet());
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
            ChargeType type = orderChargeInfoVO.getType();
            if (ChargeType.transaction_reward.equals(type)) {
                int i = orderRewardRecordService.recordCountThisHour(charge.getUid(), charge.getCreateTime());
                orderChargeInfoVO.setRemarks("已发放" + i + "笔");
                orderChargeInfoVO.setRemarksEn(i + " Receiving Record(s)");
            }

            if (!ChargeType.transaction_reward.equals(type)) {
                ChargeRemarks remarks = ChargeRemarks.getInstance(charge.getType(), charge.getStatus());
                orderChargeInfoVO.setRemarks(remarks.getRemarks());
                orderChargeInfoVO.setRemarksEn(remarks.getRemarksEn());
            }

            if (ChargeType.user_credit_in.equals(type) || ChargeType.user_credit_out.equals(type) ||
                    ChargeType.credit_out.equals(type) || ChargeType.credit_in.equals(type)) {
                AccountUserTransfer accountUserTransfer =
                        accountUserTransferService.getByExternalPk(charge.getRelatedId());

                Optional.ofNullable(accountUserTransfer)
                        .ifPresent(a -> orderChargeInfoVO.setOrderOtherInfoVo(OrderOtherInfoVo.builder()
                                .transferExternalPk(a.getExternalPk())
                                .build()));

            }
            return orderChargeInfoVO;
        });
    }

    /**
     * 获取分页数据 2023-03-13需求改动
     * 查询account_balance_operation_log表
     */
    public IPage<AccountBalanceOperationLogVo> newPageByChargeGroup(Long uid, AccountDetailsNewQuery query, Page<AccountBalanceOperationLog> page) {

        //提币类型的需要根据两个字段查询
        if (CollectionUtils.isNotEmpty(query.getChargeType())) {
            if (query.getChargeType().contains(WithdrawChargeTypeEnum.withdraw_success.name())||
                    query.getChargeType().contains(WithdrawChargeTypeEnum.withdraw_failed.name())||
            query.getChargeType().contains(WithdrawChargeTypeEnum.withdraw_freeze.name())) {
                List<String> withdrawTypes = query.getChargeType().stream().filter(chargeType -> chargeType.contains(WithdrawChargeTypeEnum.withdraw.name())).collect(Collectors.toList());
                query.setSepicalType(WithdrawChargeTypeEnum.withdraw.name());
                query.setLogType(WithdrawChargeTypeEnum.getTypeByDesc(withdrawTypes.get(0)));
            }
        }
        Page<AccountBalanceOperationLog> logPages = accountBalanceOperationLogMapper.pageList(page, uid, query);
        return logPages.convert(logPage -> {
            return   log2VO(logPage);
        });
    }


    /**
     * 封装AccountBalanceOperationLogVo
     *
     * @param log
     * @return
     */
    public AccountBalanceOperationLogVo log2VO(AccountBalanceOperationLog log) {
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getOrderNo, log.getOrderNo());
        Order order = orderService.getOne(wrapper);
        MoreObjects.firstNonNull(order, new Order());
        AccountBalanceOperationLogVo logVo = new AccountBalanceOperationLogVo();
        logVo.setId(log.getId());
        logVo.setUid(log.getUid());
        logVo.setOrderNo(log.getOrderNo());
        ChargeType chargeType = log.getChargeType();
        logVo.setNewChargeType(NewChargeType.getInstance(chargeType));
        NewChargeType newChargeType = logVo.getNewChargeType();
        //提币状态分离
        if (newChargeType.name().equals(WithdrawChargeTypeEnum.withdraw.getType())) {
            OrderChargeInfo orderChargeInfo = orderChargeInfoService.getById(order.getRelatedId());
            if (log.getLogType().name().equals(WithdrawChargeTypeEnum.withdraw_success.getType())) {
                //只有提币成功、提币失败（链上失败）有详情，提币冻结（即提币中）、提币失败（审核拒绝）都是没有详情的
                logVo.setStatus(NewChargeStatus.withdraw_success);
                logVo.setNewChargeType(NewChargeType.withdraw_success);
                logVo.setNewChargeTypeName(NewChargeType.withdraw_success.getNameZn());
                logVo.setNewChargeTypeNameEn(NewChargeType.withdraw_success.getNameEn());
                if (Objects.nonNull(orderChargeInfo)){
                    logVo.setTxid(orderChargeInfo.getTxid());
                }
            } else if (log.getLogType().name().equals(WithdrawChargeTypeEnum.withdraw_failed.getType())) {
                logVo.setStatus(NewChargeStatus.withdraw_failed);
                logVo.setNewChargeType(NewChargeType.withdraw_failed);
                logVo.setNewChargeTypeName(NewChargeType.withdraw_failed.getNameZn());
                logVo.setNewChargeTypeNameEn(NewChargeType.withdraw_failed.getNameEn());
                if (Objects.nonNull(orderChargeInfo)){
                    logVo.setTxid(orderChargeInfo.getTxid());
                }
            } else {
                logVo.setStatus(NewChargeStatus.withdraw_freeze);
                logVo.setNewChargeType(NewChargeType.withdraw_freeze);
                logVo.setNewChargeTypeName(NewChargeType.withdraw_freeze.getNameZn());
                logVo.setNewChargeTypeNameEn(NewChargeType.withdraw_freeze.getNameEn());
            }
        } else {
            logVo.setStatus(NewChargeStatus.getInstance(order.getStatus()));
            logVo.setNewChargeTypeName(NewChargeType.getInstance(chargeType).getNameZn());
            logVo.setNewChargeTypeNameEn(NewChargeType.getInstance(chargeType).getNameEn());
        }

        logVo.setCreateTime(log.getCreateTime());
        logVo.setCompleteTime(order.getCompleteTime());
        logVo.setAmount(log.getAmount());
        logVo.setCoin(log.getCoin());
        logVo.setServiceAmount(order.getServiceAmount());
        logVo.setUpdateTime(order.getUpdateTime());
        logVo.setRelatedId(order.getRelatedId());
        LambdaQueryWrapper<OrderChargeType> wrapper1 = new LambdaQueryWrapper<>();
        wrapper1.eq(OrderChargeType::getType,logVo.getNewChargeType());
        OrderChargeType orderChargeType = iOrderChargeTypeService.getOne(wrapper1);
        logVo.setGroupEn(orderChargeType.getOperationGroup());
        logVo.setGroup(ChargeTypeGroupEnum.getTypeGroup(orderChargeType.getOperationGroup()));
        NewChargeType type = logVo.getNewChargeType();
        if (NewChargeType.transaction_reward.equals(type)) {
            int i = orderRewardRecordService.recordCountThisHour(logVo.getUid(), logVo.getCreateTime());
            logVo.setRemarks("已发放" + i + "笔");
            logVo.setRemarksEn(i + " Receiving Record(s)");
        }

        if (!NewChargeType.transaction_reward.equals(type)) {
            NewChargeRemarks remarks = NewChargeRemarks.getInstance(logVo.getNewChargeType(), logVo.getStatus());
            logVo.setRemarks(remarks.getRemarks());
            logVo.setRemarksEn(remarks.getRemarksEn());
        }

        if (NewChargeType.user_credit_in.equals(type) || NewChargeType.user_credit_out.equals(type) ||
                ChargeType.credit_out.equals(type) || ChargeType.credit_in.equals(type)) {
            AccountUserTransfer accountUserTransfer =
                    accountUserTransferService.getByExternalPk(logVo.getRelatedId());

            Optional.ofNullable(accountUserTransfer)
                    .ifPresent(a -> logVo.setOrderOtherInfoVo(OrderOtherInfoVo.builder()
                            .transferExternalPk(a.getExternalPk())
                            .build()));

        }

        return logVo;
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
            case erc20_arbitrum:
                fromAddress = configService.get(ConfigConstants.ARBITRUM_MAIN_WALLET_ADDRESS);
                break;
            case erc20_optimistic:
                fromAddress = configService.get(ConfigConstants.OP_MAIN_WALLET_ADDRESS);
                break;
            case erc20_polygon:
                fromAddress = configService.get(ConfigConstants.POLYGON_MAIN_WALLET_ADDRESS);
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
    private AsyncService asyncService;
    @Resource
    private OrderReviewService orderReviewService;
    @Resource
    private WebHookService webHookService;
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
    @Resource
    private OccasionalAddressService occasionalAddressService;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private AccountUserTransferService accountUserTransferService;
}
