package com.tianli.financial.controller;

import com.tianli.account.entity.AccountBalance;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.common.PageQuery;
import com.tianli.common.TimeUtils;
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
import java.time.LocalDateTime;
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
    @Resource
    private AccountBalanceService accountBalanceService;

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


        var useQuota = financialRecordService.getUseQuota(List.of(product.getId()));
        var personUseQuota = financialRecordService.getUseQuota(List.of(product.getId()),uid);
        var accountBalance = accountBalanceService.get(uid, product.getCoin());

        FinancialProductVO productVO = financialConverter.toVO(product);
        LocalDateTime now = LocalDateTime.now();
        productVO.setUseQuota(useQuota.getOrDefault(productVO.getId(),BigDecimal.ZERO));
        productVO.setUserPersonQuota(personUseQuota.getOrDefault(productVO.getId(),BigDecimal.ZERO));
        productVO.setAvailableBalance(accountBalance.getRemain());
        productVO.setPurchaseTime(now);
        productVO.setStartIncomeTime(TimeUtils.StartOfTime(TimeUtils.Util.DAY).plusDays(1));
        productVO.setPurchaseTime(now.plusDays(product.getTerm().getDay()));
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
