package com.tianli.task;

import cn.hutool.core.date.DateUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.tianli.account.enums.AccountChangeType;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.charge.entity.Order;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.service.OrderService;
import com.tianli.common.CommonFunction;
import com.tianli.common.RedisLockConstants;
import com.tianli.common.async.AsyncService;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.financial.entity.FinancialIncomeAccrue;
import com.tianli.financial.entity.FinancialRecord;
import com.tianli.financial.enums.ProductType;
import com.tianli.financial.service.FinancialIncomeAccrueService;
import com.tianli.financial.service.FinancialIncomeDailyService;
import com.tianli.financial.service.FinancialRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Component
public class FinancialIncomeTask {

    @Resource
    private AccountBalanceService accountBalanceService;
    @Resource
    private FinancialRecordService financialRecordService;
    @Resource
    private FinancialIncomeAccrueService financialIncomeAccrueService;
    @Resource
    private FinancialIncomeDailyService financialIncomeDailyService;
    @Resource
    private Gson gson;
    @Resource
    private OrderService orderService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private AsyncService asyncService;

    private static final AtomicInteger threadId = new AtomicInteger(1);

    private static final ConcurrentHashMap<String, AtomicInteger> FAIL_COUNT_CACHE = new ConcurrentHashMap<>();

    private static final ScheduledThreadPoolExecutor CURRENCY_INTEREST_TASK_SCHEDULE_EXECUTOR = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
            r -> {
                Thread thread = new Thread(r);
                thread.setName("FinancialIncomeTask#calIncome-" + threadId.getAndIncrement());
                return thread;
            }
    );

    /**
     * 计算利息
     */
//    @Scheduled(cron = "0 0 0 1/1 * ? ")
    @Scheduled(cron = "0 0/2 * * * ?")
    public void calIncome() {
        log.info("========执行计算每日利息定时任务========");
        asyncService.async(() -> {
            LocalDateTime now = LocalDateTime.now();
            String day = String.format("%s_%s", now.getMonthValue(), now.getDayOfMonth());
            String redisKey = RedisLockConstants.FINANCIAL_INCOME_TASK + day;
            BoundValueOperations<String, Object> operation = redisTemplate.boundValueOps(redisKey);
            operation.setIfAbsent(0, 1, TimeUnit.HOURS);
            while (true) {
                Long page = operation.increment();
                List<FinancialRecord> records;
                if (page == null) {
                    records = new ArrayList<>();
                }else {
                    records = financialRecordService.needCalIncomeRecord(new Page<>(page, 20)).getRecords();
                }

                if ((records.size()) <= 0) {
                    break;
                }
                for (FinancialRecord c : records) {
                    FinancialIncomeTask task = SpringUtil.getBean(FinancialIncomeTask.class);
                    task.interestStat(c);
                }
            }
        });
    }

    /**
     * 统计每日利息
     */
    @Transactional
    public void interestStat(FinancialRecord financialRecord) {
        try {
            ProductType type = financialRecord.getProductType();
            LocalDateTime todayZero = DateUtil.beginOfDay(new Date()).toLocalDateTime();
            LocalDateTime endTime = financialRecord.getEndTime();
            LocalDateTime now = LocalDateTime.now();

            // 如果是定期产品且当前时间为到期前一天则计算利息
            if(ProductType.fixed.equals(type) && endTime.compareTo(todayZero) > 0 && todayZero.plusDays(1).compareTo(endTime) > 0){
                incomeOperation(financialRecord, now);
                settleOperation(financialRecord, now);
            }

            if(ProductType.current.equals(type)){
                incomeOperation(financialRecord, now);
            }


        } catch (Exception e) {
            String toJson = gson.toJson(financialRecord);
            log.warn("统计每日利息异常: record:{}", toJson, e);
            CURRENCY_INTEREST_TASK_SCHEDULE_EXECUTOR.schedule(() -> {
                AtomicInteger atomicInteger = FAIL_COUNT_CACHE.get(String.valueOf(financialRecord.getId()));
                if (Objects.isNull(atomicInteger)) {
                    atomicInteger = new AtomicInteger(3);
                    FAIL_COUNT_CACHE.put(String.valueOf(financialRecord.getId()), atomicInteger);
                }
                int andDecrement = atomicInteger.getAndDecrement();
                if (andDecrement > 0) {
                    interestStat(financialRecord);
                } else {
                    log.error("统计每日利息失败: record:{}", toJson);
                }
            }, 30, TimeUnit.MINUTES);
        }
    }

    private void settleOperation(FinancialRecord financialRecord, LocalDateTime now) {
        FinancialIncomeAccrue financialIncomeAccrue =
                financialIncomeAccrueService.selectByRecordId(financialRecord.getUid(), financialRecord.getId());
        long id = CommonFunction.generalId();
        Order order = Order.builder()
                .id(id)
                .orderNo(AccountChangeType.settle.getPrefix() + CommonFunction.generalSn(id))
                .type(ChargeType.settle)
                .status(ChargeStatus.chain_success)
                .coin(financialRecord.getCoin())
                // 结算金额为总收益
                .amount(financialIncomeAccrue.getIncomeAmount())
                .relatedId(financialRecord.getId())
                .createTime(now)
                .completeTime(now)
                .build();
        orderService.save(order);

        // 更新结算时间
        financialRecord.setEndTime(now);
        financialRecordService.updateById(financialRecord);


        // todo 自动续期操作
    }

    private void incomeOperation(FinancialRecord financialRecord, LocalDateTime now) {
        BigDecimal income = financialRecord.getHoldAmount()
                .multiply(financialRecord.getRate()) // 乘年化利率
                .multiply(BigDecimal.valueOf(financialRecord.getProductTerm().getDay())) // 乘计息周期，活期默认为1
                .divide(BigDecimal.valueOf(365),8, RoundingMode.HALF_DOWN);
        Long uid = financialRecord.getUid();
        // 记录利息汇总
        financialIncomeAccrueService.insertIncomeAccrue(uid, financialRecord.getId()
                , financialRecord.getCoin(),income);
        // 记录昨日利息
        financialIncomeDailyService.insertIncomeDaily(uid, financialRecord.getId()
                , income);
        // 生成订单
        long id = CommonFunction.generalId();
        Order order = Order.builder()
                .id(id)
                .uid(financialRecord.getUid())
                .orderNo(AccountChangeType.income.getPrefix() + CommonFunction.generalSn(id))
                .type(ChargeType.income)
                .status(ChargeStatus.chain_success)
                .coin(financialRecord.getCoin())
                .amount(income)
                .createTime(now)
                .completeTime(now)
                .relatedId(financialRecord.getId()).build();
        orderService.save(order);

        // 操作余额
        accountBalanceService.increase(uid,ChargeType.income, financialRecord.getCoin()
                ,income,order.getOrderNo(),CurrencyLogDes.收益.name());
    }

}
