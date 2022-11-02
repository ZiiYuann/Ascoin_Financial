package com.tianli.financial.controller;

import com.tianli.common.PageQuery;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.exception.Result;
import com.tianli.financial.dto.FinancialIncomeAccrueDTO;
import com.tianli.financial.entity.FinancialIncomeDaily;
import com.tianli.financial.entity.FinancialProduct;
import com.tianli.financial.entity.FinancialRecord;
import com.tianli.financial.enums.ProductType;
import com.tianli.financial.query.RecordRenewalQuery;
import com.tianli.financial.service.FinancialProductLadderRateService;
import com.tianli.financial.service.FinancialProductService;
import com.tianli.financial.service.FinancialRecordService;
import com.tianli.financial.service.FinancialService;
import com.tianli.financial.vo.ExpectIncomeVO;
import com.tianli.management.query.FinancialProductIncomeQuery;
import com.tianli.sso.init.RequestInitService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

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
    @Resource
    private FinancialProductLadderRateService financialProductLadderRateService;


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
    public Result productDetailsByCoin(@PathVariable("coin") CurrencyCoin coin) {
        return Result.instance().setData(financialService.fixedProductDetails(coin));
    }

    /**
     * 预计收益接口
     */
    @GetMapping("/expect/income")
    public Result expectIncome(Long productId, BigDecimal amount) {
        FinancialProduct product = financialProductService.getById(productId);
        ExpectIncomeVO expectIncomeVO = new ExpectIncomeVO();

        if (Objects.isNull(product)) {
            expectIncomeVO.setExpectIncome(BigDecimal.ZERO);
            return Result.instance().setData(expectIncomeVO);
        }


        if (product.getRateType() == 1) {
            BigDecimal income = financialProductLadderRateService.calLadderIncome(productId, amount);
            expectIncomeVO.setExpectIncome(income);
            return Result.instance().setData(expectIncomeVO);
        }

        expectIncomeVO.setExpectIncome(product.getRate().multiply(amount)
                .multiply(BigDecimal.valueOf(product.getTerm().getDay()))
                .divide(BigDecimal.valueOf(365), 8, RoundingMode.DOWN));


        return Result.instance().setData(expectIncomeVO);
    }

    /**
     * 【我的持用】
     */
    @GetMapping("/hold")
    public Result myHold(PageQuery<FinancialProduct> pageQuery, ProductType productType) {
        Long uid = requestInitService.uid();
        return Result.instance().setData(financialService.holdProductPage(pageQuery.page(), uid, productType));
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


}
