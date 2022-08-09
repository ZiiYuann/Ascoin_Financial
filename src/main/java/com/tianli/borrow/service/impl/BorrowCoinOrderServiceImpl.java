package com.tianli.borrow.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.account.entity.AccountBalance;
import com.tianli.account.enums.AccountChangeType;
import com.tianli.account.enums.AccountOperationType;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.borrow.contant.*;
import com.tianli.borrow.convert.BorrowOrderConverter;
import com.tianli.borrow.dao.*;
import com.tianli.borrow.bo.AdjustPledgeBO;
import com.tianli.borrow.bo.BorrowOrderBO;
import com.tianli.borrow.bo.BorrowOrderRepayBO;
import com.tianli.borrow.entity.*;
import com.tianli.borrow.enums.BorrowStatisticsType;
import com.tianli.borrow.query.BorrowInterestRecordQuery;
import com.tianli.borrow.query.BorrowOrderQuery;
import com.tianli.borrow.query.BorrowPledgeRecordQuery;
import com.tianli.borrow.query.BorrowRepayQuery;
import com.tianli.borrow.service.IBorrowCoinConfigService;
import com.tianli.borrow.service.IBorrowCoinOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.borrow.service.IBorrowOrderNumDailyService;
import com.tianli.borrow.service.IBorrowPledgeCoinConfigService;
import com.tianli.borrow.vo.*;
import com.tianli.charge.entity.Order;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.service.OrderService;
import com.tianli.common.CommonFunction;
import com.tianli.common.PageQuery;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.financial.mapper.FinancialRecordMapper;
import com.tianli.sso.init.RequestInitService;
import com.tianli.tool.time.TimeTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


/**
 * <p>
 * 借币订单 服务实现类
 * </p>
 *
 * @author xn
 * @since 2022-07-20
 */
@Service
@Transactional
public class BorrowCoinOrderServiceImpl extends ServiceImpl<BorrowCoinOrderMapper, BorrowCoinOrder> implements IBorrowCoinOrderService {

    @Resource
    private BorrowOrderConverter borrowConverter;

    @Autowired
    private RequestInitService requestInitService;

    @Autowired
    private FinancialRecordMapper financialRecordMapper;

    @Autowired
    private BorrowCoinOrderMapper borrowCoinOrderMapper;

    @Autowired
    private IBorrowCoinConfigService borrowCoinConfigService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private AccountBalanceService accountBalanceService;

    @Autowired
    private BorrowPledgeRecordMapper borrowPledgeRecordMapper;

    @Autowired
    private BorrowInterestRecordMapper borrowInterestRecordMapper;

    @Autowired
    private BorrowRepayRecordMapper borrowRepayRecordMapper;

    @Autowired
    private IBorrowPledgeCoinConfigService  borrowPledgeCoinConfigService;

    @Autowired
    private IBorrowOrderNumDailyService borrowOrderNumDailyService;

    @Override
    public BorrowCoinMainPageVO mainPage() {
        Long uid = requestInitService.uid();
        //存款市场总额
        BigDecimal totalHoldAmount = financialRecordMapper.selectTotalHoldAmount();
        //借款市场总额
        BigDecimal totalBorrowAmount = borrowCoinOrderMapper.selectTotalBorrowAmount();
        //借出总额
        BigDecimal borrowAmount = borrowCoinOrderMapper.selectBorrowAmountByUid(uid);
        //质押数额
        BigDecimal pledgeAmount = borrowCoinOrderMapper.selectPledgeAmountByUid(uid);

        Integer historyOrderCount = borrowCoinOrderMapper.selectCount(new QueryWrapper<BorrowCoinOrder>().lambda()
                .eq(BorrowCoinOrder::getUid, uid));

        List<BorrowCoinMainPageVO.BorrowOrder> borrowCoinOrders = borrowCoinOrderMapper.selectList(new QueryWrapper<BorrowCoinOrder>().lambda()
                .eq(BorrowCoinOrder::getUid, uid)
                .eq(BorrowCoinOrder::getStatus, BorrowOrderStatus.INTEREST_ACCRUAL)
                .orderByDesc(BorrowCoinOrder::getBorrowTime))
                .stream().map(borrowConverter::toMainVO).collect(Collectors.toList());
        return BorrowCoinMainPageVO.builder()
                .totalDepositAmount(totalHoldAmount)
                .totalBorrowAmount(totalBorrowAmount)
                .borrowAmount(borrowAmount)
                .pledgeAmount(pledgeAmount)
                .hasHistoryOrder(historyOrderCount > 0)
                .borrowOrders(borrowCoinOrders).build();
    }

    @Override
    public IPage<BorrowCoinOrderVO> pageList(PageQuery<BorrowCoinOrder> pageQuery, BorrowOrderQuery query) {

        QueryWrapper<BorrowCoinOrder> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc(query.getOrderColumn());
        LambdaQueryWrapper<BorrowCoinOrder> lambdaQueryWrapper = queryWrapper.lambda();
        if(Objects.nonNull(query.getUid())){
            lambdaQueryWrapper.eq(BorrowCoinOrder::getUid, query.getUid());
        }

        if(Objects.nonNull(query.getStatus())){
            lambdaQueryWrapper.in(BorrowCoinOrder::getStatus, query.getStatus());
        }

        if(StrUtil.isNotEmpty(query.getQueryUid())){
            lambdaQueryWrapper.like(BorrowCoinOrder::getUid,query.getQueryUid());
        }

        if(StrUtil.isNotEmpty(query.getQueryOrderId())){
            lambdaQueryWrapper.like(BorrowCoinOrder::getId,query.getQueryOrderId());
        }

        if(Objects.nonNull(query.getPledgeStatus())){
            lambdaQueryWrapper.eq(BorrowCoinOrder::getPledgeStatus,query.getPledgeStatus());
        }

        if(Objects.nonNull(query.getMinPledgeRate())){
            lambdaQueryWrapper.ge(BorrowCoinOrder::getPledgeRate,query.getMinPledgeRate());
        }

        if(Objects.nonNull(query.getMaxPledgeRate())){
            lambdaQueryWrapper.le(BorrowCoinOrder::getPledgeRate,query.getMaxPledgeRate());
        }

        if(Objects.nonNull(query.getStartTime())){
            lambdaQueryWrapper.ge(BorrowCoinOrder::getBorrowTime,query.getStartTime());
        }

        if(Objects.nonNull(query.getEndTime())){
            lambdaQueryWrapper.le(BorrowCoinOrder::getBorrowTime,query.getEndTime());
        }

        return borrowCoinOrderMapper.selectPage(pageQuery.page(),queryWrapper).convert(borrowConverter::toVO);
    }

    @Override
    public AmountVO cumulativeInterestAmount(BorrowOrderQuery query) {
        BigDecimal cumulativeInterest = borrowCoinOrderMapper.selectCumulativeInterestByQuery(query);
        return new AmountVO(cumulativeInterest);
    }

    @Override
    public BorrowApplePageVO applyPage(CurrencyCoin coin) {
        Long uid = requestInitService.uid();
        BorrowCoinConfig coinConfig = borrowCoinConfigService.getByCoin(coin);
        BorrowPledgeCoinConfig pledgeCoinConfig = borrowPledgeCoinConfigService.getByCoin(coin);
        if(Objects.isNull(coinConfig)) ErrorCodeEnum.BORROW_CONFIG_NO_EXIST.throwException();
        if(Objects.isNull(pledgeCoinConfig)) ErrorCodeEnum.BORROW_CONFIG_NO_EXIST.throwException();
        AccountBalance accountBalance = accountBalanceService.getAndInit(uid, CurrencyCoin.usdt);
        return BorrowApplePageVO.builder()
                .coin(coin.getName())
                .logo(coin.getLogoPath())
                .availableAmount(accountBalance.getRemain())
                .maximumBorrow(coinConfig.getMaximumBorrow())
                .minimumBorrow(coinConfig.getMinimumBorrow())
                .annualInterestRate(coinConfig.getAnnualInterestRate())
                .initialPledgeRate(pledgeCoinConfig.getInitialPledgeRate())
                .liquidationPledgeRate(pledgeCoinConfig.getLiquidationPledgeRate())
                .build();
    }

    @Override
    public void borrowCoin(BorrowOrderBO bo) {
        Long uid = requestInitService.uid();
        CurrencyCoin currencyCoin = bo.getCoin();

        BigDecimal borrowAmount = bo.getBorrowAmount();
        BorrowCoinConfig coinConfig = borrowCoinConfigService.getByCoin(currencyCoin);
        BorrowPledgeCoinConfig pledgeCoinConfig = borrowPledgeCoinConfigService.getByCoin(currencyCoin);
        //校验币别配置
        if(Objects.isNull(coinConfig) || Objects.isNull(pledgeCoinConfig))ErrorCodeEnum.BORROW_CONFIG_NO_EXIST.throwException();

        //校验单笔最大和最小数量
        BigDecimal minimumBorrow = coinConfig.getMinimumBorrow();
        BigDecimal maximumBorrow = coinConfig.getMaximumBorrow();
        if(borrowAmount.compareTo(minimumBorrow) < 0 || borrowAmount.compareTo(maximumBorrow)>0)ErrorCodeEnum.BORROW_RANGE_ERROR.throwException();

        //校验数量
        BigDecimal initialPledgeRate = pledgeCoinConfig.getInitialPledgeRate();
        AccountBalance accountBalance = accountBalanceService.getAndInit(uid, currencyCoin);
        BigDecimal availableAmount = accountBalance.getRemain();
        if(borrowAmount.compareTo(availableAmount.multiply(initialPledgeRate)) > 0)ErrorCodeEnum.BORROW_GT_AVAILABLE_ERROR.throwException();

        //质押数量
        BigDecimal pledgeAmount = borrowAmount.divide(initialPledgeRate,8,RoundingMode.UP);

        //添加订单
        BorrowCoinOrder borrowCoinOrder = BorrowCoinOrder.builder()
                .uid(uid)
                .borrowCoin(currencyCoin.getName())
                .logo(currencyCoin.getLogoPath())
                .borrowCapital(borrowAmount)
                .waitRepayCapital(borrowAmount)
                .pledgeCoin(currencyCoin.getName())
                .pledgeAmount(pledgeAmount)
                .pledgeRate(initialPledgeRate)
                .status(BorrowOrderStatus.INTEREST_ACCRUAL)
                .pledgeStatus(BorrowOrderPledgeStatus.SAFE_PLEDGE)
                .borrowTime(LocalDateTime.now())
                .createTime(LocalDateTime.now()).build();
        borrowCoinOrderMapper.insert(borrowCoinOrder);
        //质押余额
        borrowPledge(uid,borrowCoinOrder.getId(),currencyCoin,pledgeAmount);
        //增加余额
        borrowCoin(uid,borrowCoinOrder.getId(),currencyCoin,borrowAmount);
        //添加质押记录
        BorrowPledgeRecord pledgeRecord = BorrowPledgeRecord.builder()
                .coin(currencyCoin.getName())
                .orderId(borrowCoinOrder.getId())
                .amount(pledgeAmount)
                .type(BorrowPledgeType.INIT)
                .pledgeTime(LocalDateTime.now())
                .createTime(LocalDateTime.now()).build();
        borrowPledgeRecordMapper.insert(pledgeRecord);

        //统计每日计息订单
        borrowOrderNumDailyService.statisticalOrderNum();

    }

    @Override
    public BorrowCoinOrderVO info(Long orderId) {
        BorrowCoinOrder borrowCoinOrder = borrowCoinOrderMapper.selectById(orderId);
        if(Objects.isNull(borrowCoinOrder)) ErrorCodeEnum.BORROW_ORDER_NO_EXIST.throwException();
        BorrowCoinOrderVO borrowCoinOrderVO = borrowConverter.toVO(borrowCoinOrder);
        BorrowCoinConfig coinConfig = borrowCoinConfigService.getByCoin(CurrencyCoin.valueOf(borrowCoinOrder.getBorrowCoin()));
        borrowCoinOrderVO.setAnnualInterestRate(coinConfig.getAnnualInterestRate());
        return borrowCoinOrderVO;
    }

    @Override
    public BorrowRecordVO borrowRecord(Long orderId) {
        BorrowCoinOrder borrowCoinOrder = borrowCoinOrderMapper.selectById(orderId);
        if(Objects.isNull(borrowCoinOrder))return new BorrowRecordVO();
        BorrowRecordVO recordVO = new BorrowRecordVO();
        Integer status = borrowCoinOrder.getStatus();
        List<BorrowRecordVO.Record> records = new ArrayList<>();
        BorrowRecordVO.Record createRecord = new BorrowRecordVO.Record();
        createRecord.setRecord("订单创建");
        createRecord.setRecordEn("create order");
        createRecord.setTime(borrowCoinOrder.getCreateTime());
        records.add(createRecord);
        BorrowRecordVO.Record borrowingRecord = new BorrowRecordVO.Record();
        borrowingRecord.setRecord("借币中");
        borrowingRecord.setRecordEn("borrowing");
        records.add(borrowingRecord);
        BorrowRecordVO.Record record = new BorrowRecordVO.Record();
        record.setTime(borrowCoinOrder.getSettlementTime());
        long borrowDuration ;
        if(status.equals(BorrowOrderStatus.SUCCESSFUL_REPAYMENT)){
            record.setRecord("还款成功");
            record.setRecordEn("successful repayment");
            records.add(record);
            borrowingRecord.setTime(borrowCoinOrder.getSettlementTime());
            borrowDuration = DateUtil.between(TimeTool.toDate(borrowCoinOrder.getCreateTime()) ,TimeTool.toDate(borrowCoinOrder.getSettlementTime()), DateUnit.HOUR);
        }else if (status.equals(BorrowOrderStatus.FORCED_LIQUIDATION)){
            record.setRecord("已平仓");
            record.setRecordEn("liquidated");
            records.add(record);
            borrowingRecord.setTime(borrowCoinOrder.getSettlementTime());
            borrowDuration = DateUtil.between(TimeTool.toDate(borrowCoinOrder.getCreateTime()) ,TimeTool.toDate(borrowCoinOrder.getSettlementTime()), DateUnit.HOUR);
        }else {
            borrowingRecord.setTime(LocalDateTime.now());
            borrowDuration = DateUtil.between(TimeTool.toDate(borrowCoinOrder.getCreateTime()),new Date(), DateUnit.HOUR);
        }
        recordVO.setRecords(records);
        recordVO.setBorrowDuration(borrowDuration);
        return recordVO;
    }

    @Override
    public IPage<BorrowPledgeRecordVO>  pledgeRecord(PageQuery<BorrowPledgeRecord> pageQuery, BorrowPledgeRecordQuery query) {
        LambdaQueryWrapper<BorrowPledgeRecord> queryWrapper = new QueryWrapper<BorrowPledgeRecord>().lambda();

        if(Objects.nonNull(query.getOrderId())){
            queryWrapper.eq(BorrowPledgeRecord::getOrderId,query.getOrderId());
        }

        if(Objects.nonNull(query.getType())){
            queryWrapper.eq(BorrowPledgeRecord::getType,query.getType());
        }

        if(Objects.nonNull(query.getStartTime())){
            queryWrapper.ge(BorrowPledgeRecord::getPledgeTime,query.getStartTime());
        }

        if(Objects.nonNull(query.getEndTime())){
            queryWrapper.le(BorrowPledgeRecord::getPledgeTime,query.getEndTime());
        }

        queryWrapper.ne(BorrowPledgeRecord::getType,4);

        queryWrapper.orderByDesc(BorrowPledgeRecord::getPledgeTime);
        return borrowPledgeRecordMapper.selectPage(pageQuery.page(), queryWrapper).convert(borrowConverter::toPledgeVO);
    }

    @Override
    public IPage<BorrowInterestRecordVO> interestRecord(PageQuery<BorrowInterestRecord> pageQuery, BorrowInterestRecordQuery query) {


        LambdaQueryWrapper<BorrowInterestRecord> queryWrapper = new QueryWrapper<BorrowInterestRecord>().lambda();

        if(Objects.nonNull(query.getOrderId())){
            queryWrapper.eq(BorrowInterestRecord::getOrderId,query.getOrderId());
        }

        if(Objects.nonNull(query.getStartTime())){
            queryWrapper.ge(BorrowInterestRecord::getInterestAccrualTime,query.getStartTime());
        }

        if(Objects.nonNull(query.getEndTime())){
            queryWrapper.le(BorrowInterestRecord::getInterestAccrualTime,query.getEndTime());
        }

        queryWrapper.orderByDesc(BorrowInterestRecord::getInterestAccrualTime);
        return borrowInterestRecordMapper.selectPage(pageQuery.page(),queryWrapper).convert(borrowConverter::toInterestVO);
    }

    @Override
    public AmountVO interestAmount(BorrowInterestRecordQuery query) {
        return new AmountVO(borrowInterestRecordMapper.selectInterestSumByQuery(query)) ;
    }

    @Override
    public BorrowOrderAmountVO cumulativeAmount(BorrowOrderQuery query) {
        return borrowCoinOrderMapper.selectAmountSumByQuery(query);
    }

    @Override
    public IPage<BorrowRepayRecordVO> repayRecord(PageQuery<BorrowRepayRecord> pageQuery, BorrowRepayQuery query) {
        LambdaQueryWrapper<BorrowRepayRecord> queryWrapper = new QueryWrapper<BorrowRepayRecord>().lambda();

        if(Objects.nonNull(query.getOrderId())){
            queryWrapper.eq(BorrowRepayRecord::getOrderId, query.getOrderId());
        }

        if(Objects.nonNull(query.getType())){
            queryWrapper.eq(BorrowRepayRecord::getType, query.getType());
        }

        if(Objects.nonNull(query.getStatus())){
            queryWrapper.eq(BorrowRepayRecord::getStatus, query.getStatus());
        }

        if(Objects.nonNull(query.getStartTime())){
            queryWrapper.ge(BorrowRepayRecord::getRepayTime, query.getStartTime());
        }

        if(Objects.nonNull(query.getEndTime())){
            queryWrapper.le(BorrowRepayRecord::getRepayTime, query.getEndTime());
        }

        queryWrapper.orderByDesc(BorrowRepayRecord::getRepayTime);
        return borrowRepayRecordMapper.selectPage(pageQuery.page(),queryWrapper).convert(borrowConverter::toRepayVO);
    }


    @Override
    public AmountVO repayAmount(BorrowRepayQuery query) {
        BigDecimal repaySum = borrowRepayRecordMapper.selectRepaySumQuery(query);
        return new AmountVO(repaySum);
    }

    @Override
    public BorrowLiquidationRecordVO liquidationRecord(Long orderId) {
        BorrowCoinOrder borrowCoinOrder = borrowCoinOrderMapper.selectById(orderId);
        if(Objects.isNull(borrowCoinOrder))ErrorCodeEnum.BORROW_ORDER_NO_EXIST.throwException();
        if(!borrowCoinOrder.getStatus().equals(BorrowOrderStatus.FORCED_LIQUIDATION)) return new BorrowLiquidationRecordVO();
        return BorrowLiquidationRecordVO.builder().status("已平仓").time(borrowCoinOrder.getSettlementTime()).build();
    }

    @Override
    public BorrowRepayPageVO repayPage(Long orderId,BigDecimal repayAmount, CurrencyCoin coin) {
        BorrowCoinOrder borrowCoinOrder = borrowCoinOrderMapper.selectById(orderId);
        if(Objects.isNull(borrowCoinOrder))return null;
        BigDecimal waitRepayInterest = borrowCoinOrder.getWaitRepayInterest();
        BigDecimal waitRepayCapital = borrowCoinOrder.getWaitRepayCapital();
        BigDecimal pledgeAmount = borrowCoinOrder.getPledgeAmount();
        BigDecimal waitRepay = borrowCoinOrder.calculateWaitRepay();
        Long uid = requestInitService.uid();
        AccountBalance accountBalance = accountBalanceService.getAndInit(uid, coin);
        BorrowPledgeCoinConfig pledgeCoinConfig = borrowPledgeCoinConfigService.getByCoin(coin);
        BigDecimal initialPledgeRate = pledgeCoinConfig.getInitialPledgeRate();

        BigDecimal totalRepayAmount = waitRepay;
        BigDecimal repayInterest;
        BigDecimal repayCapital;
        BigDecimal pledgeRate;
        BigDecimal releasePledgeAmount = BigDecimal.ZERO;

        if(repayAmount.compareTo(waitRepay) > 0)ErrorCodeEnum.REPAY_GT_CAPITAL.throwException();

        if(repayAmount.compareTo(waitRepay) == 0){
            repayInterest = waitRepayInterest;
            repayCapital = waitRepayCapital;
            releasePledgeAmount = borrowCoinOrder.getPledgeAmount();
            pledgeRate = BigDecimal.ZERO;
        }else {
            if (repayAmount.compareTo(waitRepayInterest) <= 0) {
                repayInterest = repayAmount;
                repayCapital = BigDecimal.ZERO;
            } else {
                repayInterest = borrowCoinOrder.getWaitRepayInterest();
                repayCapital = repayAmount.subtract(repayInterest);
            }
            waitRepay = waitRepay.subtract(repayAmount);
            pledgeRate = waitRepay.divide(pledgeAmount, 8, RoundingMode.UP);
            if (pledgeRate.compareTo(initialPledgeRate) < 0) {
                pledgeRate = initialPledgeRate;
                releasePledgeAmount = pledgeAmount.subtract(waitRepay.divide(pledgeRate, 8, RoundingMode.UP));
            }
        }
        return BorrowRepayPageVO.builder()
                .waitRepayAmount(borrowCoinOrder.calculateWaitRepay())
                .totalRepayAmount(totalRepayAmount)
                .availableBalance(accountBalance.getRemain())
                .repayCapital(repayCapital)
                .repayInterest(repayInterest)
                .pledgeRate(pledgeRate)
                .releasePledgeAmount(releasePledgeAmount)
                .coin(borrowCoinOrder.getBorrowCoin())
                .logo(borrowCoinOrder.getLogo())
                .build();
    }

    @Override
    public void orderRepay(BorrowOrderRepayBO bo) {
        Long uid = requestInitService.uid();
        Long orderId = bo.getOrderId();
        BigDecimal repayAmount = bo.getRepayAmount();
        CurrencyCoin currencyCoin = bo.getCurrencyCoin();

        //校验钱余额
        AccountBalance accountBalance = accountBalanceService.getAndInit(uid, currencyCoin);
        if(accountBalance.getRemain().compareTo(repayAmount) <= 0)ErrorCodeEnum.INSUFFICIENT_BALANCE.throwException();
        //校验订单
        BorrowCoinOrder borrowCoinOrder = borrowCoinOrderMapper.selectById(orderId);
        if(Objects.isNull(borrowCoinOrder))ErrorCodeEnum.BORROW_ORDER_NO_EXIST.throwException();
        if(!borrowCoinOrder.getStatus().equals(BorrowOrderStatus.INTEREST_ACCRUAL))ErrorCodeEnum.BORROW_ORDER_STATUS_ERROR.throwException();
        //校验还款币别
        if(!borrowCoinOrder.getBorrowCoin().equals(currencyCoin.getName()))ErrorCodeEnum.CURRENCY_COIN_ERROR.throwException();
        BigDecimal waitRepayCapital = borrowCoinOrder.getWaitRepayCapital();
        BigDecimal waitRepayInterest = borrowCoinOrder.getWaitRepayInterest();
        BigDecimal totalAmount = borrowCoinOrder.calculateWaitRepay();
        if(repayAmount.compareTo(totalAmount) > 0)ErrorCodeEnum.REPAY_GT_CAPITAL.throwException();
        //质押记录
        BorrowPledgeRecord pledgeRecord = BorrowPledgeRecord.builder()
                .orderId(borrowCoinOrder.getId())
                .coin(currencyCoin.getName())
                .type(BorrowPledgeType.REDUCE)
                .pledgeTime(LocalDateTime.now())
                .createTime(LocalDateTime.now()).build();
        //还款记录
        BorrowRepayRecord repayRecord = BorrowRepayRecord.builder()
                .orderId(borrowCoinOrder.getId())
                .coin(currencyCoin.getName())
                .repayAmount(repayAmount)
                .type(BorrowRepayType.NORMAL_REPAYMENT)
                .status(BorrowOrderStatus.SUCCESSFUL_REPAYMENT)
                .repayTime(LocalDateTime.now())
                .createTime(LocalDateTime.now()).build();

        //修改借币订单
        BigDecimal releasePledgeAmount=BigDecimal.ZERO;

        //质押币配置
        BorrowPledgeCoinConfig pledgeCoinConfig = borrowPledgeCoinConfigService.getByCoin(currencyCoin);
        BigDecimal liquidationPledgeRate = pledgeCoinConfig.getLiquidationPledgeRate();
        BigDecimal warnPledgeRate = pledgeCoinConfig.getWarnPledgeRate();
        if(repayAmount.compareTo(totalAmount) == 0){
            //全部还款
            releasePledgeAmount = borrowCoinOrder.getPledgeAmount();
            //借币质押记录
            pledgeRecord.setAmount(borrowCoinOrder.getPledgeAmount().negate());
            borrowPledgeRecordMapper.insert(pledgeRecord);
            borrowCoinOrder.setRepayAmount(borrowCoinOrder.getRepayAmount().add(repayAmount));
            borrowCoinOrder.setWaitRepayCapital(BigDecimal.ZERO);
            borrowCoinOrder.setWaitRepayInterest(BigDecimal.ZERO);
            borrowCoinOrder.setPledgeAmount(BigDecimal.ZERO);
            borrowCoinOrder.setPledgeRate(BigDecimal.ZERO);
            borrowCoinOrder.setPledgeStatus(BorrowOrderPledgeStatus.SAFE_PLEDGE);
            borrowCoinOrder.setSettlementTime(LocalDateTime.now());
            borrowCoinOrder.setStatus(BorrowOrderStatus.SUCCESSFUL_REPAYMENT);
            borrowCoinOrder.setBorrowDuration(DateUtil.between(TimeTool.toDate(borrowCoinOrder.getCreateTime()) ,TimeTool.toDate(borrowCoinOrder.getSettlementTime()),DateUnit.HOUR));
            borrowCoinOrderMapper.updateById(borrowCoinOrder);
            //还款记录
            repayRecord.setRepayCapital(waitRepayCapital);
            repayRecord.setRepayInterest(waitRepayInterest);
            repayRecord.setStatus(BorrowRepayStatus.SUCCESSFUL_REPAYMENT);
            repayRecord.setReleasePledgeAmount(borrowCoinOrder.getPledgeAmount());
            //统计每日计息订单
            borrowOrderNumDailyService.statisticalOrderNum();
        }else {
            //部分还款
            BigDecimal initialPledgeRate = pledgeCoinConfig.getInitialPledgeRate();
            BigDecimal pledgeAmount = borrowCoinOrder.getPledgeAmount();
            if(repayAmount.compareTo(waitRepayInterest) <= 0){
                waitRepayInterest = waitRepayInterest.subtract(repayAmount);
                //还款记录
                repayRecord.setRepayCapital(BigDecimal.ZERO);
                repayRecord.setRepayInterest(repayAmount);
            }else {
                waitRepayCapital = waitRepayCapital.subtract(repayAmount.subtract(waitRepayInterest));
                //还款记录
                repayRecord.setRepayCapital(repayAmount.subtract(waitRepayInterest));
                repayRecord.setRepayInterest(waitRepayInterest);
                waitRepayInterest= BigDecimal.ZERO;
            }
            //计算质押率
            BigDecimal waitRepayAmount = waitRepayCapital.add(waitRepayInterest);
            BigDecimal pledgeRate = waitRepayAmount.divide(pledgeAmount,8,RoundingMode.UP);
            if(pledgeRate.compareTo(initialPledgeRate) < 0){
                //释放质押物
                pledgeRate = initialPledgeRate;
                //初始质押率计算
                BigDecimal currPledgeAmount = waitRepayAmount.divide(initialPledgeRate, 8, RoundingMode.UP);
                releasePledgeAmount = pledgeAmount.subtract(currPledgeAmount);
                //借币质押记录
                pledgeRecord.setAmount(releasePledgeAmount.negate());
                borrowPledgeRecordMapper.insert(pledgeRecord);
                //修改借币订单
                borrowCoinOrder.setRepayAmount(borrowCoinOrder.getRepayAmount().add(repayAmount));
                borrowCoinOrder.setWaitRepayCapital(waitRepayCapital);
                borrowCoinOrder.setWaitRepayInterest(waitRepayInterest);
                borrowCoinOrder.setPledgeAmount(currPledgeAmount);
            }else {
                borrowCoinOrder.setRepayAmount(borrowCoinOrder.getRepayAmount().add(repayAmount));
                borrowCoinOrder.setWaitRepayCapital(waitRepayCapital);
                borrowCoinOrder.setWaitRepayInterest(waitRepayInterest);
                borrowCoinOrder.setPledgeAmount(pledgeAmount);
            }
            borrowCoinOrder.setPledgeRate(pledgeRate);
            if(pledgeRate.compareTo(warnPledgeRate) < 0 ){
                borrowCoinOrder.setPledgeStatus(BorrowOrderPledgeStatus.SAFE_PLEDGE);
            }else if (pledgeRate.compareTo(liquidationPledgeRate) < 0 ){
                borrowCoinOrder.setPledgeStatus(BorrowOrderPledgeStatus.WARN_PLEDGE);
            }
            borrowCoinOrderMapper.updateById(borrowCoinOrder);
        }
        //还款记录
        repayRecord.setReleasePledgeAmount(releasePledgeAmount);
        borrowRepayRecordMapper.insert(repayRecord);
        //减少余额
        repayCoin(uid,orderId,currencyCoin,repayAmount);
        //释放质押
        if(Objects.nonNull(releasePledgeAmount)) releasePledge(uid,orderId,currencyCoin,releasePledgeAmount);
    }

    @Override
    public BorrowAdjustPageVO adjustPage(Long orderId,Integer pledgeType,BigDecimal adjustAmount,CurrencyCoin coin) {

        Long uid = requestInitService.uid();
        //校验订单
        BorrowCoinOrder borrowCoinOrder = borrowCoinOrderMapper.selectById(orderId);
        if(Objects.isNull(borrowCoinOrder))ErrorCodeEnum.BORROW_ORDER_NO_EXIST.throwException();
        //初始质押率
        BorrowPledgeCoinConfig pledgeCoinConfig = borrowPledgeCoinConfigService.getByCoin(coin);
        BigDecimal initialPledgeRate = pledgeCoinConfig.getInitialPledgeRate();
        //质押数额
        BigDecimal pledgeAmount = borrowCoinOrder.getPledgeAmount();
        //待借款
        BigDecimal waitRepay = borrowCoinOrder.calculateWaitRepay();
        BorrowAdjustPageVO  borrowAdjustPageVO = new BorrowAdjustPageVO();
        borrowAdjustPageVO.setPledgeRate(borrowCoinOrder.getPledgeRate());
        if(pledgeType.equals(BorrowPledgeType.INCREASE)){
            //增加质押
            AccountBalance accountBalance = accountBalanceService.getAndInit(uid, coin);
            BigDecimal availableAmount = accountBalance.getRemain();
            if(adjustAmount.compareTo(availableAmount)>0){
                borrowAdjustPageVO.setAvailableAmount(availableAmount);
                borrowAdjustPageVO.setAdjustPledgeRate(waitRepay.divide((pledgeAmount.add(availableAmount)),8,RoundingMode.UP));
            }else {
                borrowAdjustPageVO.setAvailableAmount(availableAmount);
                borrowAdjustPageVO.setAdjustPledgeRate(waitRepay.divide((pledgeAmount.add(adjustAmount)), 8, RoundingMode.UP));
            }
        }else {
            //减少质押
            BigDecimal pledgeRateAmount = waitRepay.divide(initialPledgeRate, 8, RoundingMode.UP);
            BigDecimal ableReduceAmount = borrowCoinOrder.getPledgeAmount().subtract(pledgeRateAmount);
            if(adjustAmount.compareTo(ableReduceAmount) > 0){
                borrowAdjustPageVO.setAbleReduceAmount(BigDecimal.ZERO);
                borrowAdjustPageVO.setAdjustPledgeRate(borrowCoinOrder.getPledgeRate());
            }else {
                if (ableReduceAmount.compareTo(BigDecimal.ZERO) <= 0) {
                    borrowAdjustPageVO.setAbleReduceAmount(BigDecimal.ZERO);
                    borrowAdjustPageVO.setAdjustPledgeRate(borrowCoinOrder.getPledgeRate());
                } else {
                    borrowAdjustPageVO.setAbleReduceAmount(ableReduceAmount);
                    borrowAdjustPageVO.setAdjustPledgeRate(waitRepay.divide((pledgeAmount.subtract(adjustAmount)), 8, RoundingMode.UP));
                }
            }
        }
        return borrowAdjustPageVO;
    }

    @Override
    public void adjustPledge(AdjustPledgeBO bo) {
        BigDecimal adjustAmount = bo.getAdjustAmount();
        Integer pledgeType = bo.getPledgeType();
        Long orderId = bo.getOrderId();
        CurrencyCoin currencyCoin = bo.getCoin();
        Long uid = requestInitService.uid();
        //校验订单
        BorrowCoinOrder borrowCoinOrder = borrowCoinOrderMapper.selectById(orderId);
        if(Objects.isNull(borrowCoinOrder))ErrorCodeEnum.BORROW_ORDER_NO_EXIST.throwException();
        if(!borrowCoinOrder.getStatus().equals(BorrowOrderStatus.INTEREST_ACCRUAL))ErrorCodeEnum.BORROW_ORDER_STATUS_ERROR.throwException();
        //校验币种
        if(!borrowCoinOrder.getBorrowCoin().equals(currencyCoin.getName()))ErrorCodeEnum.CURRENCY_COIN_ERROR.throwException();
        //初始质押率
        BorrowPledgeCoinConfig pledgeCoinConfig = borrowPledgeCoinConfigService.getByCoin(bo.getCoin());
        BigDecimal initialPledgeRate = pledgeCoinConfig.getInitialPledgeRate();
        BigDecimal warnPledgeRate = pledgeCoinConfig.getWarnPledgeRate();
        BigDecimal liquidationPledgeRate = pledgeCoinConfig.getLiquidationPledgeRate();
        BigDecimal pledgeAmount = borrowCoinOrder.getPledgeAmount();
        BigDecimal waitRepayCapital = borrowCoinOrder.getWaitRepayCapital();
        BigDecimal waitRepayInterest = borrowCoinOrder.getWaitRepayInterest();
        BigDecimal totalWaitRepay = waitRepayCapital.add(waitRepayInterest);

        //质押记录
        BorrowPledgeRecord pledgeRecord = BorrowPledgeRecord.builder()
                .orderId(orderId)
                .coin(currencyCoin.getName())
                .pledgeTime(LocalDateTime.now())
                .createTime(LocalDateTime.now()).build();

        if(pledgeType.equals(BorrowPledgeType.INCREASE)){
            AccountBalance accountBalance = accountBalanceService.getAndInit(uid, currencyCoin);
            BigDecimal availableAmount = accountBalance.getRemain();
            if(adjustAmount.compareTo(availableAmount) > 0)ErrorCodeEnum.ADJUST_GT_AVAILABLE.throwException();

            pledgeAmount = pledgeAmount.add(adjustAmount);
            BigDecimal pledgeRate = totalWaitRepay.divide(pledgeAmount,8,RoundingMode.UP);
            //修改借币订单
            borrowCoinOrder.setPledgeRate(pledgeRate);
            borrowCoinOrder.setPledgeAmount(pledgeAmount);
            if(pledgeRate.compareTo(warnPledgeRate) < 0 ){
                borrowCoinOrder.setPledgeStatus(BorrowOrderPledgeStatus.SAFE_PLEDGE);
            }else if (pledgeRate.compareTo(liquidationPledgeRate) < 0 ){
                borrowCoinOrder.setPledgeStatus(BorrowOrderPledgeStatus.WARN_PLEDGE);
            }
            borrowCoinOrderMapper.updateById(borrowCoinOrder);
            //增加质押
            borrowPledge(uid,orderId,currencyCoin,adjustAmount);
            //质押记录
            pledgeRecord.setType(BorrowPledgeType.INCREASE);
            pledgeRecord.setAmount(adjustAmount);
        }else {
            if(adjustAmount.compareTo(pledgeAmount) > 0)ErrorCodeEnum.ADJUST_GT_AVAILABLE.throwException();
            pledgeAmount = pledgeAmount.subtract(adjustAmount);
            BigDecimal pledgeRate = totalWaitRepay.divide(pledgeAmount,8,RoundingMode.UP);
            if(pledgeRate.compareTo(initialPledgeRate) >0) ErrorCodeEnum.PLEDGE_RATE_RANGE_ERROR.throwException();
            //修改借币订单
            borrowCoinOrder.setPledgeRate(pledgeRate);
            borrowCoinOrder.setPledgeAmount(pledgeAmount);
            borrowCoinOrderMapper.updateById(borrowCoinOrder);
            //释放质押
            releasePledge(uid,orderId,currencyCoin,adjustAmount);
            //质押记录
            pledgeRecord.setType(BorrowPledgeType.REDUCE);
            pledgeRecord.setAmount(adjustAmount.negate());
        }
        //保存质押记录
        borrowPledgeRecordMapper.insert(pledgeRecord);

    }

    @Override
    public void forcedLiquidation(Long orderId) {
        //校验订单
        BorrowCoinOrder borrowCoinOrder = borrowCoinOrderMapper.selectById(orderId);
        if(Objects.isNull(borrowCoinOrder))ErrorCodeEnum.BORROW_ORDER_NO_EXIST.throwException();
        if(!borrowCoinOrder.getStatus().equals(BorrowOrderStatus.INTEREST_ACCRUAL))ErrorCodeEnum.BORROW_ORDER_STATUS_ERROR.throwException();
        CurrencyCoin currencyCoin = CurrencyCoin.getCurrencyCoinEnum(borrowCoinOrder.getPledgeCoin());
        BorrowPledgeCoinConfig pledgeCoinConfig = borrowPledgeCoinConfigService.getByCoin(currencyCoin);
        //校验质押率
        if(borrowCoinOrder.getPledgeRate().compareTo(pledgeCoinConfig.getLiquidationPledgeRate()) < 0 )ErrorCodeEnum.PLEDGE_LT_LIQUIDATION.throwException();
        //增加还款记录
        BorrowRepayRecord repayRecord = BorrowRepayRecord.builder()
                .orderId(orderId)
                .coin(borrowCoinOrder.getBorrowCoin())
                .repayAmount(borrowCoinOrder.getWaitRepayCapital().add(borrowCoinOrder.getWaitRepayInterest()))
                .repayCapital(borrowCoinOrder.getWaitRepayCapital())
                .repayInterest(borrowCoinOrder.getWaitRepayInterest())
                .releasePledgeAmount(BigDecimal.ZERO)
                .type(BorrowRepayType.FORCED_LIQUIDATION)
                .status(BorrowRepayStatus.SUCCESSFUL_REPAYMENT)
                .repayTime(LocalDateTime.now())
                .createTime(LocalDateTime.now())
                .build();
        borrowRepayRecordMapper.insert(repayRecord);
        //质押记录
        BorrowPledgeRecord borrowPledgeRecord = BorrowPledgeRecord.builder()
                .orderId(orderId)
                .coin(borrowCoinOrder.getPledgeCoin())
                .amount(borrowCoinOrder.getPledgeAmount().negate())
                .type(BorrowPledgeType.LIQUIDATION)
                .pledgeTime(LocalDateTime.now()).createTime(LocalDateTime.now()).build();
        borrowPledgeRecordMapper.insert(borrowPledgeRecord);
        //修改订单状态
        borrowCoinOrder.setWaitRepayCapital(BigDecimal.ZERO);
        borrowCoinOrder.setWaitRepayInterest(BigDecimal.ZERO);
        borrowCoinOrder.setPledgeAmount(BigDecimal.ZERO);
        borrowCoinOrder.setPledgeRate(BigDecimal.ZERO);
        borrowCoinOrder.setStatus(BorrowOrderStatus.FORCED_LIQUIDATION);
        borrowCoinOrder.setSettlementTime(LocalDateTime.now());
        borrowCoinOrder.setBorrowDuration(DateUtil.between(TimeTool.toDate(borrowCoinOrder.getCreateTime()) ,TimeTool.toDate(borrowCoinOrder.getSettlementTime()),DateUnit.HOUR));
        borrowCoinOrderMapper.updateById(borrowCoinOrder);
        //统计每日计息订单
        borrowOrderNumDailyService.statisticalOrderNum();
    }

    @Override
    public BorrowOrderStatisticsVO statistics() {
        BigDecimal borrowAmount = borrowCoinOrderMapper.selectBorrowCapitalSum();
        BigDecimal pledgeAmount = borrowCoinOrderMapper.selectPledgeAmountSum();
        BigDecimal interestAmount = borrowCoinOrderMapper.selectWaitRepayInterestSum();
        Integer orderNum = borrowCoinOrderMapper.selectCountByStatus(BorrowOrderStatus.INTEREST_ACCRUAL);
        return BorrowOrderStatisticsVO.builder()
                .borrowAmount(borrowAmount)
                .pledgeAmount(pledgeAmount)
                .interestAmount(interestAmount)
                .orderNum(orderNum).build();
    }

    @Override
    public List<BorrowOrderStatisticsChartVO> statisticsChart(BorrowStatisticsType statisticsType) {
        int offsetDay = -14;
        //获取14天前零点时间
        DateTime beginOfDay = DateUtil.beginOfDay(DateUtil.offsetDay(new Date(), offsetDay));
        //构建十四天的数据
        Map<String,BorrowOrderStatisticsChartVO> borrowOrderStatisticsChartVOMap = new LinkedHashMap<>();
        for(int i = offsetDay; i < 0; i++){
            String dateTime = DateUtil.format(DateUtil.offsetDay(new Date(), i), "yyyy-MM-dd");
            borrowOrderStatisticsChartVOMap.put(dateTime,new BorrowOrderStatisticsChartVO(dateTime,BigDecimal.ZERO));
        }

        List<BorrowOrderStatisticsChartVO> borrowOrderStatisticsChartVOS = null;
        switch (statisticsType){
            case borrow:{
                borrowOrderStatisticsChartVOS = borrowCoinOrderMapper.selectBorrowCapitalChartByTime(beginOfDay);
                break;
            }
            case pledge:{
                borrowOrderStatisticsChartVOS = borrowPledgeRecordMapper.selectAmountChartByTime(beginOfDay);
                break;
            }
            case interest:{
                borrowOrderStatisticsChartVOS = borrowInterestRecordMapper.selectInterestChartByTime(beginOfDay);
                break;
            }
            default:
                borrowOrderStatisticsChartVOS = borrowOrderNumDailyService.selectTotalChart(beginOfDay);
        }
        borrowOrderStatisticsChartVOS.forEach(item -> borrowOrderStatisticsChartVOMap.put(item.getTime(),item));
        return CollUtil.list(false,borrowOrderStatisticsChartVOMap.values()) ;
    }


    private void borrowPledge(Long uid, Long orderId, CurrencyCoin coin, BigDecimal pledgeAmount){
        Order order = Order.builder()
                .uid(uid)
                .coin(coin)
                .relatedId(orderId)
                .orderNo(AccountChangeType.borrow_pledge.getPrefix() + CommonFunction.generalSn(CommonFunction.generalId()))
                .amount(pledgeAmount)
                .type(ChargeType.pledge)
                .status(ChargeStatus.chain_success)
                .createTime(LocalDateTime.now())
                .build();
        orderService.save(order);
        accountBalanceService.decrease(uid,ChargeType.pledge,coin,null,pledgeAmount,order.getOrderNo()
                ,CurrencyLogDes.质押.name(), AccountOperationType.pledge);

    }

    private void borrowCoin(Long uid,Long orderId,CurrencyCoin coin, BigDecimal pledgeAmount){
        Order order = Order.builder()
                .uid(uid)
                .coin(coin)
                .relatedId(orderId)
                .orderNo(AccountChangeType.borrow.getPrefix() + CommonFunction.generalSn(CommonFunction.generalId()))
                .amount(pledgeAmount)
                .type(ChargeType.borrow)
                .status(ChargeStatus.chain_success)
                .createTime(LocalDateTime.now())
                .build();
        orderService.save(order);
        accountBalanceService.increase(uid, ChargeType.borrow,coin,null, pledgeAmount, order.getOrderNo()
                , CurrencyLogDes.借币.name(),AccountOperationType.borrow);
    }

    private void releasePledge(Long uid, Long orderId, CurrencyCoin coin, BigDecimal amount){
        Order releaseOrder = Order.builder()
                .uid(uid)
                .orderNo(AccountChangeType.release.getPrefix() + CommonFunction.generalSn(CommonFunction.generalId()))
                .completeTime(LocalDateTime.now())
                .amount(amount)
                .status(ChargeStatus.chain_success)
                .type(ChargeType.release)
                .coin(coin)
                .createTime(LocalDateTime.now())
                .relatedId(orderId)
                .build();
        orderService.save(releaseOrder);
        accountBalanceService.increase(uid, ChargeType.release, coin,null, amount, releaseOrder.getOrderNo()
                , CurrencyLogDes.释放质押.name(),AccountOperationType.release);
    }

    private void repayCoin(Long uid,Long orderId,CurrencyCoin coin, BigDecimal amount){
        Order order = Order.builder()
                .uid(uid)
                .orderNo(AccountChangeType.repay.getPrefix() + CommonFunction.generalSn(CommonFunction.generalId()))
                .completeTime(LocalDateTime.now())
                .amount(amount)
                .status(ChargeStatus.chain_success)
                .type(ChargeType.repay)
                .coin(coin)
                .createTime(LocalDateTime.now())
                .relatedId(orderId)
                .build();
        orderService.save(order);
        accountBalanceService.decrease(uid, ChargeType.repay, coin,null, amount, order.getOrderNo()
                , CurrencyLogDes.还币.name(),AccountOperationType.repay);
    }
}
