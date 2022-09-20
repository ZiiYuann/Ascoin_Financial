package com.tianli.management.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tianli.exception.Result;
import com.tianli.fund.entity.FundIncomeRecord;
import com.tianli.fund.entity.FundRecord;
import com.tianli.fund.service.IFundIncomeRecordService;
import com.tianli.fund.service.IFundRecordService;
import com.tianli.management.query.FundIncomeTestQuery;
import com.tianli.mconfig.ConfigService;
import com.tianli.sso.permission.AdminPrivilege;
import com.tianli.sso.permission.Privilege;
import com.tianli.task.FundIncomeTask;
import com.tianli.tool.time.TimeTool;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    /**
     * 交易记录
     */
    @PutMapping("/fund/income")
    @AdminPrivilege(and = Privilege.基金管理)
    public Result transactionRecord(@RequestBody FundIncomeTestQuery query) {

        configService.get("taskTest");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nowZero = TimeTool.minDay(now);

        List<FundRecord> list = fundRecordService.list(new LambdaQueryWrapper<FundRecord>()
                .eq(FundRecord::getUid, query.getUid()));

        list.forEach(record -> {
            List<FundIncomeRecord> incomeRecords = Optional.ofNullable(fundIncomeRecordService.list(new LambdaQueryWrapper<FundIncomeRecord>()
                    .eq(FundIncomeRecord::getFundId, record.getId()))).orElse(new ArrayList<>());

            // 计息时间为4天后，所以手动修改为5天前
            record.setCreateTime(now.plusDays(-5));
            // 利息修改为前一天的时间
            for (int i = 0; i < incomeRecords.size(); i++) {
                FundIncomeRecord fundIncomeRecord = incomeRecords.get(i);
                fundIncomeRecord.setCreateTime(nowZero.plusDays(-(i + 1)));
            }

            fundRecordService.updateById(record);
            fundIncomeRecordService.updateBatchById(incomeRecords);

            fundIncomeTask.calculateIncome(record, now);
        });

        return Result.success();
    }

}
