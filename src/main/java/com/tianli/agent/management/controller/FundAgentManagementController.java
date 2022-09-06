package com.tianli.agent.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.agent.management.auth.AgentContent;
import com.tianli.agent.management.auth.AgentPrivilege;
import com.tianli.agent.management.bo.FundAuditBO;
import com.tianli.agent.management.query.FundStatisticsQuery;
import com.tianli.agent.management.service.FundAgentManageService;
import com.tianli.agent.management.vo.FundAuditRecordVO;
import com.tianli.agent.management.vo.FundProductStatisticsVO;
import com.tianli.agent.management.vo.HoldDataVO;
import com.tianli.agent.management.vo.TransactionDataVO;
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
import com.tianli.management.vo.FundIncomeAmountVO;
import com.tianli.management.vo.FundTransactionAmountVO;
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

    @GetMapping("/transaction/data")
    @AgentPrivilege
    public Result transactionData(FundStatisticsQuery query){
        TransactionDataVO transactionDataVO = fundAgentManageService.transactionData(query);
        return Result.success(transactionDataVO);
    }

    @GetMapping("/hold/data")
    @AgentPrivilege
    public Result holdData(){
        HoldDataVO holdDataVO = fundAgentManageService.holdData();
        return Result.success(holdDataVO);
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
        query.setAgentUId(AgentContent.getAgentUId());
        IPage<FundTransactionRecordVO> transactionPage = fundTransactionRecordService.getTransactionPage(page, query);
        return Result.success(transactionPage);
    }

    @GetMapping("/transaction/amount")
    @AgentPrivilege
    public Result transactionAmount(FundTransactionQuery query){
        query.setAgentUId(AgentContent.getAgentUId());
        FundTransactionAmountVO transactionAmount = fundTransactionRecordService.getTransactionAmount(query);
        return Result.success(transactionAmount);
    }

    @GetMapping("/income/record")
    @AgentPrivilege
    public Result incomeRecord(PageQuery<FundIncomeRecord> page , FundIncomeQuery query){
        query.setAgentUId(AgentContent.getAgentUId());
        fundIncomeRecordService.getPage(page,query);
        return Result.success();
    }

    @GetMapping("/income/amount")
    @AgentPrivilege
    public Result incomeAmount(FundIncomeQuery query){
        query.setAgentUId(AgentContent.getAgentUId());
        FundIncomeAmountVO incomeAmount = fundIncomeRecordService.getIncomeAmount(query);
        return Result.success(incomeAmount);
    }

    @GetMapping("/hold/record")
    @AgentPrivilege
    public Result holdRecord(PageQuery<FundRecord> pageQuery, FundRecordQuery query){
        query.setAgentUId(AgentContent.getAgentUId());
        IPage<FundUserRecordVO> fundUserRecordPage = fundRecordService.fundUserRecordPage(pageQuery, query);
        return Result.success(fundUserRecordPage);
    }

    @PostMapping("/redemption/audit/{ids}")
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
