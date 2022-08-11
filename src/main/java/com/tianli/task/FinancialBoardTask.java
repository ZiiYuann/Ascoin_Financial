package com.tianli.task;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.tianli.address.AddressService;
import com.tianli.charge.service.OrderService;
import com.tianli.exception.ErrCodeException;
import com.tianli.financial.service.FinancialIncomeAccrueService;
import com.tianli.financial.service.FinancialRecordService;
import com.tianli.management.entity.FinancialBoardProduct;
import com.tianli.management.entity.FinancialBoardWallet;
import com.tianli.management.service.FinancialBoardProductService;
import com.tianli.management.service.FinancialBoardWalletService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 【管理端】理财看板数据定时器
 *
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

    //    @Scheduled(cron = "0 0/1 * * * ?")
    public void taskTest() {
        boardTask(DateUtil.beginOfDay(new Date()).toLocalDateTime().plusDays(0));
    }

    @Scheduled(cron = "0 0 0 1/1 * ? ")
    public void task() {
        try {
            boardTask(null);
        } catch (Exception e) {
            if (e instanceof ErrCodeException) {
                return;
            }
            RetryScheduledExecutor.DEFAULT_EXECUTOR.schedule(() -> {
                AtomicInteger atomicInteger = new AtomicInteger(3);

                int andDecrement = atomicInteger.getAndDecrement();
                if (andDecrement > 0) {
                    boardTask(null);
                } else {
                    log.error("执行计算管理端数据展板定时任务失败");
                }
            }, 30, TimeUnit.MINUTES, new RetryTaskInfo<>("boardTask", "执行计算管理端数据展板定时任务", null));
        }
    }

    @Transactional
    public void boardTask(LocalDateTime todayBegin) {
        todayBegin = Optional.ofNullable(todayBegin).orElse(DateUtil.beginOfDay(new DateTime()).toLocalDateTime());
        LocalDateTime yesterdayBegin = todayBegin.plusDays(-1);
        log.info("========执行计算管理端数据展板定时任务{}========", todayBegin);


        FinancialBoardProduct today = financialBoardProductService.getByDate(yesterdayBegin.toLocalDate());
        financialBoardProductService.getFinancialBoardProduct(yesterdayBegin,todayBegin , today);
        financialBoardProductService.updateById(today);

//        ========================== 云钱包数据看板 ==========================
        FinancialBoardWallet financialBoardWallet = financialBoardWalletService.getByDate(yesterdayBegin.toLocalDate());
        financialBoardWalletService.getFinancialBoardWallet(yesterdayBegin, todayBegin, financialBoardWallet);
        financialBoardWalletService.updateById(financialBoardWallet);
    }


}
