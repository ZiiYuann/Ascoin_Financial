package com.tianli.fund.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.common.PageQuery;
import com.tianli.common.RedisLockConstants;
import com.tianli.exception.Result;
import com.tianli.financial.entity.FinancialProduct;
import com.tianli.fund.bo.FundPurchaseBO;
import com.tianli.fund.bo.FundRedemptionBO;
import com.tianli.fund.contant.FundQuota;
import com.tianli.fund.entity.FundIncomeRecord;
import com.tianli.fund.entity.FundRecord;
import com.tianli.fund.entity.FundTransactionRecord;
import com.tianli.fund.enums.FundIncomeStatus;
import com.tianli.fund.query.FundIncomeQuery;
import com.tianli.fund.query.FundTransactionQuery;
import com.tianli.fund.service.IFundRecordService;
import com.tianli.fund.vo.*;
import com.tianli.sso.init.RequestInitService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
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

    @Resource
    private IFundRecordService fundRecordService;

    @Resource
    private RedissonClient redissonClient;

    @Resource
    private RequestInitService requestInitService;

    /**
     * 主页面持有统计
     */
    @GetMapping("/main/statistics")
    public Result mainPage(){
        FundMainPageVO fundMainPageVO = fundRecordService.mainPage();
        return Result.success(fundMainPageVO);
    }

    /**
     * 主页面产品列表
     */
    @GetMapping("/main/products")
    public Result productList(PageQuery<FinancialProduct> page){
        IPage<FundProductVO> fundProductPage = fundRecordService.productPage(page);
        return Result.success(fundProductPage);
    }

    /**
     * 用户持有
     */
    @GetMapping("/main/hold")
    public Result holdProductList(PageQuery<FundRecord> page){
        IPage<FundRecordVO> recordPage = fundRecordService.fundRecordPage(page);
        return Result.success(recordPage);
    }

    /**
     * 申购页面
     */
    @GetMapping("/apply/page")
    public Result applyPage(@RequestParam Long productId,@RequestParam(defaultValue = "0") BigDecimal purchaseAmount){
        FundApplyPageVO fundApplyPageVO = fundRecordService.applyPage(productId, purchaseAmount);
        return Result.success(fundApplyPageVO);
    }

    /**
     * 基金申购
     */
    @PostMapping("/purchase")
    public Result purchase(@RequestBody @Valid FundPurchaseBO bo){
        Long uid = requestInitService.uid();
        RLock lock = redissonClient.getLock(RedisLockConstants.FUND_CREATE_LOCK + uid);
        lock.lock();
        try{
            FundTransactionRecordVO purchase = fundRecordService.purchase(bo);
            return Result.success(purchase);
        }finally {
            lock.unlock();
        }
    }

    /**
     * 持有详情
     */
    @GetMapping("/detail/{id}")
    public Result detail(@PathVariable Long id){
        FundRecordVO detail = fundRecordService.detail(id);
        return Result.success(detail);
    }

    /**
     * 收益明细
     */
    @GetMapping("/income/record")
    public Result incomeRecord(PageQuery<FundIncomeRecord> page , FundIncomeQuery query){
        IPage<FundIncomeRecordVO> fundIncomeRecordPage = fundRecordService.incomeRecord(page, query);
        return Result.success(fundIncomeRecordPage);
    }

    /**
     * 收益明细
     */
    @GetMapping("/incomes")
    public Result incomes(PageQuery<FundIncomeRecord> page , FundIncomeQuery query){
        IPage<FundIncomeRecordVO> fundIncomeRecordPage = fundRecordService.incomeSummary(page, query);
        return Result.success(fundIncomeRecordPage);
    }

    /**
     * 赎回页面
     */
    @GetMapping("/redemption/page")
    public Result redemptionPage(Long id){
        FundRecordVO fundRecordVO = fundRecordService.redemptionPage(id);
        return Result.success(fundRecordVO);
    }

    /**
     * 基金赎回
     */
    @PostMapping("/apply/redemption")
    public Result applyRedemption(@RequestBody @Valid FundRedemptionBO bo){
        RLock lock = redissonClient.getLock(RedisLockConstants.FUND_UPDATE_LOCK + bo.getId());
        lock.lock();
        try {
            FundTransactionRecordVO fundTransactionRecordVO = fundRecordService.applyRedemption(bo);
            return Result.success(fundTransactionRecordVO);
        }finally {
            lock.unlock();
        }
    }

    /**
     *交易记录
     */
    @GetMapping("/transaction/record")
    public Result transactionRecord(PageQuery<FundTransactionRecord> page , FundTransactionQuery query){
        Long uid = requestInitService.uid();
        query.setUid(uid);
        IPage<FundTransactionRecordVO> transactionRecordPage = fundRecordService.transactionRecord(page, query);
        return Result.success(transactionRecordPage);
    }

    /**
     *交易记录详情
     */
    @GetMapping("/transaction/detail/{transactionId}")
    public Result transactionDetail(@PathVariable Long transactionId){
        FundTransactionRecordVO fundTransactionRecordVO = fundRecordService.transactionDetail(transactionId);
        return Result.success(fundTransactionRecordVO);
    }

    /**
     * 申购或赎回配置列表
     */
    @GetMapping("/quota/config")
    public Result quota(){
        return Result.success(FundQuota.quotas);
    }

}
