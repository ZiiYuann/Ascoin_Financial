package com.tianli.management.financial.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.common.CommonFunction;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.exception.Result;
import com.tianli.financial.FinancialProductService;
import com.tianli.financial.mapper.FinancialProduct;
import com.tianli.financial.mapper.FinancialProductStatus;
import com.tianli.financial.mapper.FinancialProductType;
import com.tianli.management.financial.dto.FinancialProductEditDto;
import com.tianli.management.financial.vo.FinancialProductListVo;
import com.tianli.role.annotation.AdminPrivilege;
import com.tianli.role.annotation.Privilege;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 * @author lzy
 * @date 2022/4/1 3:40 下午
 */
@RestController
@RequestMapping("/management/financial")
public class FinancialProductController {


    @Resource
    FinancialProductService financialProductService;


    @PostMapping("/edit")
    @AdminPrivilege(and = Privilege.理财配置)
    public Result edit(@RequestBody @Validated FinancialProductEditDto financialProductEditDto) {
        FinancialProduct financialProduct = BeanUtil.copyProperties(financialProductEditDto, FinancialProduct.class, "all_invest");
        if (ObjectUtil.isNull(financialProduct.getId())) {
            financialProduct.setCreate_time(LocalDateTime.now());
            financialProduct.setId(CommonFunction.generalId());
            financialProduct.setStatus(FinancialProductStatus.enable.name());
        } else {
            financialProduct.setUpdate_time(LocalDateTime.now());
        }
        financialProduct.setAll_invest(TokenCurrencyType.usdt_omni.amount(financialProductEditDto.getAll_invest()));
        financialProduct.setType(financialProduct.getPeriod().equals(0L) ? FinancialProductType.current.name() : FinancialProductType.fixed.name());
        financialProductService.saveOrUpdate(financialProduct);
        return Result.success();
    }

    @GetMapping("/list")
    @AdminPrivilege(and = Privilege.理财配置)
    public Result list(@RequestParam(value = "page", defaultValue = "1") Integer page,
                       @RequestParam(value = "size", defaultValue = "10") Integer size) {
        Page<FinancialProduct> productPage = financialProductService.page(new Page<>(page, size), new LambdaQueryWrapper<FinancialProduct>().orderByAsc(FinancialProduct::getCreate_time));
        Page<FinancialProductListVo> productListVoPage = new Page<>(page, size);
        productListVoPage.setRecords(new ArrayList<>());
        for (FinancialProduct record : productPage.getRecords()) {
            FinancialProductListVo productListVo = BeanUtil.copyProperties(record, FinancialProductListVo.class, "all_invest");
            productListVo.setAll_invest(TokenCurrencyType.usdt_omni.money(record.getAll_invest()));
            productListVoPage.getRecords().add(productListVo);
        }
        productListVoPage.setTotal(productPage.getTotal()).setPages(productPage.getPages());
        return Result.success(productListVoPage);
    }
}
