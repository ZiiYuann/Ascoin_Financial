package com.tianli.agent.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.agent.management.auth.AgentContent;
import com.tianli.agent.management.auth.AgentPrivilege;
import com.tianli.agent.management.bo.FundAuditBO;
import com.tianli.agent.management.query.FundStatisticsQuery;
import com.tianli.agent.management.service.FundAgentManageService;
import com.tianli.agent.management.vo.FundProductStatisticsVO;
import com.tianli.agent.management.vo.FundReviewVO;
import com.tianli.agent.management.vo.HoldDataVO;
import com.tianli.agent.management.vo.TransactionDataVO;
import com.tianli.common.PageQuery;
import com.tianli.common.RedisLockConstants;
import com.tianli.exception.Result;
import com.tianli.product.afund.entity.FundIncomeRecord;
import com.tianli.product.afund.entity.FundRecord;
import com.tianli.product.afund.entity.FundTransactionRecord;
import com.tianli.product.afund.query.FundIncomeQuery;
import com.tianli.product.afund.query.FundRecordQuery;
import com.tianli.product.afund.query.FundTransactionQuery;
import com.tianli.product.afund.service.IFundIncomeRecordService;
import com.tianli.product.afund.service.IFundRecordService;
import com.tianli.product.afund.service.IFundTransactionRecordService;
import com.tianli.product.afund.vo.FundIncomeRecordVO;
import com.tianli.product.afund.vo.FundTransactionRecordVO;
import com.tianli.management.entity.WalletAgentProduct;
import com.tianli.management.vo.FundIncomeAmountVO;
import com.tianli.management.vo.FundTransactionAmountVO;
import com.tianli.management.vo.FundUserRecordVO;
import com.tianli.management.vo.HoldUserAmount;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/agent/management/fund")
public class FundAgentManagementController {

    @Resource
    private FundAgentManageService fundAgentManageService;
    @Resource
    private IFundTransactionRecordService fundTransactionRecordService;
    @Resource
    private IFundIncomeRecordService fundIncomeRecordService;
    @Resource
    private IFundRecordService fundRecordService;
    @Resource
    private RedissonClient redissonClient;


    /**
     * 交易数据概览
     */
    @GetMapping("/transaction/data")
    @AgentPrivilege
    public Result<TransactionDataVO> transactionData(FundStatisticsQuery query) {
        TransactionDataVO transactionDataVO = fundAgentManageService.transactionData(query);
        return Result.success(transactionDataVO);
    }

    /**
     * 持有数据概览
     */
    @GetMapping("/hold/data")
    @AgentPrivilege
    public Result<HoldDataVO> holdData(FundStatisticsQuery query) {
        HoldDataVO holdDataVO = fundAgentManageService.holdData(query);
        return Result.success(holdDataVO);
    }

    /**
     * 产品概览
     */
    @GetMapping("/product/statistics")
    @AgentPrivilege
    public Result<IPage<FundProductStatisticsVO>> productStatistics(PageQuery<WalletAgentProduct> page, FundStatisticsQuery query) {
        IPage<FundProductStatisticsVO> statisticsPage = fundAgentManageService.productStatistics(page, query);
        return Result.success(statisticsPage);
    }

    /**
     * 交易记录
     */
    @GetMapping("/transaction/record")
    @AgentPrivilege
    public Result<IPage<FundTransactionRecordVO>> transactionRecord(PageQuery<FundTransactionRecord> page, FundTransactionQuery query) {
        query.setAgentId(AgentContent.getAgentId());
        IPage<FundTransactionRecordVO> transactionPage = fundTransactionRecordService.getTransactionPage(page, query);
        return Result.success(transactionPage);
    }

    /**
     * 交易记录数据
     */
    @GetMapping("/transaction/amount")
    @AgentPrivilege
    public Result<FundTransactionAmountVO> transactionAmount(FundTransactionQuery query) {
        query.setAgentId(AgentContent.getAgentId());
        FundTransactionAmountVO transactionAmount = fundTransactionRecordService.getTransactionAmount(query);
        return Result.success(transactionAmount);
    }

    /**
     * 收益数据
     */
    @GetMapping("/income/record")
    @AgentPrivilege
    public Result<IPage<FundIncomeRecordVO>> incomeRecord(PageQuery<FundIncomeRecord> page, FundIncomeQuery query) {
        query.setAgentId(AgentContent.getAgentId());
        if (CollectionUtils.isEmpty(query.getStatus())) {
            query.setStatus(List.of(1, 3));
        }
        return Result.success(fundIncomeRecordService.getPage(page, query));
    }

    /**
     * 收益数据
     */
    @GetMapping("/income/record/audit")
    @AgentPrivilege
    public Result<IPage<FundIncomeRecordVO>> auditIncomeRecord(PageQuery<FundIncomeRecord> page, FundIncomeQuery query) {
        query.setAgentId(AgentContent.getAgentId());
        if (CollectionUtils.isEmpty(query.getStatus())) {
            query.setStatus(List.of(2, 4));
        }
        return Result.success(fundIncomeRecordService.getPage(page, query));
    }

    /**
     * 收益数据统计
     */
    @GetMapping("/income/amount")
    @AgentPrivilege
    public Result<FundIncomeAmountVO> incomeAmount(FundIncomeQuery query) {
        query.setAgentId(AgentContent.getAgentId());
        if (CollectionUtils.isEmpty(query.getStatus())) {
            query.setStatus(List.of(1, 3));
        }
        FundIncomeAmountVO incomeAmount = fundIncomeRecordService.getIncomeAmount(query);
        return Result.success(incomeAmount);
    }

    /**
     * 收益数据统计
     */
    @GetMapping("/income/amount/audit")
    @AgentPrivilege
    public Result<FundIncomeAmountVO> incomeAuditAmount(FundIncomeQuery query) {
        query.setAgentId(AgentContent.getAgentId());
        if (CollectionUtils.isEmpty(query.getStatus())) {
            query.setStatus(List.of(2, 4));
        }
        FundIncomeAmountVO incomeAmount = fundIncomeRecordService.getIncomeAmount(query);
        return Result.success(incomeAmount);
    }


    /**
     * 持有记录
     */
    @GetMapping("/hold/record")
    @AgentPrivilege
    public Result<IPage<FundUserRecordVO>> holdRecord(PageQuery<FundRecord> pageQuery, FundRecordQuery query) {
        query.setAgentId(AgentContent.getAgentId());
        IPage<FundUserRecordVO> fundUserRecordPage = fundRecordService.fundUserRecordPage(pageQuery, query);
        return Result.success(fundUserRecordPage);
    }

    /**
     * 持有记录统计
     */
    @GetMapping("/hold/amount")
    @AgentPrivilege
    public Result<HoldUserAmount> holdAmount(FundRecordQuery query) {
        query.setAgentId(AgentContent.getAgentId());
        HoldUserAmount holdUserAmount = fundRecordService.fundUserAmount(query);
        return Result.success(holdUserAmount);
    }

    /**
     * 赎回审核
     */
    @PostMapping("/redemption/audit")
    @AgentPrivilege
    public Result<Void> redemptionAudit(@RequestBody @Valid FundAuditBO bo) {
        String key = StringUtils.join(bo.getIds(), ",");
        RLock lock = redissonClient.getLock(RedisLockConstants.FUND_REDEEM_LOCK + key);
        lock.lock();
        try {
            fundTransactionRecordService.redemptionAudit(bo);
            return Result.success();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 赎回审核记录
     */
    @GetMapping("/redemption/audit/{id}")
    @AgentPrivilege
    public Result<List<FundReviewVO>> redemptionAuditRecord(@PathVariable Long id) {
        List<FundReviewVO> fundReviewVOS = fundTransactionRecordService.getRedemptionAuditRecord(id);
        return Result.success(fundReviewVOS);
    }

    /**
     * 收益审核
     */
    @PostMapping("/income/audit")
    @AgentPrivilege
    public Result<Void> incomeAudit(@RequestBody @Valid FundAuditBO bo) {
        String key = StringUtils.join(bo.getIds(), ",");
        RLock lock = redissonClient.getLock(RedisLockConstants.FUND_INCOME_LOCK + key);
        lock.lock();
        try {
            fundIncomeRecordService.incomeAudit(bo);
            return Result.success();
        } finally {
            lock.unlock();
        }
    }

    /**
     * 收益审核记录
     */
    @GetMapping("/income/audit/{id}")
    @AgentPrivilege
    public Result<List<FundReviewVO>> incomeAuditRecord(@PathVariable Long id) {
        List<FundReviewVO> fundReviewVOS = fundIncomeRecordService.getIncomeAuditRecord(id);
        return Result.success(fundReviewVOS);
    }
}
