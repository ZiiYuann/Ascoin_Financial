package com.tianli.management.controller;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.charge.enums.ChargeType;
import com.tianli.common.PageQuery;
import com.tianli.exception.Result;
import com.tianli.financial.dto.FinancialIncomeAccrueDTO;
import com.tianli.financial.entity.FinancialProduct;
import com.tianli.financial.enums.ProductType;
import com.tianli.financial.service.FinancialProductService;
import com.tianli.financial.service.FinancialService;
import com.tianli.financial.vo.OrderFinancialVO;
import com.tianli.management.query.*;
import com.tianli.management.service.FinancialBoardProductService;
import com.tianli.management.vo.FundProductBindDropdownVO;
import com.tianli.management.vo.MFinancialProductVO;
import com.tianli.sso.permission.AdminPrivilege;
import com.tianli.sso.permission.Privilege;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author lzy
 * @since 2022/4/1 3:40 下午
 */
@RestController
@RequestMapping("/management/financial")
public class ManageProductController {

    @Resource
    private FinancialProductService financialProductService;
    @Resource
    private FinancialService financialService;
    @Resource
    private FinancialBoardProductService financialProductBoardService;


    /**
     * 数据展板
     */
    @GetMapping("/board")
    @AdminPrivilege(and = Privilege.理财配置)
    public Result edit(FinancialBoardQuery query) {
        query.calTime();
        return Result.success().setData(financialProductBoardService.productBoard(query));
    }

    /**
     * 新增或者修改产品
     */
    @PostMapping("/product/save")
    @AdminPrivilege(and = Privilege.理财配置)
    public Result edit(@RequestBody @Validated FinancialProductEditQuery financialProductQuery) {
        financialProductService.saveOrUpdate(financialProductQuery);
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/product/{productId}")
    @AdminPrivilege(and = Privilege.理财配置, api = "/management/financial/product/productId")
    public Result delete(@PathVariable Long productId) {
        financialProductService.delete(productId);
        return Result.success();
    }

    /**
     * 修改产品状态
     */
    @PostMapping("/product/status")
    @AdminPrivilege(and = Privilege.理财配置)
    public Result edit(@RequestBody @Validated FinancialProductEditStatusQuery query) {
        financialProductService.editProductStatus(query);
        return Result.success();
    }

    /**
     * 产品列表
     */
    @GetMapping("/products")
    @AdminPrivilege(and = Privilege.理财配置)
    public Result products(PageQuery<FinancialProduct> page, FinancialProductsQuery query) {
        IPage<MFinancialProductVO> financialProductVOIPage = financialProductService.mSelectListByQuery(page.page(), query);
        return Result.success(financialProductVOIPage);
    }

    /**
     * 订单列表
     */
    @GetMapping("/orders")
    @AdminPrivilege(and = Privilege.理财配置)
    public Result orders(PageQuery<OrderFinancialVO> page, FinancialOrdersQuery query) {
        query.setDefaultChargeType(List.of(ChargeType.purchase, ChargeType.redeem, ChargeType.transfer, ChargeType.settle));
        IPage<OrderFinancialVO> financialOrderVOIPage = financialService.orderPage(page.page(), query);
        return Result.success(financialOrderVOIPage);
    }


    /**
     * 用户理财收益记录
     */
    @GetMapping("/record/income")
    @AdminPrivilege(and = Privilege.理财配置)
    public Result income(PageQuery<FinancialIncomeAccrueDTO> page, FinancialProductIncomeQuery query) {
        return Result.success(financialService.incomeRecordPage(page.page(), query));
    }

    /**
     * 用户理财收益记录列表累计信息
     */
    @GetMapping("/record/income/data")
    @AdminPrivilege(and = Privilege.理财配置)
    public Result incomeData(FinancialProductIncomeQuery query) {
        return Result.success(financialService.incomeSummaryData(query));
    }

    /**
     * 手动更新展板数据
     */
    @PutMapping("/board/manual")
    @AdminPrivilege(and = Privilege.理财配置)
    public Result boardManual(@RequestBody TimeQuery query) {
        financialService.boardManual(query);
        return Result.success();
    }

    /**
     * 产品下拉
     */
    @GetMapping("/product/dropdown")
    public Result dropdownList(ProductType type) {
        List<FundProductBindDropdownVO> dropdownVOS = financialService.fundProductBindDropdownList(type);
        return Result.success(dropdownVOS);
    }

    /**
     * 修改产品推荐状态
     */
    @PutMapping("/product/recommend")
    public Result productRecommend(@RequestBody String str) {
        JSONObject jsonObject = JSONUtil.parseObj(str);
        Long id = jsonObject.get("id", Long.class);
        Boolean recommend = jsonObject.get("recommend", Boolean.class);
        financialProductService.modifyRecommend(id, recommend);
        return Result.success();
    }


}
