package com.tianli.management.financial.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.common.CommonFunction;
import com.tianli.exception.Result;
import com.tianli.financial.service.FinancialProductService;
import com.tianli.financial.entity.FinancialProduct;
import com.tianli.financial.enums.FinancialProductStatus;
import com.tianli.management.financial.dto.FinancialProductEditDto;
import com.tianli.management.financial.vo.FinancialProductListVo;
import com.tianli.sso.permission.AdminPrivilege;
import com.tianli.sso.permission.AdminPrivilege;
import com.tianli.sso.permission.Privilege;
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
            financialProduct.setCreateTime(LocalDateTime.now());
            financialProduct.setId(CommonFunction.generalId());
            financialProduct.setStatus(FinancialProductStatus.enable);
        } else {
            financialProduct.setUpdateTime(LocalDateTime.now());
        }
        financialProductService.saveOrUpdate(financialProduct);
        return Result.success();
    }

    @GetMapping("/list")
    @AdminPrivilege(and = Privilege.理财配置)
    public Result list(@RequestParam(value = "page", defaultValue = "1") Integer page,
                       @RequestParam(value = "size", defaultValue = "10") Integer size) {
        Page<FinancialProduct> productPage = financialProductService.page(new Page<>(page, size), new LambdaQueryWrapper<FinancialProduct>().orderByAsc(FinancialProduct::getCreateTime));
        Page<FinancialProductListVo> productListVoPage = new Page<>(page, size);
        productListVoPage.setRecords(new ArrayList<>());
        for (FinancialProduct record : productPage.getRecords()) {
            FinancialProductListVo productListVo = BeanUtil.copyProperties(record, FinancialProductListVo.class, "all_invest");
            productListVoPage.getRecords().add(productListVo);
        }
        productListVoPage.setTotal(productPage.getTotal()).setPages(productPage.getPages());
        return Result.success(productListVoPage);
    }
}
