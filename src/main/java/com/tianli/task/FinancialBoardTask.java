package com.tianli.task;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.tianli.address.AddressService;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.service.OrderService;
import com.tianli.financial.enums.ProductType;
import com.tianli.financial.service.FinancialIncomeAccrueService;
import com.tianli.financial.service.FinancialRecordService;
import com.tianli.management.entity.FinancialBoardProduct;
import com.tianli.management.entity.FinancialBoardWallet;
import com.tianli.management.service.FinancialBoardProductService;
import com.tianli.management.service.FinancialBoardWalletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * 【管理端】理财看板数据定时器
 * @author chenb
 * @apiNote
 * @since 2022-07-25
 **/
@Slf4j
@Component
public class FinancialBoardTask {

    @Resource
    private OrderService orderService;
    @Resource
    private FinancialBoardProductService financialBoardProductService;
    @Resource
    private FinancialBoardWalletService financialBoardWalletService;
    @Resource
    private FinancialIncomeAccrueService financialIncomeAccrueService;
    @Resource
    private FinancialRecordService financialRecordService;
    @Resource
    private AddressService addressService;

    private static final ScheduledThreadPoolExecutor CURRENCY_INTEREST_TASK_SCHEDULE_EXECUTOR = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
            r -> {
                Thread thread = new Thread(r);
                return thread;
            }
    );

//    @Scheduled(cron = "0 0/2 * * * ?")
    public void task(){
        LocalDateTime todayBegin = null;
        log.info("========执行计算管理端数据展板定时任务========");
        todayBegin = Optional.ofNullable(todayBegin).orElse(DateUtil.beginOfDay(new DateTime()).toLocalDateTime());
        LocalDateTime yesterdayBegin = todayBegin.plusDays(-1);

        FinancialBoardProduct today = financialBoardProductService.getToday();

        BigDecimal purchaseAmount = orderService.amountSumByCompleteTime(ChargeType.purchase,yesterdayBegin,todayBegin);
        BigDecimal redeemAmount = orderService.amountSumByCompleteTime(ChargeType.redeem,yesterdayBegin,todayBegin);
        BigDecimal settleAmount = orderService.amountSumByCompleteTime(ChargeType.settle,yesterdayBegin,todayBegin);
        BigDecimal transferAmount = orderService.amountSumByCompleteTime(ChargeType.transfer,yesterdayBegin,todayBegin);
        BigDecimal income = Optional.ofNullable(financialIncomeAccrueService.getAmountSum(todayBegin)).orElse(BigDecimal.ZERO);
        BigInteger fixedProductCount = financialRecordService.countProcess(ProductType.fixed);
        BigInteger currentProductCount = financialRecordService.countProcess(ProductType.current);
        BigInteger totalProductCount = currentProductCount.add(fixedProductCount);
        BigInteger holdUserCount =  financialRecordService.countUid();

        today.setPurchaseAmount(purchaseAmount);
        today.setRedeemAmount(redeemAmount);
        today.setSettleAmount(settleAmount);
        today.setTransferAmount(transferAmount);
        today.setIncome(income);
        today.setCurrentProductCount(currentProductCount);
        today.setFixedProductCount(fixedProductCount);
        today.setTotalProductCount(totalProductCount);
        today.setHoldUserCount(holdUserCount);
        financialBoardProductService.updateById(today);

        FinancialBoardWallet financialBoardWallet = financialBoardWalletService.getToday();
        BigDecimal rechargeAmount = orderService.amountSumByCompleteTime(ChargeType.recharge,yesterdayBegin,todayBegin);
        BigDecimal withdrawAmount = orderService.amountSumByCompleteTime(ChargeType.withdraw,yesterdayBegin,todayBegin);;
        BigInteger activeWalletCount = addressService.activeCount(yesterdayBegin,todayBegin);

    }


}
