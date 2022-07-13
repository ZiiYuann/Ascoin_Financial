package com.tianli.financial.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tianli.account.service.AccountBalanceOperationLogService;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.exception.Result;
import com.tianli.financial.convert.FinancialConverter;
import com.tianli.financial.entity.FinancialProduct;
import com.tianli.financial.enums.FinancialProductStatus;
import com.tianli.financial.query.PurchaseQuery;
import com.tianli.financial.service.FinancialLogService;
import com.tianli.financial.service.FinancialProductService;
import com.tianli.financial.service.impl.FinancialServiceImpl;
import com.tianli.sso.init.RequestInitService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/financial")
public class FinancialController {

    @Resource
    private FinancialProductService financialProductService;
    @Resource
    private RequestInitService requestInitService;
    @Resource
    private FinancialServiceImpl financialService;
    @Resource
    private FinancialLogService userFinancialLogService;
    @Resource
    private AccountBalanceOperationLogService currencyLogService;
    @Resource
    private AccountBalanceService accountSummaryService;
    @Resource
    private FinancialConverter financialConverter;

    /**
     * 理财产品列表
     */
    @GetMapping("/products")
    public Result products(){
        var list = financialProductService.list(new LambdaQueryWrapper<FinancialProduct>()
                .eq(FinancialProduct::getStatus, FinancialProductStatus.enable)
                .orderByAsc(f-> f.getPurchaseTerm().getDay())
        ).stream().map(financialConverter :: toVO).collect(Collectors.toList());
        return Result.instance().setData(list);
    }

    /**
     * 理财产品详情
     */
    @GetMapping("/product/{productId}")
    public Result oneProduct(@PathVariable("productId") Long productId){
        FinancialProduct financialProduct = financialProductService.getById(productId);
        return Result.instance().setData(financialConverter.toVO(financialProduct));
    }

    /**
     * 申购理财产品
     */
    @PostMapping("/purchase")
    public Result purchase(@RequestBody @Valid PurchaseQuery purchaseQuery){
        //TODO 币种的转换，校验密码
        return Result.instance().setData(financialService.purchase(purchaseQuery));
    }

    @GetMapping("/my")
    public Result my( @RequestParam(value = "page", defaultValue = "1") Integer page,
                      @RequestParam(value = "size", defaultValue = "10") Integer size) {
        return null;
    }




}
