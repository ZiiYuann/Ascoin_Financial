package com.tianli.fund.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.common.PageQuery;
import com.tianli.exception.Result;
import com.tianli.financial.entity.FinancialProduct;
import com.tianli.fund.bo.FundPurchaseBO;
import com.tianli.fund.bo.FundRedemptionBO;
import com.tianli.fund.entity.FundIncomeRecord;
import com.tianli.fund.entity.FundRecord;
import com.tianli.fund.entity.FundTransactionRecord;
import com.tianli.fund.query.FundIncomeQuery;
import com.tianli.fund.query.FundTransactionQuery;
import com.tianli.fund.service.IFundRecordService;
import com.tianli.fund.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;

/**
 * <p>
 * 基金收益记录 前端控制器
 * </p>
 *
 * @author xianeng
 * @since 2022-08-30
 */
@RestController
@RequestMapping("/fund")
public class FundController {

    @Autowired
    private IFundRecordService fundRecordService;

    @GetMapping("/main/statistics")
    public Result mainPage(){
        FundMainPageVO fundMainPageVO = fundRecordService.mainPage();
        return Result.success(fundMainPageVO);
    }

    @GetMapping("/main/products")
    public Result productList(PageQuery<FinancialProduct> page){
        IPage<FundProductVO> fundProductPage = fundRecordService.productPage(page);
        return Result.success(fundProductPage);
    }

    @GetMapping("/main/hold")
    public Result holdProductList(PageQuery<FundRecord> page){
        IPage<FundRecordVO> recordPage = fundRecordService.fundRecordPage(page);
        return Result.success(recordPage);
    }

    @GetMapping("/apply/page")
    public Result applyPage(@RequestParam Long productId,@RequestParam(defaultValue = "0") BigDecimal purchaseAmount){
        FundApplyPageVO fundApplyPageVO = fundRecordService.applyPage(productId, purchaseAmount);
        return Result.success(fundApplyPageVO);
    }

    @PostMapping("/purchase")
    public Result purchase(@RequestBody @Valid FundPurchaseBO bo){
        fundRecordService.purchase(bo);
        return Result.success();
    }

    @GetMapping("/detail/{id}")
    public Result detail(@PathVariable Long id){
        FundRecordVO detail = fundRecordService.detail(id);
        return Result.success(detail);
    }

    @GetMapping("/income/record")
    public Result incomeRecord(PageQuery<FundIncomeRecord> page , FundIncomeQuery query){
        IPage<FundIncomeRecordVO> fundIncomeRecordPage = fundRecordService.incomeRecord(page, query);
        return Result.success(fundIncomeRecordPage);
    }

    @GetMapping("/redemption/page")
    public Result redemptionPage(Long id){
        FundRecordVO fundRecordVO = fundRecordService.redemptionPage(id);
        return Result.success(fundRecordVO);
    }

    @PostMapping("/apply/redemption")
    public Result applyRedemption(@RequestBody @Valid FundRedemptionBO bo){
        FundTransactionRecordVO fundTransactionRecordVO = fundRecordService.applyRedemption(bo);
        return Result.success(fundTransactionRecordVO);
    }

    @GetMapping("/transaction/record")
    public Result transactionRecord(PageQuery<FundTransactionRecord> page , FundTransactionQuery query){
        IPage<FundTransactionRecordVO> transactionRecordPage = fundRecordService.transactionRecord(page, query);
        return Result.success(transactionRecordPage);
    }

    @GetMapping("/transaction/detail/{transactionId}")
    public Result transactionDetail(@PathVariable Long transactionId){
        FundTransactionRecordVO fundTransactionRecordVO = fundRecordService.transactionDetail(transactionId);
        return Result.success(fundTransactionRecordVO);
    }

}

