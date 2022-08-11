package com.tianli.financial.controller;

import cn.hutool.core.date.DateUtil;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.common.PageQuery;
import com.tianli.exception.Result;
import com.tianli.financial.convert.FinancialConverter;
import com.tianli.financial.dto.FinancialIncomeAccrueDTO;
import com.tianli.financial.entity.FinancialIncomeDaily;
import com.tianli.financial.entity.FinancialProduct;
import com.tianli.financial.entity.FinancialRecord;
import com.tianli.financial.enums.BusinessType;
import com.tianli.financial.enums.ProductType;
import com.tianli.financial.query.RecordRenewalQuery;
import com.tianli.financial.service.FinancialProductService;
import com.tianli.financial.service.FinancialRecordService;
import com.tianli.financial.service.FinancialService;
import com.tianli.financial.vo.ExpectIncomeVO;
import com.tianli.financial.vo.FinancialProductVO;
import com.tianli.management.query.FinancialProductIncomeQuery;
import com.tianli.sso.init.RequestInitService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/financial")
public class FinancialController {

    @Resource
    private FinancialProductService financialProductService;
    @Resource
    private RequestInitService requestInitService;
    @Resource
    private FinancialService financialService;
    @Resource
    private FinancialConverter financialConverter;
    @Resource
    private FinancialRecordService financialRecordService;
    @Resource
    private AccountBalanceService accountBalanceService;

    /**
     * 理财产品列表
     */
    @GetMapping("/products/summary")
    public Result productSummary(PageQuery<FinancialProduct> pageQuery,ProductType productType){
        return Result.instance().setData(financialService.products(pageQuery.page(),productType));
    }

    /**
     * 理财产品列表
     */
    @GetMapping("/products")
    public Result products(PageQuery<FinancialProduct> pageQuery,ProductType productType){
        return Result.instance().setData(financialService.products(pageQuery.page(),productType));
    }

    /**
     * 理财产品详情
     */
    @GetMapping("/product/{productId}")
    public Result oneProduct(@PathVariable("productId") Long productId){
        Long uid = requestInitService.uid();
        FinancialProduct product = financialProductService.getById(productId);

        FinancialProductVO productVO = financialConverter.toFinancialProductVO(product);

        var useQuota = financialRecordService.getUseQuota(List.of(product.getId()));
        var personUseQuota = financialRecordService.getUseQuota(List.of(product.getId()),uid);
        var accountBalance = accountBalanceService.getAndInit(uid, product.getCoin());

        LocalDateTime now = LocalDateTime.now();
        productVO.setUseQuota(useQuota.getOrDefault(productVO.getId(),BigDecimal.ZERO));
        productVO.setUserPersonQuota(personUseQuota.getOrDefault(productVO.getId(),BigDecimal.ZERO));
        productVO.setAvailableBalance(accountBalance.getRemain());
        productVO.setPurchaseTime(now);
        LocalDateTime startIncomeTime = DateUtil.beginOfDay(new Date()).toLocalDateTime().plusDays(1);
        productVO.setStartIncomeTime(startIncomeTime);
        productVO.setSettleTime(startIncomeTime.plusDays(product.getTerm().getDay()));
        return Result.instance().setData(productVO);
    }

    /**
     * 预计收益接口
     */
    @GetMapping("/expect/income")
    public Result expectIncome(Long productId,BigDecimal amount){
        FinancialProduct product = financialProductService.getById(productId);
        ExpectIncomeVO expectIncomeVO = new ExpectIncomeVO();
        expectIncomeVO.setExpectIncome(product.getRate().multiply(amount)
                .multiply(BigDecimal.valueOf(product.getTerm().getDay()))
                .divide(BigDecimal.valueOf(365),8, RoundingMode.DOWN));
        return Result.instance().setData(expectIncomeVO);
    }

    /**
     * 【我的赚币】我的持用
     */
    @GetMapping("/hold")
    public Result myHold(PageQuery<FinancialRecord> pageQuery,ProductType productType) {
        Long uid = requestInitService.uid();
        return Result.instance().setData(financialService.myHold(pageQuery.page(),uid,productType));
    }

    /**
     * 【持有详情】下方收益明细列表
     */
    @GetMapping("/incomes")
    public Result incomes(PageQuery<FinancialIncomeAccrueDTO> page,ProductType productType) {
        Long uid = requestInitService.uid();

        FinancialProductIncomeQuery query = new FinancialProductIncomeQuery();
        query.setUid(uid + "");
        query.setProductType(productType);

        return Result.instance().setData(financialService.incomeRecord(page.page(),query));
    }

    /**
     * 我的赚币【上方】
     */
    @GetMapping("/income")
    public Result income() {
        Long uid = requestInitService.uid();
        return Result.instance().setData(financialService.income(uid));
    }

    /**
     * 【持有详情】上方汇总信息
     */
    @GetMapping("/income/{recordId}")
    public Result incomeByRecordId(@PathVariable Long recordId) {
        Long uid = requestInitService.uid();
        return Result.instance().setData(financialService.incomeByRecordId(uid,recordId));
    }

    /**
     * 持有产品续费配置
     */
    @PostMapping("/record/renewal")
    public Result recordRenewal(@RequestBody @Valid RecordRenewalQuery query) {
        financialRecordService.recordRenewal(query);
        return Result.instance();
    }

    /**
     * 具体收益明细
     */
    @GetMapping("/income/details")
    public Result incomeDetails(PageQuery<FinancialIncomeDaily> pageQuery, Long recordId) {
        Long uid = requestInitService.uid();
        return Result.instance().setData(financialService.incomeDetails(pageQuery.page(),uid,recordId));
    }

    /**
     * 限时活动
     */
    @GetMapping("/activities")
    public Result limitedActivities(PageQuery<FinancialProduct> pageQuery){
        return Result.instance().setData(financialService.activitiesProducts(pageQuery.page(),BusinessType.limited));
    }

}
