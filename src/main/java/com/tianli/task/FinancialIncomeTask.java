package com.tianli.task;

import cn.hutool.core.date.DateUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
import com.tianli.exception.ErrCodeException;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.financial.entity.FinancialProduct;
import com.tianli.financial.entity.FinancialRecord;
import com.tianli.financial.enums.BusinessType;
import com.tianli.financial.enums.ProductStatus;
import com.tianli.financial.enums.ProductType;
import com.tianli.financial.enums.RecordStatus;
import com.tianli.financial.service.FinancialIncomeAccrueService;
import com.tianli.financial.service.FinancialIncomeDailyService;
import com.tianli.financial.service.FinancialProductService;
import com.tianli.financial.service.FinancialRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
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
    @Resource
    private FinancialProductService financialProductService;

    private static final ConcurrentHashMap<String, AtomicInteger> FAIL_COUNT_CACHE = new ConcurrentHashMap<>();

//        @Scheduled(cron = "0 0/1 * * * ?")
    public void calIncomeTest() {
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
                } else {
                    LambdaQueryWrapper<FinancialRecord> eq = new LambdaQueryWrapper<FinancialRecord>()
                            .eq(FinancialRecord::getId, 1740575276235094472L);
                    records = financialRecordService.list(eq);
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
     * 计算利息
     */
    @Scheduled(cron = "0 0 0 1/1 * ? ")
//    @Scheduled(cron = "0 0/2 * * * ?")
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
                } else {
                    records = financialRecordService.needCalIncomeRecord(new Page<>(page, 20)).getRecords();
                }

                if ((records.size()) <= 0) {
                    break;
                }
                for (FinancialRecord financialRecord : records) {
                    FinancialIncomeTask task = SpringUtil.getBean(FinancialIncomeTask.class);
                    try {
                        task.interestStat(financialRecord);
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (e instanceof ErrCodeException) {
                            return;
                        }
                        String toJson = gson.toJson(financialRecord);
                        RetryScheduledExecutor.DEFAULT_EXECUTOR.schedule(() -> {
                            String recordId = String.valueOf(financialRecord.getId());
                            AtomicInteger atomicInteger = FAIL_COUNT_CACHE.getOrDefault(recordId, new AtomicInteger(3));
                            FAIL_COUNT_CACHE.put(String.valueOf(financialRecord.getId()), atomicInteger);

                            int andDecrement = atomicInteger.getAndDecrement();
                            if (andDecrement > 0) {
                                interestStat(financialRecord);
                            } else {
                                log.error("统计每日利息失败: record:{}", toJson);
                                FAIL_COUNT_CACHE.remove(recordId);
                            }
                        }, 30, TimeUnit.MINUTES, new RetryTaskInfo<>("incomeTask", "定时计息", financialRecord));
                    }
                }
            }
        });
    }

    /**
     * 统计每日利息
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void interestStat(FinancialRecord financialRecord) {
        ProductType type = financialRecord.getProductType();
        LocalDateTime endTime = financialRecord.getEndTime();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime grantIncomeTime = financialRecord.getStartIncomeTime().plusDays(1);
        LocalDateTime todayZero = DateUtil.beginOfDay(new Date()).toLocalDateTime();


        // 如果是定期产品且当前时间为到期前一天则计算利息
        if (ProductType.fixed.equals(type) && endTime.compareTo(todayZero) == 0) {
            var incomeOrder = incomeOperation(financialRecord, now);
            var settleOrder = settleOperation(financialRecord, now);
            renewalOperation(financialRecord, incomeOrder, settleOrder,now);
        }

        // 如果是活期产品需要当前时间 >= 收益发放时间
        if (ProductType.current.equals(type) && todayZero.compareTo(grantIncomeTime) >= 0) {
            incomeOperation(financialRecord, now);
        }
    }

    /**
     * 自动续费操作
     */
    private void renewalOperation(FinancialRecord financialRecord, Order incomeOrder, Order settleOrder,LocalDateTime now) {
        if (!financialRecord.isAutoRenewal()) {
            return;
        }

        var query = new LambdaQueryWrapper<FinancialProduct>()
                .eq(FinancialProduct::getCoin, financialRecord.getCoin())
                .eq(FinancialProduct::getStatus, ProductStatus.open)
                .eq(FinancialProduct::isDeleted, false)
                .eq(FinancialProduct::getBusinessType, BusinessType.normal)
                .eq(FinancialProduct::getType, ProductType.current)
                .orderByDesc(FinancialProduct::getCreateTime);


        List<FinancialProduct> products = financialProductService.list(query);
        if (CollectionUtils.isEmpty(products)) {
            log.error("当前币别下不存在有效的活期产品用于自动续费");
            ErrorCodeEnum.SYSTEM_ERROR.throwException();
        }
        // 转存金额 = 收益金额 + 本金
        BigDecimal transferAmount = incomeOrder.getAmount().add(settleOrder.getAmount());

        // 取第一个有效的产品
        FinancialProduct product = products.get(0);
        FinancialRecord transferRecord = financialRecordService.generateFinancialRecord(financialRecord.getUid()
                , product, transferAmount, false);

        long id = CommonFunction.generalId();
        Order order = Order.builder()
                .id(id)
                .uid(financialRecord.getUid())
                .orderNo(AccountChangeType.transfer.getPrefix() + CommonFunction.generalSn(id))
                .type(ChargeType.transfer)
                .status(ChargeStatus.chain_success)
                .coin(financialRecord.getCoin())
                // 转存金额为 结算金额加收益金额
                .amount(transferAmount)
                .relatedId(transferRecord.getId())
                .createTime(now.plusSeconds(1))
                .completeTime(now.plusSeconds(1))
                .build();
        orderService.save(order);

        // 减少金额
        accountBalanceService.decrease(financialRecord.getUid(), ChargeType.purchase, product.getCoin(), transferAmount
                , order.getOrderNo(), "转存");
    }

    /**
     * 结算操作
     */
    private Order settleOperation(FinancialRecord financialRecord, LocalDateTime now) {
        long id = CommonFunction.generalId();
        Order order = Order.builder()
                .id(id)
                .uid(financialRecord.getUid())
                .orderNo(AccountChangeType.settle.getPrefix() + CommonFunction.generalSn(id))
                .type(ChargeType.settle)
                .status(ChargeStatus.chain_success)
                .coin(financialRecord.getCoin())
                // 结算金额为持有金额
                .amount(financialRecord.getHoldAmount())
                .relatedId(financialRecord.getId())
                .createTime(now)
                .completeTime(now)
                .build();
        orderService.save(order);

        // 更新申购记录信息
        financialRecord.setEndTime(now);
        financialRecord.setStatus(RecordStatus.SUCCESS);
        financialRecordService.updateById(financialRecord);

        // 增加
        accountBalanceService.increase(financialRecord.getUid(), ChargeType.settle, financialRecord.getCoin()
                , financialRecord.getHoldAmount(), order.getOrderNo(), CurrencyLogDes.结算.name());
        return order;
    }

    /**
     * 收益操作
     */
    private Order incomeOperation(FinancialRecord financialRecord, LocalDateTime now) {
        BigDecimal income = financialRecord.getHoldAmount()
                .multiply(financialRecord.getRate()) // 乘年化利率
                .multiply(BigDecimal.valueOf(financialRecord.getProductTerm().getDay())) // 乘计息周期，活期默认为1
                .divide(BigDecimal.valueOf(365), 8, RoundingMode.DOWN);
        Long uid = financialRecord.getUid();
        // 记录利息汇总
        financialIncomeAccrueService.insertIncomeAccrue(uid, financialRecord.getId()
                , financialRecord.getCoin(), income);
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
        accountBalanceService.increase(uid, ChargeType.income, financialRecord.getCoin()
                , income, order.getOrderNo(), CurrencyLogDes.收益.name());
        return order;
    }

}
