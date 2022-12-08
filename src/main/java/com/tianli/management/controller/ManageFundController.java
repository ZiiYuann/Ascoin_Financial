package com.tianli.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.agent.management.vo.FundReviewVO;
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
import com.tianli.fund.vo.FundIncomeRecordVO;
import com.tianli.fund.vo.FundTransactionRecordVO;
import com.tianli.management.vo.FundIncomeAmountVO;
import com.tianli.management.vo.FundTransactionAmountVO;
import com.tianli.management.vo.FundUserRecordVO;
import com.tianli.management.vo.HoldUserAmount;
import com.tianli.sso.permission.AdminPrivilege;
import com.tianli.sso.permission.Privilege;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/management/fund")
public class ManageFundController {

    @Resource
    private IFundTransactionRecordService fundTransactionRecordService;

    @Resource
    private IFundIncomeRecordService fundIncomeRecordService;

    @Resource
    private IFundRecordService fundRecordService;

    /**
     * 交易记录
     */
    @GetMapping("/transaction/record")
    @AdminPrivilege(and = Privilege.基金管理)
    public Result transactionRecord(PageQuery<FundTransactionRecord> pageQuery, FundTransactionQuery query) {
        IPage<FundTransactionRecordVO> transactionPage = fundTransactionRecordService.getTransactionPage(pageQuery, query);
        return Result.success(transactionPage);
    }

    /**
     * 交易记录金额统计
     */
    @GetMapping("/transaction/amount")
    @AdminPrivilege(and = Privilege.基金管理)
    public Result transactionAmount(FundTransactionQuery query) {
        FundTransactionAmountVO transactionAmount = fundTransactionRecordService.getTransactionAmount(query);
        return Result.success(transactionAmount);
    }

    /**
     * 收益记录
     */
    @GetMapping("/income/record")
    @AdminPrivilege(and = Privilege.基金管理)
    public Result incomeRecord(PageQuery<FundIncomeRecord> pageQuery, FundIncomeQuery query) {
        IPage<FundIncomeRecordVO> page = fundIncomeRecordService.getPage(pageQuery, query);
        return Result.success(page);
    }

    /**
     * 收益记录金额统计
     */
    @GetMapping("/income/amount")
    @AdminPrivilege(and = Privilege.基金管理)
    public Result incomeAmount(FundIncomeQuery query) {
        FundIncomeAmountVO incomeAmount = fundIncomeRecordService.getIncomeAmount(query);
        return Result.success(incomeAmount);
    }

    /**
     * 持仓用户
     */
    @GetMapping("/hold/record")
    @AdminPrivilege(and = Privilege.基金管理)
    public Result holdRecord(PageQuery<FundRecord> pageQuery, FundRecordQuery query) {
        IPage<FundUserRecordVO> fundUserRecordPage = fundRecordService.fundUserRecordPage(pageQuery, query);
        return Result.success(fundUserRecordPage);
    }

    /**
     * 持仓用户统计
     */
    @GetMapping("/hold/amount")
    @AdminPrivilege(and = Privilege.基金管理)
    public Result holdAmount(FundRecordQuery query) {
        HoldUserAmount holdUserAmount = fundRecordService.fundUserAmount(query);
        return Result.success(holdUserAmount);
    }

    /**
     * 收益审核记录
     */
    @GetMapping("/income/audit/{id}")
    @AdminPrivilege(api = "/management/fund/income/audit/id")
    public Result incomeAuditRecord(@PathVariable Long id) {
        List<FundReviewVO> fundReviewVOS = fundIncomeRecordService.getIncomeAuditRecord(id);
        return Result.success(fundReviewVOS);
    }

    /**
     * 赎回审核记录
     */
    @GetMapping("/redemption/audit/{id}")
    @AdminPrivilege(api = "/management/fund/redemption/audit/id")
    public Result redemptionAuditRecord(@PathVariable Long id) {
        List<FundReviewVO> fundReviewVOS = fundTransactionRecordService.getRedemptionAuditRecord(id);
        return Result.success(fundReviewVOS);
    }

}
