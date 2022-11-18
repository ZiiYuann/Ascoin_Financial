package com.tianli.task;

import cn.hutool.core.date.DateUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.base.MoreObjects;
import com.google.gson.Gson;
import com.tianli.account.enums.AccountChangeType;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.charge.entity.Order;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.service.OrderService;
import com.tianli.common.CommonFunction;
import com.tianli.common.RedisLockConstants;
import com.tianli.common.webhook.WebHookService;
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
import com.tianli.financial.query.PurchaseQuery;
import com.tianli.financial.service.*;
import com.tianli.financial.vo.FinancialPurchaseResultVO;
import com.tianli.tool.ApplicationContextTool;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
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
    @Resource
    private FinancialProductLadderRateService financialProductLadderRateService;
    @Resource
    private WebHookService webHookService;

    private static final ConcurrentHashMap<String, AtomicInteger> FAIL_COUNT_CACHE = new ConcurrentHashMap<>();

    //        @Scheduled(cron = "0 0/1 * * * ?")
    public void calIncomeTest() {
        log.info("========执行计算每日利息定时任务========");
        asyncService.async(() -> {
        });
    }

    /**
     * 计算利息
     */
    @Scheduled(cron = "0 0 0 1/1 * ? ")
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
                if (Objects.isNull(page)) {
                    return;
                }
                List<FinancialRecord> records = financialRecordService.needCalIncomeRecord(new Page<>(page, 20)).getRecords();

                if (CollectionUtils.isEmpty(records)) {
                    return;
                }

                FinancialIncomeTask task = SpringUtil.getBean(FinancialIncomeTask.class);

                records.forEach(financialRecord -> {
                    try {
                        task.incomeExternalTranscation(financialRecord);
                    } catch (Exception e) {
                        incomeCompensate(e, task, financialRecord);
                    }
                });

            }
        });
    }

    /**
     * 计算利息补偿
     */
    private void incomeCompensate(Exception e, FinancialIncomeTask task, FinancialRecord financialRecord) {

        if (e instanceof ErrCodeException) {
            webHookService.dingTalkSend("理财计算利息业务异常", e);
            return;
        }
        webHookService.dingTalkSend("理财计算利息异常", e);

        String toJson = gson.toJson(financialRecord);
        RetryScheduledExecutor.DEFAULT_EXECUTOR.schedule(() -> {
            String recordId = String.valueOf(financialRecord.getId());
            AtomicInteger atomicInteger = FAIL_COUNT_CACHE.getOrDefault(recordId, new AtomicInteger(3));
            FAIL_COUNT_CACHE.put(String.valueOf(financialRecord.getId()), atomicInteger);

            int andDecrement = atomicInteger.getAndDecrement();
            if (andDecrement > 0) {
                task.incomeExternalTranscation(financialRecord);
            } else {
                log.error("统计每日利息失败: record:{}", toJson);
                webHookService.dingTalkSend("理财计算利息多次失败，请处理！", new RuntimeException(toJson));
                FAIL_COUNT_CACHE.remove(recordId);
            }
        }, 30, TimeUnit.MINUTES, new RetryTaskInfo<>("incomeTask", "定时计息", financialRecord));
    }

    public void incomeExternalTranscation(FinancialRecord financialRecord) {
        incomeExternalTranscation(financialRecord, null);
    }

    /**
     * 统计每日利息
     */
    public void incomeExternalTranscation(FinancialRecord financialRecord, LocalDateTime incomeTime) {

        FinancialIncomeTask bean = ApplicationContextTool.getBean(FinancialIncomeTask.class);
        if (Objects.isNull(bean)) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }
        incomeTime = MoreObjects.firstNonNull(incomeTime, LocalDateTime.now());
        LocalDateTime todayZero = MoreObjects.firstNonNull(incomeTime, DateUtil.beginOfDay(new Date()).toLocalDateTime());
        HashMap<String, Order> orderMap = bean.incomeAndSettleTransaction(financialRecord, incomeTime);

        try {
            if (ProductType.fixed.equals(financialRecord.getProductType()) && financialRecord.getEndTime()
                    .compareTo(todayZero) == 0) {
                bean.renewalTransaction(financialRecord, orderMap.get("income"), orderMap.get("settle"), incomeTime);
            }
        } catch (Exception e) {
            webHookService.dingTalkSend(String.format("产品[%d]自动续费失败", financialRecord.getProductId()), e);
        }

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public HashMap<String, Order> incomeAndSettleTransaction(FinancialRecord financialRecord, LocalDateTime incomeTime) {
        HashMap<String, Order> result = new HashMap<>();
        ProductType type = financialRecord.getProductType();
        LocalDateTime endTime = financialRecord.getEndTime();
        LocalDateTime grantIncomeTime = financialRecord.getStartIncomeTime().plusDays(1);
        LocalDateTime incomeTimeZero = incomeTime.toLocalDate().atStartOfDay();

        if (financialRecord.getIncomeAmount().compareTo(BigDecimal.ZERO) == 0
                && financialRecord.getWaitAmount().compareTo(BigDecimal.ZERO) > 0) {
            financialRecordService.increaseIncomeAmount(financialRecord.getId(), financialRecord.getWaitAmount()
                    , financialRecord.getIncomeAmount());
            // 记录利息完毕后需要重新获取对象
            financialRecord = financialRecordService.getById(financialRecord.getId());
        }

        // 如果是定期产品且当前时间为到期前一天则计算利息
        if (ProductType.fixed.equals(type) && endTime.compareTo(incomeTimeZero) == 0) {
            var incomeOrder = incomeOperation(financialRecord, incomeTime);
            var settleOrder = settleOperation(financialRecord, incomeTime);
            // 对于自动续费操作来说，可能会有业务异常，不影响利息对发放
            result.put("income", incomeOrder);
            result.put("settle", settleOrder);
        }

        // 如果是活期产品需要当前时间 >= 收益发放时间
        if (ProductType.current.equals(type)) {
            // 记息金额为0 且 待记息金额 不为 0， 直接增加记息金额后返回
            if (incomeTimeZero.compareTo(grantIncomeTime) >= 0) {
                Order incomeOrder = incomeOperation(financialRecord, incomeTime);
                result.put("income", incomeOrder);
            }
        }

        return result;
    }


    /**
     * 自动续费操作
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void renewalTransaction(FinancialRecord financialRecord, Order incomeOrder, Order settleOrder, LocalDateTime now) {
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
                .relatedId(financialRecord.getId())
                .createTime(now.plusSeconds(1))
                .completeTime(now.plusSeconds(1))
                .build();
        orderService.save(order);

        PurchaseQuery purchaseQuery = new PurchaseQuery();
        purchaseQuery.setAmount(transferAmount);
        purchaseQuery.setProductId(product.getId());
        purchaseQuery.setCoin(product.getCoin());
        purchaseQuery.setTerm(product.getTerm());
        purchaseQuery.setAutoCurrent(false);
        financialProductService.purchase(financialRecord.getUid(), purchaseQuery, FinancialPurchaseResultVO.class, order);
    }

    /**
     * 结算操作
     */
    private Order settleOperation(FinancialRecord financialRecord, LocalDateTime incomeTime) {
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
                .createTime(incomeTime.plusSeconds(2))
                .completeTime(incomeTime.plusSeconds(2))
                .build();
        orderService.save(order);

        // 更新申购记录信息
        financialRecord.setEndTime(incomeTime.plusDays(2));
        financialRecord.setStatus(RecordStatus.SUCCESS);
        financialRecord.setHoldAmount(BigDecimal.ZERO);
        financialRecordService.updateById(financialRecord);

        // 增加
        accountBalanceService.increase(financialRecord.getUid(), ChargeType.settle, financialRecord.getCoin()
                , financialRecord.getHoldAmount(), order.getOrderNo(), CurrencyLogDes.结算.name());
        // 减少产品使用额度
        financialProductService.reduceUseQuota(financialRecord.getProductId(), financialRecord.getHoldAmount());
        return order;
    }

    /**
     * 收益操作
     */
    private Order incomeOperation(FinancialRecord financialRecord, LocalDateTime incomeTime) {

        FinancialProduct product = financialProductService.getById(financialRecord.getProductId());
        BigDecimal income = BigDecimal.ZERO;

        if (product.getRateType() == 0) {
            income = financialRecord.getIncomeAmount()
                    .multiply(financialRecord.getRate()) // 乘年化利率
                    .multiply(BigDecimal.valueOf(financialRecord.getProductTerm().getDay())) // 乘计息周期，活期默认为1
                    .divide(BigDecimal.valueOf(365), 8, RoundingMode.DOWN);
        }

        if (product.getRateType() == 1) {
            income = financialProductLadderRateService.calLadderIncome(financialRecord);
        }

        long id = CommonFunction.generalId();
        String orderNo = AccountChangeType.income.getPrefix() + CommonFunction.generalSn(id);

        Long uid = financialRecord.getUid();
        // 记录利息汇总
        financialIncomeAccrueService.insertIncomeAccrue(uid, financialRecord.getId(), financialRecord.getCoin(), income);
        // 记录昨日利息
        financialIncomeDailyService.insertIncomeDaily(uid, financialRecord.getId(), income
                , financialRecord.getIncomeAmount(), financialRecord.getRate(), orderNo, incomeTime);
        // 生成订单

        Order order = Order.builder()
                .id(id)
                .uid(financialRecord.getUid())
                .orderNo(orderNo)
                .type(ChargeType.income)
                .status(ChargeStatus.chain_success)
                .coin(financialRecord.getCoin())
                .amount(income)
                .createTime(incomeTime)
                .completeTime(incomeTime)
                .relatedId(financialRecord.getId()).build();
        orderService.save(order);

        // 操作余额
        accountBalanceService.increase(uid, ChargeType.income, financialRecord.getCoin()
                , income, order.getOrderNo(), CurrencyLogDes.收益.name());


        // 如果等待记息金额 大于 0 ，则计算完利息之后添加到 记息金额中
        if (financialRecord.getWaitAmount().compareTo(BigDecimal.ZERO) > 0) {
            financialRecordService.increaseIncomeAmount(financialRecord.getId(), financialRecord.getWaitAmount()
                    , financialRecord.getIncomeAmount());
        }
        return order;
    }

}
