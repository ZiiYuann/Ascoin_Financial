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
import com.tianli.management.vo.FinancialSummaryDataVO;
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


    /**
     * 新增或者修改产品
     */
    @PostMapping("/product/save")
    @AdminPrivilege(and = Privilege.理财配置)
    public Result<Void> edit(@RequestBody @Validated FinancialProductEditQuery financialProductQuery) {
        financialProductService.saveOrUpdate(financialProductQuery);
        return new Result<>();
    }

    /**
     * 删除
     */
    @DeleteMapping("/product/{productId}")
    @AdminPrivilege(and = Privilege.理财配置, api = "/management/financial/product/productId")
    public Result<Void> delete(@PathVariable Long productId) {
        financialProductService.delete(productId);
        return new Result<>();
    }

    /**
     * 修改产品状态
     */
    @PostMapping("/product/status")
    @AdminPrivilege(and = Privilege.理财配置)
    public Result<Void> edit(@RequestBody @Validated FinancialProductEditStatusQuery query) {
        financialProductService.editProductStatus(query);
        return new Result<>();
    }

    /**
     * 产品列表
     */
    @GetMapping("/products")
    @AdminPrivilege(and = Privilege.理财配置)
    public Result<IPage<MFinancialProductVO>> products(PageQuery<FinancialProduct> page, FinancialProductsQuery query) {
        IPage<MFinancialProductVO> financialProductVOIPage = financialProductService.mSelectListByQuery(page.page(), query);
        return new Result<>(financialProductVOIPage);
    }

    /**
     * 订单列表
     */
    @GetMapping("/orders")
    @AdminPrivilege(and = Privilege.理财配置)
    public Result<IPage<OrderFinancialVO>> orders(PageQuery<OrderFinancialVO> page, FinancialOrdersQuery query) {
        query.setDefaultChargeType(List.of(ChargeType.purchase, ChargeType.redeem, ChargeType.transfer, ChargeType.settle));
        IPage<OrderFinancialVO> financialOrderVOIPage = financialService.orderPage(page.page(), query);
        return new Result<>(financialOrderVOIPage);
    }


    /**
     * 用户理财收益记录
     */
    @GetMapping("/record/income")
    @AdminPrivilege(and = Privilege.理财配置)
    public Result<IPage<FinancialIncomeAccrueDTO>> income(PageQuery<FinancialIncomeAccrueDTO> page
            , FinancialProductIncomeQuery query) {
        return new Result<>(financialService.incomeRecordPage(page.page(), query));
    }

    /**
     * 用户理财收益记录列表累计信息
     */
    @GetMapping("/record/income/data")
    @AdminPrivilege(and = Privilege.理财配置)
    public Result<FinancialSummaryDataVO> incomeData(FinancialProductIncomeQuery query) {
        return new Result<>(financialService.incomeSummaryData(query));
    }

    /**
     * 产品下拉
     */
    @GetMapping("/product/dropdown")
    public Result<List<FundProductBindDropdownVO>> dropdownList(ProductType type) {
        List<FundProductBindDropdownVO> dropdownVOS = financialService.fundProductBindDropdownList(type);
        return new Result<>(dropdownVOS);
    }

    /**
     * 修改产品推荐状态
     */
    @PutMapping("/product/recommend")
    public Result<Void> productRecommend(@RequestBody String str) {
        JSONObject jsonObject = JSONUtil.parseObj(str);
        Long id = jsonObject.get("id", Long.class);
        Integer recommendWeight = jsonObject.getInt("recommendWeight");
        Boolean recommend = jsonObject.get("recommend", Boolean.class);
        financialProductService.modifyRecommend(id, recommend, recommendWeight);
        return new Result<>();
    }


}
