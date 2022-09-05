package com.tianli.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
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
import com.tianli.management.vo.FundTransactionAmountVO;
import com.tianli.management.vo.FundUserRecordVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/management/fund")
public class FundManageController {

    @Autowired
    private IFundTransactionRecordService fundTransactionRecordService;

    @Autowired
    private IFundIncomeRecordService fundIncomeRecordService;

    @Autowired
    private IFundRecordService fundRecordService;

    @GetMapping("/transaction/record")
    public Result transactionRecord(PageQuery<FundTransactionRecord> pageQuery, FundTransactionQuery query){
        fundTransactionRecordService.getTransactionPage(pageQuery,query);
        return Result.success();
    }

    @GetMapping("/transaction/amount")
    public Result transactionAmount(FundTransactionQuery query){
        FundTransactionAmountVO transactionAmount = fundTransactionRecordService.getTransactionAmount(query);
        return Result.success(transactionAmount);
    }

    @GetMapping("/income/record")
    public Result incomeRecord(PageQuery<FundIncomeRecord> pageQuery, FundIncomeQuery query){
        IPage<FundIncomeRecordVO> page = fundIncomeRecordService.getPage(pageQuery, query);
        return Result.success(page);
    }

    @GetMapping("/income/amount")
    public Result incomeAmount(PageQuery<FundIncomeRecord> pageQuery, FundIncomeQuery query){
        IPage<FundIncomeRecordVO> page = fundIncomeRecordService.getPage(pageQuery, query);
        return Result.success(page);
    }

    @GetMapping("/hold/record")
    public Result holdRecord(PageQuery<FundRecord> pageQuery, FundRecordQuery query){
        IPage<FundUserRecordVO> fundUserRecordPage = fundRecordService.fundUserRecordPage(pageQuery, query);
        return Result.success(fundUserRecordPage);
    }

}
