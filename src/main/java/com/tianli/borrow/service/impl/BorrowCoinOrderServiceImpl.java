package com.tianli.borrow.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.account.entity.AccountBalance;
import com.tianli.account.enums.AccountChangeType;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.borrow.contant.*;
import com.tianli.borrow.convert.BorrowCoinConfigConverter;
import com.tianli.borrow.convert.BorrowOrderConverter;
import com.tianli.borrow.dao.*;
import com.tianli.borrow.bo.AdjustPledgeBO;
import com.tianli.borrow.bo.BorrowOrderBO;
import com.tianli.borrow.bo.BorrowOrderRepayBO;
import com.tianli.borrow.entity.*;
import com.tianli.borrow.enums.BorrowStatisticsChartDay;
import com.tianli.borrow.enums.BorrowStatisticsType;
import com.tianli.borrow.query.BorrowInterestRecordQuery;
import com.tianli.borrow.query.BorrowOrderQuery;
import com.tianli.borrow.query.BorrowPledgeRecordQuery;
import com.tianli.borrow.query.BorrowRepayQuery;
import com.tianli.borrow.service.IBorrowCoinConfigService;
import com.tianli.borrow.service.IBorrowCoinOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import com.tianli.financial.entity.FinancialRecord;
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

    @Resource
    private BorrowCoinConfigConverter borrowCoinConfigConverter;

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
    private FinancialPledgeInfoMapper financialPledgeInfoMapper;

    @Autowired
    private IBorrowPledgeCoinConfigService  borrowPledgeCoinConfigService;

    @Override
    public BorrowCoinMainPageVO mainPage() {
        Long uid = requestInitService.uid();

        BigDecimal totalHoldAmount = financialRecordMapper.selectTotalHoldAmount();
        BigDecimal totalBorrowAmount = borrowCoinOrderMapper.selectTotalBorrowAmount();
        BigDecimal holdAmount = financialRecordMapper.selectHoldAmountByUid(uid);
        BigDecimal borrowAmount = borrowCoinOrderMapper.selectBorrowAmountByUid(uid);
        BigDecimal pledgeAmount = borrowCoinOrderMapper.selectPledgeAmountByUid(uid);

        List<BorrowCoinMainPageVO.BorrowOrder> borrowCoinOrders = borrowCoinOrderMapper.selectList(new QueryWrapper<BorrowCoinOrder>().lambda()
                .eq(BorrowCoinOrder::getUid, uid)
                .eq(BorrowCoinOrder::getStatus, BorrowOrderStatus.INTEREST_ACCRUAL))
                .stream().map(borrowConverter::toMainVO).collect(Collectors.toList());

        //借款额度
        BigDecimal borrowQuota = holdAmount.multiply(BorrowPledgeRate.INITIAL_PLEDGE_RATE);

        return BorrowCoinMainPageVO.builder()
                .totalDepositAmount(totalHoldAmount)
                .totalBorrowAmount(totalBorrowAmount)
                .depositAmount(holdAmount)
                .borrowAmount(borrowAmount)
                .pledgeAmount(pledgeAmount)
                .borrowQuota(borrowQuota)
                .borrowOrders(borrowCoinOrders)
                .borrowRate(borrowAmount.divide(borrowQuota, 8, RoundingMode.HALF_UP)).build();
    }

    @Override
    public IPage<BorrowCoinOrderVO> pageList(PageQuery<BorrowCoinOrder> pageQuery, BorrowOrderQuery query) {

        LambdaQueryWrapper<BorrowCoinOrder> queryWrapper = new QueryWrapper<BorrowCoinOrder>().lambda();
        if(Objects.nonNull(query.getUid())){
            queryWrapper.eq(BorrowCoinOrder::getUid, query.getUid());
        }

        if(Objects.nonNull(query.getStatus())){
            queryWrapper.in(BorrowCoinOrder::getStatus, query.getStatus());
        }

        if(Objects.nonNull(query.getQueryUid())){
            queryWrapper.like(BorrowCoinOrder::getUid,query.getQueryUid());
        }

        if(Objects.nonNull(query.getQueryOrderId())){
            queryWrapper.like(BorrowCoinOrder::getId,query.getQueryOrderId());
        }

        if(Objects.nonNull(query.getPledgeStatus())){
            queryWrapper.eq(BorrowCoinOrder::getPledgeStatus,query.getPledgeStatus());
        }

        if(Objects.nonNull(query.getMinPledgeRate())){
            queryWrapper.ge(BorrowCoinOrder::getPledgeRate,query.getMinPledgeRate());
        }

        if(Objects.nonNull(query.getMaxPledgeRate())){
            queryWrapper.le(BorrowCoinOrder::getPledgeRate,query.getMaxPledgeRate());
        }

        if(Objects.nonNull(query.getStartTime())){
            queryWrapper.ge(BorrowCoinOrder::getBorrowTime,query.getStartTime());
        }

        if(Objects.nonNull(query.getEndTime())){
            queryWrapper.le(BorrowCoinOrder::getBorrowTime,query.getEndTime());
        }
        queryWrapper.orderByDesc(BorrowCoinOrder::getBorrowTime);

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
        BorrowPledgeCoinConfig borrowPledgeCoinConfig = borrowPledgeCoinConfigService.getByCoin(coin);

        if(Objects.isNull(coinConfig)) ErrorCodeEnum.BORROW_CONFIG_NO_EXIST.throwException();

        BorrowApplePageVO borrowCoinConfigVO = borrowCoinConfigConverter.toVO(coinConfig);
        BigDecimal availableAmount = financialRecordMapper.selectAvailableAmountByUid(uid,coin);
        borrowCoinConfigVO.setAvailableAmount(availableAmount);
        borrowCoinConfigVO.setInitialPledgeRate(borrowPledgeCoinConfig.getInitialPledgeRate());
        borrowCoinConfigVO.setInitialPledgeRate(borrowPledgeCoinConfig.getInitialPledgeRate());
        return borrowCoinConfigVO;
    }

    @Override
    public void borrowCoin(BorrowOrderBO bo) {
        Long uid = requestInitService.uid();
        CurrencyCoin currencyCoin = bo.getCoin();

        BigDecimal borrowAmount = bo.getBorrowAmount();
        BorrowCoinConfig coinConfig = borrowCoinConfigService.getByCoin(currencyCoin);
        BorrowPledgeCoinConfig pledgeCoinConfig = borrowPledgeCoinConfigService.getByCoin(currencyCoin);

        //校验币别
        if(currencyCoin != CurrencyCoin.usdt && currencyCoin != CurrencyCoin.usdc) ErrorCodeEnum.CURRENCY_COIN_ERROR.throwException();

        //校验单笔最大和最小数量
        BigDecimal minimumBorrow = coinConfig.getMinimumBorrow();
        BigDecimal maximumBorrow = coinConfig.getMaximumBorrow();
        if(borrowAmount.compareTo(minimumBorrow) < 0 || borrowAmount.compareTo(maximumBorrow)>0)ErrorCodeEnum.BORROW_RANGE_ERROR.throwException();

        //校验数量
        BigDecimal initialPledgeRate = pledgeCoinConfig.getInitialPledgeRate();
        BigDecimal availableAmount = financialRecordMapper.selectAvailableAmountByUid(uid,currencyCoin);
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
                .repayAmount(borrowAmount)
                .pledgeCoin(currencyCoin.getName())
                .pledgeAmount(pledgeAmount)
                .pledgeRate(initialPledgeRate)
                .status(BorrowOrderStatus.INTEREST_ACCRUAL)
                .pledgeStatus(BorrowOrderPledgeStatus.SAFE_PLEDGE)
                .borrowTime(LocalDateTime.now())
                .createTime(LocalDateTime.now()).build();
        borrowCoinOrderMapper.insert(borrowCoinOrder);

        //锁定理财产品
        addPledgeAmount(uid,borrowCoinOrder.getId(),borrowCoinOrder.getBorrowCoin(), pledgeAmount);

        // 生成一笔订单记录(进行中)
        Order order = Order.builder()
                .uid(uid)
                .coin(currencyCoin)
                .relatedId(borrowCoinOrder.getId())
                .orderNo(AccountChangeType.borrow.getPrefix() + CommonFunction.generalSn(CommonFunction.generalId()))
                .amount(borrowAmount)
                .type(ChargeType.borrow)
                .status(ChargeStatus.created)
                .createTime(LocalDateTime.now())
                .build();
        orderService.save(order);
        //钱包增加余额
        accountBalanceService.increase(uid, ChargeType.recharge,currencyCoin,null, borrowAmount, order.getOrderNo()
                , CurrencyLogDes.借币.name());

        //添加质押记录
        BorrowPledgeRecord pledgeRecord = BorrowPledgeRecord.builder()
                .coin(currencyCoin.getName())
                .orderId(borrowCoinOrder.getId())
                .amount(pledgeAmount)
                .type(BorrowPledgeType.INIT)
                .pledgeTime(LocalDateTime.now())
                .createTime(LocalDateTime.now()).build();
        borrowPledgeRecordMapper.insert(pledgeRecord);

    }

    @Override
    public BorrowCoinOrderVO info(Long orderId) {
        BorrowCoinOrder borrowCoinOrder = borrowCoinOrderMapper.selectById(orderId);
        if(Objects.isNull(borrowCoinOrder))return new BorrowCoinOrderVO();
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
        BorrowRecordVO.Record record = new BorrowRecordVO.Record();
        record.setRecord("订单创建");
        record.setRecordEn("create order");
        record.setTime(borrowCoinOrder.getCreateTime());
        records.add(record);
        record = new BorrowRecordVO.Record();
        record.setRecord("借币中");
        record.setRecordEn("borrowing");
        record.setTime(LocalDateTime.now());
        records.add(record);
        record = new BorrowRecordVO.Record();
        records.add(record);
        record.setTime(borrowCoinOrder.getSettlementTime());
        long borrowDuration ;
        if(status.equals(BorrowOrderStatus.SUCCESSFUL_REPAYMENT)){
            record.setRecord("还款成功");
            record.setRecordEn("successful repayment");
            borrowDuration = DateUtil.between(TimeTool.toDate(borrowCoinOrder.getCreateTime()) ,TimeTool.toDate(borrowCoinOrder.getSettlementTime()), DateUnit.HOUR);
        }else if (status.equals(BorrowOrderStatus.FORCED_LIQUIDATION)){
            record.setRecord("已平仓");
            record.setRecordEn("liquidated");
            borrowDuration = DateUtil.between(TimeTool.toDate(borrowCoinOrder.getCreateTime()) ,TimeTool.toDate(borrowCoinOrder.getSettlementTime()), DateUnit.HOUR);
        }else {
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
        BigDecimal waitRepay = waitRepayInterest.add(waitRepayCapital);
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
            releasePledgeAmount = pledgeAmount;
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

        //数据校验
        AccountBalance accountBalance = accountBalanceService.getAndInit(uid, currencyCoin);
        if(accountBalance.getRemain().compareTo(repayAmount) <= 0)ErrorCodeEnum.INSUFFICIENT_BALANCE.throwException();
        BorrowCoinOrder borrowCoinOrder = borrowCoinOrderMapper.selectById(orderId);
        if(Objects.isNull(borrowCoinOrder))ErrorCodeEnum.BORROW_ORDER_NO_EXIST.throwException();
        if(!borrowCoinOrder.getBorrowCoin().equals(currencyCoin.getName()))ErrorCodeEnum.CURRENCY_COIN_ERROR.throwException();
        BigDecimal waitRepayCapital = borrowCoinOrder.getWaitRepayCapital();
        BigDecimal waitRepayInterest = borrowCoinOrder.getWaitRepayInterest();
        BigDecimal totalAmount = waitRepayCapital.add(waitRepayInterest);//总共需要还款金额
        if(repayAmount.compareTo(totalAmount) > 0)ErrorCodeEnum.REPAY_GT_CAPITAL.throwException();
        // 订单信息
        LocalDateTime now = LocalDateTime.now();
        Order order = Order.builder()
                .uid(uid)
                .orderNo(AccountChangeType.normal.getPrefix() + CommonFunction.generalSn(CommonFunction.generalId()))
                .completeTime(now)
                .amount(repayAmount.negate())
                .status(ChargeStatus.chain_success)
                .type(ChargeType.repay)
                .coin(currencyCoin)
                .createTime(now)
                .relatedId(borrowCoinOrder.getId())
                .build();
        orderService.save(order);
        //扣除钱包余额
        accountBalanceService.increase(uid, ChargeType.recharge, currencyCoin, repayAmount.negate(), order.getOrderNo()
                , CurrencyLogDes.还币.name());
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
                .repayTime(LocalDateTime.now())
                .createTime(LocalDateTime.now()).build();


        //修改借币订单
        if(repayAmount.compareTo(totalAmount) == 0){
            //全部还款
            //释放锁定数量
            reducePledgeAmount(orderId,borrowCoinOrder.getPledgeAmount());
            //借币质押记录
            pledgeRecord.setAmount(borrowCoinOrder.getPledgeAmount());
            borrowPledgeRecordMapper.insert(pledgeRecord);
            borrowCoinOrder.setRepayAmount(repayAmount);
            borrowCoinOrder.setWaitRepayCapital(BigDecimal.ZERO);
            borrowCoinOrder.setWaitRepayInterest(BigDecimal.ZERO);
            borrowCoinOrder.setPledgeAmount(BigDecimal.ZERO);
            borrowCoinOrder.setPledgeRate(BigDecimal.ZERO);
            borrowCoinOrder.setStatus(BorrowOrderStatus.SUCCESSFUL_REPAYMENT);
            borrowCoinOrder.setSettlementTime(LocalDateTime.now());
            borrowCoinOrder.setBorrowDuration(DateUtil.between(TimeTool.toDate(borrowCoinOrder.getCreateTime()) ,TimeTool.toDate(borrowCoinOrder.getSettlementTime()),DateUnit.HOUR));
            borrowCoinOrderMapper.updateById(borrowCoinOrder);
            //还款记录
            repayRecord.setRepayCapital(waitRepayCapital);
            repayRecord.setRepayInterest(waitRepayInterest);
            repayRecord.setStatus(BorrowRepayStatus.SUCCESSFUL_REPAYMENT);
            repayRecord.setReleasePledgeAmount(borrowCoinOrder.getPledgeAmount());
        }else {
            //部分还款
            BorrowPledgeCoinConfig pledgeCoinConfig = borrowPledgeCoinConfigService.getByCoin(currencyCoin);
            BigDecimal initialPledgeRate = pledgeCoinConfig.getInitialPledgeRate();
            BigDecimal pledgeAmount = borrowCoinOrder.getPledgeAmount();
            if(repayAmount.compareTo(waitRepayInterest) <= 0){
                waitRepayInterest = waitRepayInterest.subtract(repayAmount);
                //还款记录
                repayRecord.setRepayCapital(BigDecimal.ZERO);
                repayRecord.setRepayInterest(repayAmount);
            }else {
                waitRepayInterest = BigDecimal.ZERO;
                waitRepayCapital = waitRepayCapital.subtract(waitRepayInterest).subtract(repayAmount);

                //还款记录
                repayRecord.setRepayCapital(repayAmount.subtract(waitRepayInterest));
                repayRecord.setRepayInterest(waitRepayInterest);

            }
            repayRecord.setStatus(BorrowRepayStatus.REPAYMENT);
            //计算质押率
            BigDecimal totalWaitBorrowAmount = waitRepayCapital.add(waitRepayInterest);
            BigDecimal pledgeRate = totalWaitBorrowAmount.divide(pledgeAmount,8,RoundingMode.UP);
            if(pledgeRate.compareTo(initialPledgeRate) < 0){
                //释放质押物
                pledgeRate = initialPledgeRate;
                BigDecimal currPledgeAmount = totalWaitBorrowAmount.divide(pledgeRate, 4, RoundingMode.UP);
                BigDecimal reducePledgeAmount = currPledgeAmount.subtract(pledgeAmount);
                reducePledgeAmount(orderId,reducePledgeAmount);
                //还款记录
                repayRecord.setReleasePledgeAmount(borrowCoinOrder.getPledgeAmount());
                //借币质押记录
                pledgeRecord.setAmount(reducePledgeAmount);
                borrowPledgeRecordMapper.insert(pledgeRecord);
                //修改借币订单
                borrowCoinOrder.setRepayAmount(repayAmount);
                borrowCoinOrder.setWaitRepayCapital(waitRepayCapital);
                borrowCoinOrder.setWaitRepayInterest(waitRepayInterest);
                borrowCoinOrder.setPledgeAmount(currPledgeAmount);
            }else {
                borrowCoinOrder.setRepayAmount(repayAmount);
                borrowCoinOrder.setWaitRepayCapital(waitRepayCapital);
                borrowCoinOrder.setWaitRepayInterest(waitRepayInterest);
                borrowCoinOrder.setPledgeAmount(pledgeRate);
            }
            borrowCoinOrder.setPledgeRate(pledgeRate);
            borrowCoinOrderMapper.updateById(borrowCoinOrder);
        }
        //还款记录
        borrowRepayRecordMapper.insert(repayRecord);
    }

    @Override
    public BorrowAdjustPageVO adjustPage(Long orderId,Integer pledgeType,BigDecimal adjustAmount,CurrencyCoin coin) {
        Long uid = requestInitService.uid();
        BorrowCoinOrder borrowCoinOrder = borrowCoinOrderMapper.selectById(orderId);
        if(Objects.isNull(borrowCoinOrder))ErrorCodeEnum.BORROW_ORDER_NO_EXIST.throwException();
        BigDecimal waitRepayCapital = borrowCoinOrder.getWaitRepayCapital();
        BigDecimal waitRepayInterest = borrowCoinOrder.getWaitRepayInterest();
        BigDecimal pledgeAmount = borrowCoinOrder.getPledgeAmount();
        BigDecimal waitRepay = waitRepayCapital.add(waitRepayInterest);
        BorrowAdjustPageVO  borrowAdjustPageVO = new BorrowAdjustPageVO();
        borrowAdjustPageVO.setPledgeRate(borrowCoinOrder.getPledgeRate());
        if(pledgeType.equals(BorrowPledgeType.INCREASE)){
            BigDecimal availableAmount = financialRecordMapper.selectAvailableAmountByUid(uid, coin);
            if(adjustAmount.compareTo(availableAmount)>0)ErrorCodeEnum.ADJUST_GT_AVAILABLE.throwException();
            borrowAdjustPageVO.setAvailableAmount(availableAmount);
            borrowAdjustPageVO.setAdjustPledgeRate(waitRepay.divide((pledgeAmount.add(adjustAmount)),8,RoundingMode.UP));
        }else {
            BigDecimal pledgeRateAmount = waitRepay.divide(BigDecimal.valueOf(0.65), 8, RoundingMode.UP);
            BigDecimal ableReduceAmount = borrowCoinOrder.getPledgeAmount().subtract(pledgeRateAmount);
            if(ableReduceAmount.compareTo(BigDecimal.ZERO) <= 0){
                borrowAdjustPageVO.setAbleReduceAmount(BigDecimal.ZERO);
                borrowAdjustPageVO.setAdjustPledgeRate(borrowCoinOrder.getPledgeRate());
            }else {
                borrowAdjustPageVO.setAbleReduceAmount(ableReduceAmount);
                borrowAdjustPageVO.setAdjustPledgeRate(waitRepay.divide((pledgeAmount.subtract(adjustAmount)), 8, RoundingMode.UP));
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

        BorrowCoinOrder borrowCoinOrder = borrowCoinOrderMapper.selectById(orderId);
        if(Objects.isNull(borrowCoinOrder))ErrorCodeEnum.BORROW_ORDER_NO_EXIST.throwException();
        if(!borrowCoinOrder.getBorrowCoin().equals(currencyCoin.getName()))ErrorCodeEnum.CURRENCY_COIN_ERROR.throwException();

        BigDecimal pledgeAmount = borrowCoinOrder.getPledgeAmount();
        BigDecimal waitRepayCapital = borrowCoinOrder.getWaitRepayCapital();
        BigDecimal waitRepayInterest = borrowCoinOrder.getWaitRepayInterest();
        BigDecimal totalWaitRepay = waitRepayCapital.add(waitRepayInterest);



        //质押记录
        BorrowPledgeRecord pledgeRecord = BorrowPledgeRecord.builder()
                .orderId(orderId)
                .coin(currencyCoin.getName())
                .amount(adjustAmount)
                .pledgeTime(LocalDateTime.now())
                .createTime(LocalDateTime.now()).build();

        if(pledgeType.equals(BorrowPledgeType.INCREASE)){
            BigDecimal availableAmount = financialRecordMapper.selectAvailableAmountByUid(uid,currencyCoin);
            if(adjustAmount.compareTo(availableAmount) > 0)ErrorCodeEnum.ADJUST_GT_AVAILABLE.throwException();

            pledgeAmount = pledgeAmount.add(adjustAmount);
            BigDecimal pledgeRate = totalWaitRepay.divide(pledgeAmount,8,RoundingMode.UP);
            //修改借币订单
            borrowCoinOrder.setPledgeRate(pledgeRate);
            borrowCoinOrder.setPledgeAmount(pledgeAmount);
            borrowCoinOrderMapper.updateById(borrowCoinOrder);
            //调整锁定数额
            addPledgeAmount(uid,orderId,borrowCoinOrder.getBorrowCoin(),adjustAmount);
            //质押记录
            pledgeRecord.setType(BorrowPledgeType.INCREASE);

        }else {
            if(adjustAmount.compareTo(pledgeAmount) > 0)ErrorCodeEnum.ADJUST_GT_AVAILABLE.throwException();
            pledgeAmount = pledgeAmount.subtract(adjustAmount);
            BigDecimal pledgeRate = totalWaitRepay.divide(pledgeAmount,8,RoundingMode.UP);
            if(pledgeRate.compareTo(new BigDecimal("0.65")) >0) ErrorCodeEnum.PLEDGE_RATE_RANGE_ERROR.throwException();
            //修改借币订单
            borrowCoinOrder.setPledgeRate(pledgeRate);
            borrowCoinOrder.setPledgeAmount(pledgeAmount);
            borrowCoinOrderMapper.updateById(borrowCoinOrder);
            //调整锁定数额
            reducePledgeAmount(orderId,adjustAmount);
            //质押记录
            pledgeRecord.setType(BorrowPledgeType.REDUCE);
        }
        //保存质押记录
        borrowPledgeRecordMapper.insert(pledgeRecord);

    }

    @Override
    public void forcedLiquidation(Long orderId) {
        BorrowCoinOrder borrowCoinOrder = borrowCoinOrderMapper.selectById(orderId);
        if(Objects.isNull(borrowCoinOrder))ErrorCodeEnum.BORROW_ORDER_NO_EXIST.throwException();
        CurrencyCoin currencyCoin = CurrencyCoin.getCurrencyCoinEnum(borrowCoinOrder.getPledgeCoin());
        BorrowPledgeCoinConfig pledgeCoinConfig = borrowPledgeCoinConfigService.getByCoin(currencyCoin);
        if(borrowCoinOrder.getPledgeRate().compareTo(pledgeCoinConfig.getLiquidationPledgeRate()) < 0 )ErrorCodeEnum.PLEDGE_LT_LIQUIDATION.throwException();
        //修改订单状态
        borrowCoinOrder.setStatus(BorrowOrderStatus.FORCED_LIQUIDATION);
        borrowCoinOrder.setSettlementTime(LocalDateTime.now());
        borrowCoinOrderMapper.updateById(borrowCoinOrder);
        //释放锁定
        releasePledgeAmount(orderId);
    }

    @Override
    public BorrowOrderStatisticsVO statistics(BorrowStatisticsChartDay chartDay,Date startTime, Date endTime) {

        if(Objects.isNull(chartDay) && Objects.isNull(startTime) && Objects.isNull(endTime)){
            chartDay = BorrowStatisticsChartDay.day;
        }
        if(Objects.nonNull(chartDay)){
            endTime = null;
            if(chartDay == BorrowStatisticsChartDay.day){
                startTime = DateUtil.beginOfDay(new Date());
            }else if (chartDay == BorrowStatisticsChartDay.week){
                startTime = DateUtil.beginOfWeek(new Date());
            }else {
                startTime = DateUtil.beginOfMonth(new Date());
            }
        }
        BigDecimal borrowAmount = borrowCoinOrderMapper.selectBorrowCapitalSumByBorrowTime(startTime, endTime);
        BigDecimal pledgeAmount = borrowPledgeRecordMapper.selectAmountSumByTime(startTime, endTime);
        BigDecimal interestAmount = borrowInterestRecordMapper.selectInterestSumByQuery(BorrowInterestRecordQuery.builder().startTime(startTime).endTime(endTime).build());
        Integer orderNum = borrowCoinOrderMapper.selectCountByBorrowTime(BorrowOrderStatus.INTEREST_ACCRUAL,startTime, endTime);
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
        for(int i = offsetDay;i<=0;i++){
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
                borrowOrderStatisticsChartVOS = borrowCoinOrderMapper.selectTotalChartByTime(BorrowOrderStatus.INTEREST_ACCRUAL,beginOfDay);
        }
        borrowOrderStatisticsChartVOS.forEach(item -> borrowOrderStatisticsChartVOMap.put(item.getTime(),item));

        return CollUtil.list(false,borrowOrderStatisticsChartVOMap.values()) ;
    }

    /**
     * 锁定理财产品数量
     * @param uid 用户ID
     * @param addLockAmount 增加或减少的理财数量
     */
    private void lockFinancialRecordAmount(Long uid,BigDecimal addLockAmount){
        //查询用户所有产品
        List<FinancialRecord> financialRecords = financialRecordMapper.selectList(new QueryWrapper<FinancialRecord>().lambda()
                .eq(FinancialRecord::getUid, uid).orderByAsc(FinancialRecord::getPurchaseTime));
        //锁定总数
        BigDecimal totalLockAmount = financialRecords.stream().map(FinancialRecord::getPledgeAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add).add(addLockAmount);

        for(FinancialRecord financialRecord : financialRecords){
            BigDecimal holdAmount = financialRecord.getHoldAmount();
            BigDecimal lockAmount = BigDecimal.ZERO;
            if(!totalLockAmount.equals(BigDecimal.ZERO)){
                if(totalLockAmount.compareTo(holdAmount) <= 0){
                    lockAmount = totalLockAmount;
                    totalLockAmount = BigDecimal.ZERO;
                }else {
                    lockAmount = holdAmount;
                    totalLockAmount = totalLockAmount.subtract(lockAmount);
                }
            }
            financialRecord.setPledgeAmount(lockAmount);
            financialRecordMapper.updateById(financialRecord);
        }
    }

    /**
     * 理财产品增加质押数量
     * @param uid
     * @param orderId
     * @param amount
     */
    private void addPledgeAmount(Long uid,Long orderId,String coin, BigDecimal amount){
        //查询用户所有产品
        List<FinancialRecord> financialRecords = financialRecordMapper.selectList(new QueryWrapper<FinancialRecord>().lambda()
                .eq(FinancialRecord::getUid, uid)
                .eq(FinancialRecord::getCoin,coin)
                .orderByAsc(FinancialRecord::getPurchaseTime));
        for(FinancialRecord financialRecord : financialRecords){
            BigDecimal holdAmount = financialRecord.getHoldAmount();
            BigDecimal pledgeAmount = financialRecord.getPledgeAmount();
            BigDecimal availableAmount = holdAmount.subtract(pledgeAmount);
            if(availableAmount.compareTo(BigDecimal.ZERO) >0 && amount.compareTo(BigDecimal.ZERO) >0){
                BigDecimal currPledgeAmount;
                if(availableAmount.compareTo(amount) >= 0){
                    financialRecord.setPledgeAmount(pledgeAmount.add(amount));
                    currPledgeAmount = amount;
                    amount=BigDecimal.ZERO;
                }else {
                    amount = amount.subtract(availableAmount);
                    currPledgeAmount = availableAmount;
                    financialRecord.setPledgeAmount(holdAmount);
                }
                financialRecordMapper.updateById(financialRecord);
                FinancialPledgeInfo pledgeInfo = FinancialPledgeInfo.builder()
                        .uid(uid)
                        .financialId(financialRecord.getId())
                        .borrowOrderId(orderId)
                        .pledgeAmount(currPledgeAmount)
                        .createTime(new Date()).build();
                financialPledgeInfoMapper.insert(pledgeInfo);
            }
        }
    }

    /**
     * 理财产品减少质押数量
     * @param orderId
     * @param amount
     */
    private void reducePledgeAmount(Long orderId, BigDecimal amount){
        List<FinancialPledgeInfo> financialPledgeInfos = financialPledgeInfoMapper.selectList(
                new QueryWrapper<FinancialPledgeInfo>().lambda()
                        .eq(FinancialPledgeInfo::getBorrowOrderId,orderId)
                        .orderByDesc(FinancialPledgeInfo::getCreateTime));
        for (FinancialPledgeInfo info: financialPledgeInfos) {
            FinancialRecord financialRecord = financialRecordMapper.selectById(info.getFinancialId());
            BigDecimal pledgeAmount = info.getPledgeAmount();
            if(pledgeAmount.compareTo(amount) >= 0){
                info.setPledgeAmount(pledgeAmount.subtract(amount));
                if(info.getPledgeAmount().compareTo(BigDecimal.ZERO) == 0){
                    financialPledgeInfoMapper.deleteById(amount);
                }else {
                    financialPledgeInfoMapper.updateById(info);
                }
                financialRecordMapper.updateById(financialRecord.setPledgeAmount(financialRecord.getPledgeAmount().subtract(amount)));
                break;
            }else {
                financialPledgeInfoMapper.deleteById(info.getId());
                financialRecordMapper.updateById(financialRecord.setPledgeAmount(financialRecord.getPledgeAmount().subtract(amount.subtract(pledgeAmount))));
            }
        }
    }

    private void releasePledgeAmount(Long orderId){
        List<FinancialPledgeInfo> financialPledgeInfos = financialPledgeInfoMapper.selectList(
                new QueryWrapper<FinancialPledgeInfo>().lambda()
                    .eq(FinancialPledgeInfo::getBorrowOrderId,orderId).orderByDesc(FinancialPledgeInfo::getCreateTime));
        financialPledgeInfos.forEach(financialPledgeInfo -> {
            FinancialRecord financialRecord = financialRecordMapper.selectById(financialPledgeInfo.getFinancialId());
            financialRecord.setPledgeAmount(financialRecord.getPledgeAmount().subtract(financialPledgeInfo.getPledgeAmount()));
            financialRecordMapper.updateById(financialRecord);
            financialPledgeInfoMapper.deleteById(financialPledgeInfo.getId());
        });


    }

    private LambdaQueryWrapper<BorrowRepayRecord> getBorrowRepayRecordLambdaQueryWrapper(BorrowRepayQuery query) {
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
        return queryWrapper;
    }

}
