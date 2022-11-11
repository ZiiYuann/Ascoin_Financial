package com.tianli.management.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.base.MoreObjects;
import com.tianli.exception.Result;
import com.tianli.financial.entity.FinancialIncomeAccrue;
import com.tianli.financial.entity.FinancialIncomeDaily;
import com.tianli.financial.entity.FinancialRecord;
import com.tianli.financial.enums.ProductType;
import com.tianli.financial.enums.RecordStatus;
import com.tianli.financial.service.FinancialIncomeAccrueService;
import com.tianli.financial.service.FinancialIncomeDailyService;
import com.tianli.financial.service.FinancialRecordService;
import com.tianli.fund.entity.FundIncomeRecord;
import com.tianli.fund.entity.FundRecord;
import com.tianli.fund.service.IFundIncomeRecordService;
import com.tianli.fund.service.IFundRecordService;
import com.tianli.management.query.FundIncomeCompensateQuery;
import com.tianli.management.query.FundIncomeTestQuery;
import com.tianli.mconfig.ConfigService;
import com.tianli.task.FinancialIncomeTask;
import com.tianli.task.FundIncomeTask;
import com.tianli.tool.time.TimeTool;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author chenb
 * @apiNote
 * @since 2022-09-20
 **/
@RestController
@RequestMapping("/management/test")
public class TestController {

    @Resource
    private IFundRecordService fundRecordService;
    @Resource
    private IFundIncomeRecordService fundIncomeRecordService;
    @Resource
    private FundIncomeTask fundIncomeTask;
    @Resource
    private ConfigService configService;
    @Resource
    private FinancialIncomeTask financialIncomeTask;
    @Resource
    private FinancialRecordService financialRecordService;
    @Resource
    private FinancialIncomeDailyService financialIncomeDailyService;
    @Resource
    private FinancialIncomeAccrueService financialIncomeAccrueService;


    /**
     * 基金补偿
     */
    @PutMapping("/fund/compensate")
    public Result fundIncomeCompensate(@RequestBody FundIncomeCompensateQuery query) {
        fundIncomeTask.incomeCompensate(query);
        return Result.success();
    }

    /**
     * 交易记录
     */
    @PutMapping("/fund/income")
    public Result fundIncome(@RequestBody FundIncomeTestQuery query) {

        configService.get("taskTest");

        LocalDateTime now = MoreObjects.firstNonNull(query.getNow(), LocalDateTime.now());
        LocalDateTime nowZero = TimeTool.minDay(now);
        LambdaQueryWrapper<FundRecord> eq = new LambdaQueryWrapper<FundRecord>()
                .eq(FundRecord::getUid, query.getUid());
        if (Objects.nonNull(query.getRecordId())) {
            eq = eq.eq(FundRecord::getId, query.getRecordId());
        }
        List<FundRecord> list = fundRecordService.list(eq);
        list.forEach(record -> {
            List<FundIncomeRecord> incomeRecords = Optional.ofNullable(fundIncomeRecordService.list(new LambdaQueryWrapper<FundIncomeRecord>()
                            .eq(FundIncomeRecord::getFundId, record.getId())
                            .orderByDesc(FundIncomeRecord::getCreateTime)
                    )
            ).orElse(new ArrayList<>());

            if (CollectionUtils.isNotEmpty(incomeRecords)) {
                FundIncomeRecord fundIncomeRecordFirst = incomeRecords.get(0);
                if (fundIncomeRecordFirst.getCreateTime().equals(nowZero)) {
                    FundIncomeRecord fundIncomeRecordLast = incomeRecords.get(incomeRecords.size() - 1);
                    fundIncomeRecordFirst.setCreateTime(fundIncomeRecordLast.getCreateTime().plusDays(-1));
                    fundIncomeRecordService.updateById(fundIncomeRecordFirst);
                }
            }

            // 计息时间为4天后，所以手动修改为5天前
            record.setCreateTime(MoreObjects.firstNonNull(query.getCreateTime(), now.plusDays(-8)));
            // 利息修改为前一天的时间
            for (int i = 0; i < incomeRecords.size(); i++) {
                FundIncomeRecord fundIncomeRecord = incomeRecords.get(i);
                fundIncomeRecord.setCreateTime(nowZero.plusDays(-(i + 1)));
            }


            fundRecordService.updateById(record);

            fundIncomeTask.calculateIncome(record, now);
        });

        return Result.success();
    }

    /**
     * 交易记录
     */
    @PutMapping("/financial/income")
    public Result financialIncome(@RequestBody FundIncomeTestQuery query) {
        configService.get("taskTest");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nowZero = TimeTool.minDay(now);

        List<FinancialRecord> records = financialRecordService.selectList(query.getUid(), null, RecordStatus.PROCESS);

        if (Objects.nonNull(query.getRecordId())) {
            records = records.stream().filter(r -> query.getRecordId().equals(r.getId())).collect(Collectors.toList());
        }

        records.forEach(record -> {
            if (ProductType.fixed.equals(record.getProductType())) {
                record.setEndTime(nowZero);
                record.setPurchaseTime(nowZero.plusDays(-(record.getProductTerm().getDay() + 1)));
                record.setStartIncomeTime(nowZero.plusDays(-(record.getProductTerm().getDay())));
            }

            if (ProductType.current.equals(record.getProductType())) {
                record.setStartIncomeTime(nowZero.plusDays(-1));
            }

            financialRecordService.updateById(record);
            List<FinancialIncomeDaily> incomeDailies = Optional.of(financialIncomeDailyService.list(new LambdaQueryWrapper<FinancialIncomeDaily>()
                    .eq(FinancialIncomeDaily::getRecordId, record.getId()))).orElse(new ArrayList<>());

            for (int i = 0; i < incomeDailies.size(); i++) {
                FinancialIncomeDaily fundIncomeRecord = incomeDailies.get(i);
                fundIncomeRecord.setFinishTime(nowZero.plusDays(-(i + 2)));
            }
            financialIncomeDailyService.updateBatchById(incomeDailies);

            FinancialIncomeAccrue financialIncomeAccrue = financialIncomeAccrueService.getOne(new LambdaQueryWrapper<FinancialIncomeAccrue>()
                    .eq(FinancialIncomeAccrue::getRecordId, record.getId()));
            if (Objects.nonNull(financialIncomeAccrue)) {
                financialIncomeAccrue.setUpdateTime(nowZero.plusDays(-2));
                financialIncomeAccrueService.updateById(financialIncomeAccrue);
            }

            financialIncomeTask.incomeExternalTranscation(record);

        });

        return Result.success();
    }

    @PostMapping("/fund/income/rollback")
    public Result incomeRollback(Long incomeId) {
        fundIncomeRecordService.rollback(incomeId);
        return Result.success();
    }

}
