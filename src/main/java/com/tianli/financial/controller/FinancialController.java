package com.tianli.financial.controller;

import com.tianli.common.PageQuery;
import com.tianli.exception.Result;
import com.tianli.financial.convert.FinancialConverter;
import com.tianli.financial.dto.FinancialIncomeAccrueDTO;
import com.tianli.financial.entity.FinancialProduct;
import com.tianli.financial.enums.BusinessType;
import com.tianli.financial.enums.ProductType;
import com.tianli.financial.query.PurchaseQuery;
import com.tianli.financial.service.FinancialProductService;
import com.tianli.financial.service.FinancialRecordService;
import com.tianli.financial.service.FinancialService;
import com.tianli.financial.vo.FinancialProductVO;
import com.tianli.management.query.FinancialProductIncomeQuery;
import com.tianli.sso.init.RequestInitService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
        FinancialProduct product = financialProductService.getById(productId);
        Map<Long, BigDecimal> useQuota = financialRecordService.getUseQuota(List.of(product.getId()));

        FinancialProductVO productVO = financialConverter.toVO(product);
        productVO.setUseQuota(useQuota.getOrDefault(productVO.getId(),BigDecimal.ZERO));
        return Result.instance().setData(productVO);
    }

    /**
     * 申购理财产品（钱包）
     */
    @PostMapping("/purchase/wallet")
    public Result walletPurchase(@RequestBody @Valid PurchaseQuery purchaseQuery){
        //TODO 币种的转换，校验密码
        return Result.instance().setData(financialService.purchase(purchaseQuery));
    }

    /**
     * 我的持用
     */
    @GetMapping("/hold")
    public Result myHold(ProductType productType) {
        Long uid = requestInitService.uid();
        return Result.instance().setData(financialService.myHold(uid,productType));
    }

    /**
     * 收益明细列表
     */
    @GetMapping("/incomes")
    public Result incomes(PageQuery<FinancialIncomeAccrueDTO> page,ProductType productType) {
        Long uid = requestInitService.uid();

        FinancialProductIncomeQuery query = new FinancialProductIncomeQuery();
        query.setUid(uid);
        query.setProductType(productType);

        return Result.instance().setData(financialService.incomeRecord(page.page(),query));
    }

    /**
     * 具体收益明细
     */
    @GetMapping("/income/{recordId}")
    public Result incomeDetails(@PathVariable Long recordId) {
        Long uid = requestInitService.uid();
        return Result.instance().setData(financialService.incomeDetails(uid,recordId));
    }

    /**
     * 限时活动
     */
    @GetMapping("/activities")
    public Result limitedActivities(PageQuery<FinancialProduct> pageQuery){
        return Result.instance().setData(financialService.activitiesProducts(pageQuery.page(),BusinessType.limited));
    }

}
