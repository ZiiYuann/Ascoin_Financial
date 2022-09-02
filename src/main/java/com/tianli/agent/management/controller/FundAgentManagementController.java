package com.tianli.agent.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.agent.management.auth.AgentContent;
import com.tianli.agent.management.auth.AgentPrivilege;
import com.tianli.agent.management.bo.FundAuditBO;
import com.tianli.agent.management.query.FundStatisticsQuery;
import com.tianli.agent.management.service.FundAgentManageService;
import com.tianli.agent.management.vo.FundAuditRecordVO;
import com.tianli.agent.management.vo.FundProductStatisticsVO;
import com.tianli.agent.management.vo.MainPageVO;
import com.tianli.common.PageQuery;
import com.tianli.exception.Result;
import com.tianli.fund.entity.FundIncomeRecord;
import com.tianli.fund.entity.FundRecord;
import com.tianli.fund.entity.FundTransactionRecord;
import com.tianli.fund.query.FundIncomeQuery;
import com.tianli.fund.query.FundRecordQuery;
import com.tianli.fund.query.FundTransactionQuery;
import com.tianli.fund.service.IFundIncomeRecordService;
import com.tianli.fund.service.IFundRecordService;
import com.tianli.fund.service.IFundTransactionRecordService;
import com.tianli.fund.vo.FundTransactionRecordVO;
import com.tianli.management.entity.WalletAgentProduct;
import com.tianli.management.vo.FundUserRecordVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/agent/management/fund")
public class FundAgentManagementController {

    @Autowired
    private FundAgentManageService fundAgentManageService;

    @Autowired
    private IFundTransactionRecordService fundTransactionRecordService;

    @Autowired
    private IFundIncomeRecordService fundIncomeRecordService;

    @Autowired
    private IFundRecordService fundRecordService;

    @GetMapping("/statistics")
    @AgentPrivilege
    public Result statistics(FundStatisticsQuery query){
        MainPageVO statistics = fundAgentManageService.statistics(query);
        return Result.success(statistics);
    }

    @GetMapping("/product/statistics")
    @AgentPrivilege
    public Result productStatistics(PageQuery<WalletAgentProduct> page){
        IPage<FundProductStatisticsVO> statisticsPage = fundAgentManageService.productStatistics(page);
        return Result.success(statisticsPage);
    }

    @GetMapping("/transaction/record")
    @AgentPrivilege
    public Result transactionRecord(PageQuery<FundTransactionRecord> page , FundTransactionQuery query){
        query.setAgentId(AgentContent.getAgentId());
        IPage<FundTransactionRecordVO> transactionPage = fundTransactionRecordService.getTransactionPage(page, query);
        return Result.success(transactionPage);
    }

    @GetMapping("/income/record")
    @AgentPrivilege
    public Result incomeRecord(PageQuery<FundIncomeRecord> page , FundIncomeQuery query){
        query.setAgentId(AgentContent.getAgentId());
        fundIncomeRecordService.getPage(page,query);
        return Result.success();
    }

    @GetMapping("/hold/record")
    @AgentPrivilege
    public Result holdRecord(PageQuery<FundRecord> pageQuery, FundRecordQuery query){
        query.setAgentId(AgentContent.getAgentId());
        IPage<FundUserRecordVO> fundUserRecordPage = fundRecordService.fundUserRecordPage(pageQuery, query);
        return Result.success(fundUserRecordPage);
    }

    @PutMapping("/redemption/audit/{ids}")
    @AgentPrivilege
    public Result redemptionAudit(@RequestBody @Valid FundAuditBO bo){
        fundTransactionRecordService.redemptionAudit(bo);
        return Result.success();
    }

    @GetMapping("/redemption/audit/{id}")
    @AgentPrivilege
    public Result redemptionAuditRecord(@PathVariable Long id){
        FundAuditRecordVO auditRecord = fundTransactionRecordService.getRedemptionAuditRecord(id);
        return Result.success(auditRecord);
    }

    @PostMapping("/income/audit/{ids}")
    @AgentPrivilege
    public Result incomeAudit(@RequestBody @Valid FundAuditBO bo){

        return Result.success();
    }

    @PostMapping("/transaction/audit/{id}")
    @AgentPrivilege
    public Result incomeAuditRecord(@PathVariable Long id){

        return Result.success();
    }
}
