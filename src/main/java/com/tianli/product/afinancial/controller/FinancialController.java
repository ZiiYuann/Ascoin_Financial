package com.tianli.product.afinancial.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.common.PageQuery;
import com.tianli.common.annotation.AppUse;
import com.tianli.exception.Result;
import com.tianli.product.afinancial.dto.FinancialIncomeAccrueDTO;
import com.tianli.product.afinancial.entity.FinancialIncomeDaily;
import com.tianli.product.afinancial.entity.FinancialProduct;
import com.tianli.product.afinancial.enums.ProductType;
import com.tianli.product.afinancial.query.ProductHoldQuery;
import com.tianli.product.afinancial.query.RecordRenewalQuery;
import com.tianli.product.afinancial.vo.DollarIncomeVO;
import com.tianli.product.afinancial.vo.HoldProductVo;
import com.tianli.product.service.FinancialProductService;
import com.tianli.product.afinancial.service.FinancialRecordService;
import com.tianli.product.afinancial.service.FinancialService;
import com.tianli.product.afinancial.vo.ExpectIncomeVO;
import com.tianli.management.query.FinancialProductIncomeQuery;
import com.tianli.sso.init.RequestInitService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigDecimal;

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
    private FinancialRecordService financialRecordService;

    /**
     * 【量化理财主页面】产品汇总列表
     */
    @GetMapping("/summary/products")
    public Result productSummary(PageQuery<FinancialProduct> pageQuery, ProductType productType) {
        return Result.instance().setData(financialService.summaryProducts(pageQuery.page(), productType));
    }

    /**
     * 【量化理财主页面】产品列表
     */
    @GetMapping("/products")
    public Result products(PageQuery<FinancialProduct> pageQuery, ProductType productType) {
        return Result.instance().setData(financialService.products(pageQuery.page(), productType));
    }

    /**
     * 【量化理财主页面】 推荐列表
     */
    @GetMapping("/recommend/products")
    public Result recommendProducts() {
        return Result.instance().setData(financialService.recommendProducts());
    }

    /**
     * 申购详情【活期】
     */
    @GetMapping("/product/{productId}")
    public Result fixedProductDetails(@PathVariable("productId") Long productId) {
        return Result.instance().setData(financialService.currentProductDetails(productId));
    }

    /**
     * 申购详情【定期】
     */
    @GetMapping("/product/fixed/{coin}")
    public Result productDetailsByCoin(@PathVariable("coin") String coin) {
        return Result.instance().setData(financialService.fixedProductDetails(coin));
    }

    /**
     * 预计收益接口
     */
    @GetMapping("/expect/income")
    public Result expectIncome(Long productId, BigDecimal amount) {
        ExpectIncomeVO expectIncomeVO = financialProductService.expectIncome(productId, amount);
        return Result.instance().setData(expectIncomeVO);
    }

    /**
     * 【我的持用】
     */
    @GetMapping("/hold/hierarchy")
    public Result myHoldHierarchy(PageQuery<FinancialProduct> pageQuery, ProductType productType) {
        Long uid = requestInitService.uid();
        return Result.instance().setData(financialService.holdProduct(pageQuery.page(), uid, productType));
    }

    /**
     * 【我的持用】
     */
    @AppUse
    @GetMapping("/hold")
    public Result<IPage<HoldProductVo>> myHold(PageQuery<FinancialProduct> pageQuery, ProductType productType) {
        Long uid = requestInitService.uid();
        return new Result<>(financialService.holdProductPage(pageQuery.page(),
                ProductHoldQuery.builder()
                        .productType(productType)
                        .uid(uid)
                        .build()));
    }

    /**
     * 【我的持用】
     */
    @GetMapping("/transaction/records")
    public Result transactionRecords(PageQuery<FinancialProduct> pageQuery, ProductType productType) {
        Long uid = requestInitService.uid();
        return Result.instance().setData(financialService.transactionRecordPage(pageQuery.page(), uid, productType));
    }

    /**
     * 【持有详情】下方收益明细列表
     */
    @GetMapping("/incomes")
    public Result incomes(PageQuery<FinancialIncomeAccrueDTO> page, ProductType productType) {
        Long uid = requestInitService.uid();

        FinancialProductIncomeQuery query = new FinancialProductIncomeQuery();
        query.setUid(uid + "");
        query.setProductType(productType);

        return Result.instance().setData(financialService.incomeRecordPage(page.page(), query));
    }

    /**
     * 【理财首页】上方
     */
    @AppUse
    @GetMapping("/income")
    public Result<DollarIncomeVO> income() {
        Long uid = requestInitService.uid();
        return new Result<>(financialService.income(uid));
    }

    /**
     * 【持有详情】上方汇总信息
     */
    @GetMapping("/income/{recordId}")
    public Result incomeByRecordId(@PathVariable Long recordId) {
        Long uid = requestInitService.uid();
        return Result.instance().setData(financialService.recordIncome(uid, recordId));
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
        return Result.instance().setData(financialService.dailyIncomePage(pageQuery.page(), uid, recordId));
    }

    /**
     * 额外的产品信息
     */
    @GetMapping("/extraInfo/{productId}")
    public Result productInfo(@PathVariable Long productId) {
        Long uid = requestInitService.uid();
        return Result.instance().setData(financialService.productExtraInfo(uid, productId));
    }

}
