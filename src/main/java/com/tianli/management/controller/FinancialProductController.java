package com.tianli.management.controller;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.common.CommonFunction;
import com.tianli.common.PageQuery;
import com.tianli.exception.Result;
import com.tianli.financial.convert.FinancialConverter;
import com.tianli.financial.entity.FinancialProduct;
import com.tianli.financial.service.FinancialProductService;
import com.tianli.financial.service.FinancialService;
import com.tianli.financial.vo.FinancialProductVO;
import com.tianli.financial.vo.OrderFinancialVO;
import com.tianli.management.query.FinancialOrdersQuery;
import com.tianli.management.query.FinancialProductEditQuery;
import com.tianli.management.query.FinancialProductsQuery;
import com.tianli.sso.permission.AdminPrivilege;
import com.tianli.sso.permission.Privilege;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * @author lzy
 * @since  2022/4/1 3:40 下午
 */
@RestController
@RequestMapping("/management/financial")
public class FinancialProductController {

    @Resource
    FinancialProductService financialProductService;
    @Resource
    FinancialConverter financialConverter;
    @Resource
    FinancialService financialService;

    /**
     * 新增或者修改产品
     */
    @PostMapping("/save")
    @AdminPrivilege(and = Privilege.理财配置)
    public Result edit(@RequestBody @Validated FinancialProductEditQuery financialProductQuery) {

        FinancialProduct product = financialConverter.toDO(financialProductQuery);
        if (ObjectUtil.isNull(product.getId())) {
            product.setCreateTime(LocalDateTime.now());
            product.setId(CommonFunction.generalId());
        } else {
            product.setUpdateTime(LocalDateTime.now());
            // TODO 对于修改操作需要校验很多东西
        }
        financialProductService.saveOrUpdate(product);
        return Result.success();
    }

    /**
     * 产品列表
     */
    @GetMapping("/products")
    @AdminPrivilege(and = Privilege.理财配置)
    public Result products(PageQuery<FinancialProduct> page, FinancialProductsQuery query) {
        IPage<FinancialProductVO> financialProductVOIPage = financialProductService.selectListByQuery(page.page(), query);
        return Result.success(financialProductVOIPage);
    }

    /**
     * 订单列表
     */
    @GetMapping("/orders")
    @AdminPrivilege(and = Privilege.理财配置)
    public Result orders(PageQuery<OrderFinancialVO> page, FinancialOrdersQuery query) {
        IPage<OrderFinancialVO> financialOrderVOIPage = financialService.orderPage(page.page(), query);
        return Result.success(financialOrderVOIPage);
    }

}
