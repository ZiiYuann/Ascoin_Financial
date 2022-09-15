package com.tianli.agent.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.agent.management.auth.AgentContent;
import com.tianli.agent.management.auth.AgentPrivilege;
import com.tianli.agent.management.bo.FundAuditBO;
import com.tianli.agent.management.query.FundStatisticsQuery;
import com.tianli.agent.management.service.FundAgentManageService;
import com.tianli.agent.management.vo.FundReviewVO;
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
import com.tianli.management.vo.HoldUserAmount;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

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


    /**
     * 交易数据概览
     */
    @GetMapping("/transaction/data")
    @AgentPrivilege
    public Result transactionData(FundStatisticsQuery query){
        TransactionDataVO transactionDataVO = fundAgentManageService.transactionData(query);
        return Result.success(transactionDataVO);
    }

    /**
     * 持有数据概览
     */
    @GetMapping("/hold/data")
    @AgentPrivilege
    public Result holdData(FundStatisticsQuery query){
        HoldDataVO holdDataVO = fundAgentManageService.holdData(query);
        return Result.success(holdDataVO);
    }

    /**
     * 产品概览
     * @param page
     * @return
     */
    @GetMapping("/product/statistics")
    @AgentPrivilege
    public Result productStatistics(PageQuery<WalletAgentProduct> page,FundStatisticsQuery query){
        IPage<FundProductStatisticsVO> statisticsPage = fundAgentManageService.productStatistics(page,query);
        return Result.success(statisticsPage);
    }

    /**
     *交易记录
     */
    @GetMapping("/transaction/record")
    @AgentPrivilege
    public Result transactionRecord(PageQuery<FundTransactionRecord> page , FundTransactionQuery query){
        query.setAgentId(AgentContent.getAgentId());
        IPage<FundTransactionRecordVO> transactionPage = fundTransactionRecordService.getTransactionPage(page, query);
        return Result.success(transactionPage);
    }

    /**
     * 交易记录数据
     */
    @GetMapping("/transaction/amount")
    @AgentPrivilege
    public Result transactionAmount(FundTransactionQuery query){
        query.setAgentId(AgentContent.getAgentId());
        FundTransactionAmountVO transactionAmount = fundTransactionRecordService.getTransactionAmount(query);
        return Result.success(transactionAmount);
    }

    /**
     * 收益数据
     */
    @GetMapping("/income/record")
    @AgentPrivilege
    public Result incomeRecord(PageQuery<FundIncomeRecord> page , FundIncomeQuery query){
        query.setAgentId(AgentContent.getAgentId());
        fundIncomeRecordService.getPage(page,query);
        return Result.success();
    }

    /**
     *收益数据统计
     */
    @GetMapping("/income/amount")
    @AgentPrivilege
    public Result incomeAmount(FundIncomeQuery query){
        query.setAgentId(AgentContent.getAgentId());
        FundIncomeAmountVO incomeAmount = fundIncomeRecordService.getIncomeAmount(query);
        return Result.success(incomeAmount);
    }

    /**
     *持有记录
     */
    @GetMapping("/hold/record")
    @AgentPrivilege
    public Result holdRecord(PageQuery<FundRecord> pageQuery, FundRecordQuery query){
        query.setAgentId(AgentContent.getAgentId());
        IPage<FundUserRecordVO> fundUserRecordPage = fundRecordService.fundUserRecordPage(pageQuery, query);
        return Result.success(fundUserRecordPage);
    }

    /**
     *持有记录统计
     */
    @GetMapping("/hold/amount")
    @AgentPrivilege
    public Result holdAmount(FundRecordQuery query){
        query.setAgentId(AgentContent.getAgentId());
        HoldUserAmount holdUserAmount = fundRecordService.fundUserAmount(query);
        return Result.success(holdUserAmount);
    }

    /**
     * 赎回审核
     */
    @PostMapping("/redemption/audit")
    @AgentPrivilege
    public Result redemptionAudit(@RequestBody @Valid FundAuditBO bo){
        fundTransactionRecordService.redemptionAudit(bo);
        return Result.success();
    }

    /**
     *赎回审核记录
     */
    @GetMapping("/redemption/audit/{id}")
    @AgentPrivilege
    public Result redemptionAuditRecord(@PathVariable Long id){
        List<FundReviewVO> fundReviewVOS = fundTransactionRecordService.getRedemptionAuditRecord(id);
        return Result.success(fundReviewVOS);
    }

    /**
     * 收益审核
     */
    @PostMapping("/income/audit")
    @AgentPrivilege
    public Result incomeAudit(@RequestBody @Valid FundAuditBO bo){
        fundIncomeRecordService.incomeAudit(bo);
        return Result.success();
    }

    /**
     * 收益审核记录
     */
    @GetMapping("/income/audit/{id}")
    @AgentPrivilege
    public Result incomeAuditRecord(@PathVariable Long id){
        List<FundReviewVO> fundReviewVOS = fundIncomeRecordService.getIncomeAuditRecord(id);
        return Result.success(fundReviewVOS);
    }
}
