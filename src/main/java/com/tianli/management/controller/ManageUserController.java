package com.tianli.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.address.mapper.Address;
import com.tianli.common.PageQuery;
import com.tianli.exception.Result;
import com.tianli.product.entity.ProductHoldRecord;
import com.tianli.product.financial.entity.FinancialProduct;
import com.tianli.product.financial.query.ProductHoldQuery;
import com.tianli.product.financial.service.FinancialService;
import com.tianli.product.financial.vo.HoldProductVo;
import com.tianli.management.service.ManageUserService;
import com.tianli.management.vo.MUserListVO;
import com.tianli.openapi.query.OpenapiAccountQuery;
import com.tianli.openapi.service.OpenApiService;
import com.tianli.openapi.vo.StatisticsData;
import com.tianli.sso.permission.AdminPrivilege;
import com.tianli.sso.permission.Privilege;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-20
 **/
@RestController
@RequestMapping("/management/financial/user")
public class ManageUserController {

    /**
     * 理财用户管理 列表数据
     */
    @GetMapping("/list")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result<IPage<MUserListVO>> user(PageQuery<Address> page, String uid) {
        return new Result<>(manageUserService.financialUserPage(uid, page.page()));
    }

    /**
     * 理财用户管理-持仓用户
     */
    @GetMapping("/hold/record")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result<IPage<HoldProductVo>> holdRecord(ProductHoldQuery query, PageQuery<ProductHoldRecord> page) {
        return new Result<>(manageUserService.userHoldRecordPage(query, page.page()));
    }

    /**
     * 理财用户管理-持仓中
     */
    @GetMapping("/hold")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result<IPage<HoldProductVo>> userHold(PageQuery<FinancialProduct> pageQuery
            , ProductHoldQuery query) {
        return new Result<>(financialService.holdProductPage(pageQuery.page(), query));
    }

    /**
     * 理财用户管理 上方累计数据
     */
    @GetMapping("/data")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result data(String uid) {
        return Result.success().setData(financialService.userSummaryData(uid));
    }

    /**
     * 用户资金数据
     */
    @GetMapping("/details/{uid}")
    @AdminPrivilege(and = Privilege.理财管理, api = "/management/financial/user/details/uid")
    public Result details(@PathVariable Long uid) {
        return Result.success().setData(financialService.userAmountDetailsVO(uid));
    }

    /**
     * 账户信息
     */
    @GetMapping("/account")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result statisticsData(OpenapiAccountQuery query) {
        return Result.success(openApiService.accountData(query));
    }

    /**
     * 账户信息
     */
    @GetMapping("/account/sub")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result statisticsData(Long chatId, PageQuery<StatisticsData> pageQuery) {
        return Result.success(openApiService.accountSubData(chatId, pageQuery));
    }

    @Resource
    private FinancialService financialService;
    @Resource
    private OpenApiService openApiService;
    @Resource
    private ManageUserService manageUserService;
}
