package com.tianli.task;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tianli.exception.ErrCodeException;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.management.entity.FinancialBoardProduct;
import com.tianli.management.entity.FinancialBoardWallet;
import com.tianli.management.service.FinancialBoardProductService;
import com.tianli.management.service.FinancialBoardWalletService;
import com.tianli.management.service.HotWalletDetailedService;
import com.tianli.management.service.ServiceFeeService;
import com.tianli.tool.ApplicationContextTool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private ServiceFeeService serviceFeeService;
    @Resource
    private FinancialBoardProductService financialBoardProductService;
    @Resource
    private FinancialBoardWalletService financialBoardWalletService;
    @Resource
    private HotWalletDetailedService hotWalletDetailedService;


    @Scheduled(cron = "0 0 0 1/1 * ? ")
    public void task() {
        FinancialBoardTask bean = ApplicationContextTool.getBean(FinancialBoardTask.class);
        FinancialBoardTask financialBoardTask = Optional.ofNullable(bean).orElseThrow(ErrorCodeEnum.SYSTEM_ERROR::generalException);
        try {
            financialBoardTask.boardTask(null);
        } catch (Exception e) {
            if (e instanceof ErrCodeException) {
                return;
            }
            RetryScheduledExecutor.DEFAULT_EXECUTOR.schedule(() -> {
                AtomicInteger atomicInteger = new AtomicInteger(3);
                int andDecrement = atomicInteger.getAndDecrement();
                if (andDecrement > 0) {
                    financialBoardTask.boardTask(null);
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
        LocalDate date = yesterdayBegin.toLocalDate();
        FinancialBoardProduct today = financialBoardProductService.getByDate(date);
        financialBoardProductService.getFinancialBoardProduct(yesterdayBegin, todayBegin, today);
        financialBoardProductService.update(today, new LambdaQueryWrapper<FinancialBoardProduct>()
                .eq(FinancialBoardProduct::getCreateTime, date));

//        ========================== 云钱包数据看板 ==========================
        FinancialBoardWallet financialBoardWallet = financialBoardWalletService.getByDate(date);
        financialBoardWalletService.getFinancialBoardWallet(yesterdayBegin, todayBegin, financialBoardWallet);
        financialBoardWalletService.update(financialBoardWallet, new LambdaQueryWrapper<FinancialBoardWallet>()
                .eq(FinancialBoardWallet::getCreateTime, date));

        // 提现数据展板
        serviceFeeService.init(todayBegin.toLocalDate(), null);

        // 热钱包余额数据
        financialBoardWalletService.setAssetsBoardCache(yesterdayBegin, todayBegin);
    }


}
