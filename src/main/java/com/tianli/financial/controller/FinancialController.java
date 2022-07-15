package com.tianli.financial.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tianli.charge.enums.ChargeType;
import com.tianli.common.PageQuery;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.financial.convert.FinancialConverter;
import com.tianli.financial.entity.FinancialProduct;
import com.tianli.financial.enums.ProductStatus;
import com.tianli.financial.enums.ProductType;
import com.tianli.financial.query.PurchaseQuery;
import com.tianli.financial.service.FinancialProductService;
import com.tianli.financial.service.FinancialService;
import com.tianli.financial.vo.OrderFinancialVO;
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
    private FinancialService financialService;
    @Resource
    private FinancialConverter financialConverter;

    /**
     * 理财产品列表
     */
    @GetMapping("/products")
    public Result products(){
        var list = financialProductService.list(new LambdaQueryWrapper<FinancialProduct>()
                .eq(FinancialProduct::getStatus, ProductStatus.open)
                .orderByAsc(f-> f.getTerm().getDay())
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
     * 申购理财产品（余额）
     */
    @PostMapping("/purchase/balance")
    public Result balancePurchase(@RequestBody @Valid PurchaseQuery purchaseQuery){
        //TODO 币种的转换，校验密码
        return Result.instance().setData(financialService.purchase(purchaseQuery));
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
     * 收益明细
     */
    @GetMapping("/income")
    public Result income() {
        Long uid = requestInitService.uid();
        return Result.instance().setData(financialService.income(uid));
    }

    /**
     * 我的持用
     */
    @GetMapping("/myHold")
    public Result myHold(ProductType productType) {
        Long uid = requestInitService.uid();
        return Result.instance().setData(financialService.myHold(uid,productType));
    }

    /**
     * 具体收益明细
     */
    @GetMapping("/income/details/{recordId}")
    public Result incomeDetails(@PathVariable Long recordId) {
        Long uid = requestInitService.uid();
        return Result.instance().setData(financialService.incomeDetails(uid,recordId));
    }

    /**
     * 交易记录
     */
    @GetMapping("/order")
    public Result order(PageQuery<OrderFinancialVO> pageQuery, ProductType productType, ChargeType chargeType) {
        if(!ChargeType.purchase.equals(chargeType) && !ChargeType.redeem.equals(chargeType)
             && !ChargeType.transfer.equals(chargeType)){
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }

        Long uid = requestInitService.uid();
        return Result.instance().setData(financialService.orderPage(uid,pageQuery.page(),productType,chargeType));
    }



}
