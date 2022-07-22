package com.tianli.borrow.service.impl;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.account.entity.AccountBalance;
import com.tianli.account.enums.AccountChangeType;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.borrow.contant.BorrowOrderStatus;
import com.tianli.borrow.contant.BorrowPledgeRate;
import com.tianli.borrow.contant.BorrowPledgeType;
import com.tianli.borrow.contant.BorrowRepayType;
import com.tianli.borrow.convert.BorrowCoinConfigConverter;
import com.tianli.borrow.convert.BorrowConverter;
import com.tianli.borrow.dao.*;
import com.tianli.borrow.bo.AdjustPledgeBO;
import com.tianli.borrow.bo.BorrowOrderBO;
import com.tianli.borrow.bo.BorrowOrderRepayBO;
import com.tianli.borrow.entity.*;
import com.tianli.borrow.query.BorrowOrderQuery;
import com.tianli.borrow.service.IBorrowCoinOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
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
    private BorrowConverter borrowConverter;

    @Resource
    private BorrowCoinConfigConverter borrowCoinConfigConverter;

    @Autowired
    private RequestInitService requestInitService;

    @Autowired
    private FinancialRecordMapper financialRecordMapper;

    @Autowired
    private BorrowCoinOrderMapper borrowCoinOrderMapper;

    @Autowired
    private BorrowCoinConfigMapper borrowCoinConfigMapper;

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
                .borrowRate(borrowAmount.divide(borrowQuota, 2, RoundingMode.HALF_UP)).build();
    }

    @Override
    public IPage<BorrowCoinOrderVO> pageList(PageQuery<BorrowCoinOrder> pageQuery, BorrowOrderQuery query) {
        Long uid = requestInitService.uid();
        Page<BorrowCoinOrder> borrowCoinOrderPage = borrowCoinOrderMapper.selectPage(pageQuery.page(), new QueryWrapper<BorrowCoinOrder>().lambda()
                .eq(BorrowCoinOrder::getUid, uid)
                .in(BorrowCoinOrder::getStatus, query.getOrderStatus()));
        return borrowCoinOrderPage.convert(borrowConverter::toVO);
    }

    @Override
    public BorrowCoinConfigVO config() {
        Long uid = requestInitService.uid();

        BorrowCoinConfig coinConfig = borrowCoinConfigMapper.selectOne(new QueryWrapper<BorrowCoinConfig>().lambda()
                .eq(BorrowCoinConfig::getCoin, CurrencyCoin.usdt.getName()));

        if(Objects.isNull(coinConfig)) ErrorCodeEnum.BORROW_CONFIG_NO_EXIST.throwException();

        BorrowCoinConfigVO borrowCoinConfigVO = borrowCoinConfigConverter.toVO(coinConfig);
        BigDecimal availableAmount = financialRecordMapper.selectAvailableAmountByUid(uid);
        borrowCoinConfigVO.setAvailableAmount(availableAmount);
        return borrowCoinConfigVO;
    }

    @Override
    public void borrowCoin(BorrowOrderBO bo) {
        Long uid = requestInitService.uid();
        CurrencyCoin currencyCoin = bo.getCurrencyCoin();

        BigDecimal borrowAmount = bo.getBorrowAmount();
        BorrowCoinConfig coinConfig = borrowCoinConfigMapper.selectOne(new QueryWrapper<BorrowCoinConfig>().lambda()
                .eq(BorrowCoinConfig::getCoin, CurrencyCoin.usdt.getName()));

        //校验币别
        if(currencyCoin != CurrencyCoin.usdt && currencyCoin != CurrencyCoin.usdc) ErrorCodeEnum.CURRENCY_COIN_ERROR.throwException();

        //校验单笔最大和最小数量
        BigDecimal minimumBorrow = coinConfig.getMinimumBorrow();
        BigDecimal maximumBorrow = coinConfig.getMaximumBorrow();
        if(borrowAmount.compareTo(minimumBorrow) < 0 || borrowAmount.compareTo(maximumBorrow)>0)ErrorCodeEnum.BORROW_GT_AVAILABLE_ERROR.throwException();

        //校验数量
        BigDecimal initialPledgeRate = coinConfig.getInitialPledgeRate();
        BigDecimal availableAmount = financialRecordMapper.selectAvailableAmountByUid(uid);
        if(borrowAmount.compareTo(availableAmount.multiply(initialPledgeRate)) > 0)ErrorCodeEnum.BORROW_GT_AVAILABLE_ERROR.throwException();

        //质押数量
        BigDecimal pledgeAmount = borrowAmount.divide(initialPledgeRate,2,RoundingMode.UP);

        //锁定理财产品
        lockFinancialRecordAmount(uid,pledgeAmount);

        //添加订单
        BorrowCoinOrder borrowCoinOrder = BorrowCoinOrder.builder()
                .uid(uid)
                .borrowCoin(currencyCoin.getName())
                .borrowCapital(borrowAmount)
                .waitRepayCapital(borrowAmount)
                .pledgeCoin(currencyCoin.getName())
                .pledgeAmount(pledgeAmount)
                .currentPledgeRate(initialPledgeRate)
                .status(BorrowOrderStatus.INTEREST_ACCRUAL)
                .borrowTime(new Date())
                .createTime(new Date()).build();
        borrowCoinOrderMapper.insert(borrowCoinOrder);

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
        orderService.saveOrder(order);
        //钱包增加余额
        accountBalanceService.increase(uid, ChargeType.recharge,currencyCoin,null, borrowAmount, order.getOrderNo()
                , CurrencyLogDes.借币.name());

        //添加质押记录
        BorrowPledgeRecord pledgeRecord = BorrowPledgeRecord.builder()
                .coin(currencyCoin.getName())
                .orderId(borrowCoinOrder.getId())
                .number(pledgeAmount)
                .type(BorrowPledgeType.INIT)
                .adjustmentTime(new Date())
                .createTime(new Date()).build();
        borrowPledgeRecordMapper.insert(pledgeRecord);

    }

    @Override
    public BorrowCoinOrderVO info(Long orderId) {
        BorrowCoinConfig coinConfig = borrowCoinConfigMapper.selectOne(new QueryWrapper<BorrowCoinConfig>().lambda()
                .eq(BorrowCoinConfig::getCoin, CurrencyCoin.usdt.getName()));
        BorrowCoinOrder borrowCoinOrder = borrowCoinOrderMapper.selectById(orderId);
        if(Objects.isNull(borrowCoinOrder))return new BorrowCoinOrderVO();
        BorrowCoinOrderVO borrowCoinOrderVO = borrowConverter.toVO(borrowCoinOrder);
        borrowCoinOrderVO.setCurrentPledgeRate(coinConfig.getAnnualInterestRate());
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
        record.setTime(borrowCoinOrder.getCreateTime());
        records.add(record);
        record = new BorrowRecordVO.Record();
        record.setRecord("借币中");
        record.setTime(new Date());
        records.add(record);
        record = new BorrowRecordVO.Record();
        records.add(record);
        record.setTime(borrowCoinOrder.getSettlementTime());
        long borrowDuration ;
        if(status.equals(BorrowOrderStatus.SUCCESSFUL_REPAYMENT)){
            record.setRecord("还款成功");
            borrowDuration = DateUtil.between(borrowCoinOrder.getCreateTime(),borrowCoinOrder.getSettlementTime(), DateUnit.HOUR);
        }else if (status.equals(BorrowOrderStatus.FORCED_LIQUIDATION)){
            record.setRecord("已平仓");
            borrowDuration = DateUtil.between(borrowCoinOrder.getCreateTime(),borrowCoinOrder.getSettlementTime(), DateUnit.HOUR);
        }else {
            borrowDuration = DateUtil.between(borrowCoinOrder.getCreateTime(),new Date(), DateUnit.HOUR);
        }
        recordVO.setRecords(records);
        recordVO.setBorrowDuration(borrowDuration);
        return recordVO;
    }

    @Override
    public List<BorrowPledgeRecordVO> pledgeRecord(Long orderId) {
        return borrowPledgeRecordMapper.selectList(new QueryWrapper<BorrowPledgeRecord>().lambda()
                .eq(BorrowPledgeRecord::getOrderId, orderId)).stream().map(borrowConverter::toPledgeVO).collect(Collectors.toList());
    }

    @Override
    public List<BorrowInterestRecordVO> interestRecord(Long orderId) {
        return borrowInterestRecordMapper.selectList(new QueryWrapper<BorrowInterestRecord>().lambda()
                .eq(BorrowInterestRecord::getOrderId,orderId)).stream().map(borrowConverter::toInterestVO).collect(Collectors.toList());
    }

    @Override
    public List<BorrowRepayRecordVO> repayRecord(Long orderId) {
        return borrowRepayRecordMapper.selectList(new QueryWrapper<BorrowRepayRecord>().lambda()
                .eq(BorrowRepayRecord::getOrderId,orderId)).stream().map(borrowConverter::toRepayVO).collect(Collectors.toList());
    }

    @Override
    public void orderRepay(BorrowOrderRepayBO bo) {
        Long uid = requestInitService.uid();
        Long orderId = bo.getOrderId();
        BigDecimal repayAmount = bo.getRepayAmount();
        CurrencyCoin currencyCoin = bo.getCurrencyCoin();

        //数据校验
        AccountBalance accountBalance = accountBalanceService.get(uid, currencyCoin);
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
        orderService.saveOrder(order);
        //扣除钱包余额
        accountBalanceService.increase(uid, ChargeType.recharge, currencyCoin, repayAmount.negate(), order.getOrderNo()
                , CurrencyLogDes.还币.name());
        //质押记录
        BorrowPledgeRecord pledgeRecord = BorrowPledgeRecord.builder()
                .orderId(borrowCoinOrder.getId())
                .coin(currencyCoin.getName())
                .type(BorrowPledgeType.REDUCE)
                .adjustmentTime(new Date())
                .createTime(new Date()).build();
        //还款记录
        BorrowRepayRecord repayRecord = BorrowRepayRecord.builder()
                .orderId(borrowCoinOrder.getId())
                .coin(currencyCoin.getName())
                .repayAmount(repayAmount)
                .type(BorrowRepayType.NORMAL_REPAYMENT)
                .repayTime(new Date())
                .createTime(new Date()).build();


        //修改借币订单
        if(repayAmount.compareTo(totalAmount) == 0){
            //释放锁定数量
            lockFinancialRecordAmount(uid,borrowCoinOrder.getPledgeAmount().negate());
            //借币质押记录
            pledgeRecord.setNumber(borrowCoinOrder.getPledgeAmount());
            borrowPledgeRecordMapper.insert(pledgeRecord);
            borrowCoinOrder.setRepayAmount(repayAmount);
            borrowCoinOrder.setWaitRepayCapital(BigDecimal.ZERO);
            borrowCoinOrder.setWaitRepayInterest(BigDecimal.ZERO);
            borrowCoinOrder.setPledgeAmount(BigDecimal.ZERO);
            borrowCoinOrder.setCurrentPledgeRate(BigDecimal.ZERO);
            borrowCoinOrder.setStatus(BorrowOrderStatus.SUCCESSFUL_REPAYMENT);
            borrowCoinOrder.setSettlementTime(new Date());
            borrowCoinOrder.setBorrowDuration(DateUtil.between(borrowCoinOrder.getCreateTime(),borrowCoinOrder.getSettlementTime(),DateUnit.HOUR));
            borrowCoinOrderMapper.updateById(borrowCoinOrder);
            //还款记录
            repayRecord.setRepayCapital(waitRepayCapital);
            repayRecord.setRepayInterest(waitRepayInterest);

        }else {
            BorrowCoinConfig coinConfig = borrowCoinConfigMapper.selectOne(new QueryWrapper<BorrowCoinConfig>().lambda()
                    .eq(BorrowCoinConfig::getCoin, currencyCoin.getName()));
            BigDecimal initialPledgeRate = coinConfig.getInitialPledgeRate();
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
            //计算质押率
            BigDecimal totalWaitBorrowAmount = waitRepayCapital.add(waitRepayInterest);
            BigDecimal pledgeRate = totalWaitBorrowAmount.divide(pledgeAmount,4,RoundingMode.UP);
            if(pledgeRate.compareTo(initialPledgeRate) < 0){
                //释放质押物
                pledgeRate = initialPledgeRate;
                BigDecimal currPledgeAmount = totalWaitBorrowAmount.divide(pledgeRate, 4, RoundingMode.UP);
                BigDecimal reducePledgeAmount = currPledgeAmount.subtract(pledgeAmount);
                lockFinancialRecordAmount(uid,reducePledgeAmount.negate());
                //借币质押记录
                pledgeRecord.setNumber(reducePledgeAmount);
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
            borrowCoinOrder.setCurrentPledgeRate(pledgeRate);
            borrowCoinOrderMapper.updateById(borrowCoinOrder);
        }
        //还款记录
        borrowRepayRecordMapper.insert(repayRecord);
    }

    @Override
    public void adjustPledge(AdjustPledgeBO bo) {
        BigDecimal adjustAmount = bo.getAdjustAmount();
        Integer pledgeType = bo.getPledgeType();
        Long orderId = bo.getOrderId();
        CurrencyCoin currencyCoin = bo.getCurrencyCoin();
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
                .number(adjustAmount)
                .adjustmentTime(new Date())
                .createTime(new Date()).build();

        if(pledgeType.equals(BorrowPledgeType.INCREASE)){
            BigDecimal availableAmount = financialRecordMapper.selectAvailableAmountByUid(uid);
            if(adjustAmount.compareTo(availableAmount) > 0)ErrorCodeEnum.ADJUST_GT_AVAILABLE.throwException();

            pledgeAmount = pledgeAmount.add(adjustAmount);
            BigDecimal pledgeRate = totalWaitRepay.divide(pledgeAmount,4,RoundingMode.UP);
            //修改借币订单
            borrowCoinOrder.setCurrentPledgeRate(pledgeRate);
            borrowCoinOrder.setPledgeAmount(pledgeAmount);
            borrowCoinOrderMapper.updateById(borrowCoinOrder);
            //调整锁定数额
            lockFinancialRecordAmount(uid,adjustAmount);
            //质押记录
            pledgeRecord.setType(BorrowPledgeType.INCREASE);

        }else {
            if(adjustAmount.compareTo(pledgeAmount) > 0)ErrorCodeEnum.ADJUST_GT_AVAILABLE.throwException();
            pledgeAmount = pledgeAmount.subtract(adjustAmount);
            BigDecimal pledgeRate = totalWaitRepay.divide(pledgeAmount,4,RoundingMode.UP);
            if(pledgeRate.compareTo(BigDecimal.ONE) >0) ErrorCodeEnum.PLEDGE_RATE_RANGE_ERROR.throwException();
            //修改借币订单
            borrowCoinOrder.setCurrentPledgeRate(pledgeRate);
            borrowCoinOrder.setPledgeAmount(pledgeAmount);
            borrowCoinOrderMapper.updateById(borrowCoinOrder);
            //调整锁定数额
            lockFinancialRecordAmount(uid,adjustAmount.negate());
            //质押记录
            pledgeRecord.setType(BorrowPledgeType.REDUCE);
        }
        //保存质押记录
        borrowPledgeRecordMapper.insert(pledgeRecord);

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
        BigDecimal totalLockAmount = financialRecords.stream().map(FinancialRecord::getLockAmount)
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
            financialRecord.setLockAmount(lockAmount);
            financialRecordMapper.updateById(financialRecord);
        }
    }
}
