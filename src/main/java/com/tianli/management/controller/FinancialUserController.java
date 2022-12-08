package com.tianli.management.controller;

import com.tianli.account.service.AccountBalanceService;
import com.tianli.address.mapper.Address;
import com.tianli.common.PageQuery;
import com.tianli.exception.Result;
import com.tianli.financial.service.FinancialService;
import com.tianli.management.query.UidsQuery;
import com.tianli.sso.permission.AdminPrivilege;
import com.tianli.sso.permission.Privilege;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-20
 **/
@RestController
@RequestMapping("/management/financial/user")
public class FinancialUserController {

    /**
     * 理财用户管理 列表数据
     */
    @GetMapping("/list")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result user(PageQuery<Address> page, String uid) {
        return Result.success().setData(financialService.financialUserPage(uid, page.page()));
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
     * 用户资产
     */
    @GetMapping("assets/{uid}")
    @AdminPrivilege(and = Privilege.理财管理, api = "/management/financial/user/assets/uid")
    public Result assets(@PathVariable Long uid) {
        return Result.success().setData(accountBalanceService.getUserAssetsVO(uid));
    }

    /**
     * 用户资产
     */
    @GetMapping("assets/uids")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result assetsUids(@RequestBody UidsQuery query) {
        return Result.success().setData(accountBalanceService.getUserAssetsVO(query.getUids()));
    }

    /**
     * 用户资产
     */
    @GetMapping("assets/map")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result assetsMap(@RequestBody UidsQuery query) {
        return Result.success().setData(accountBalanceService.getUserAssetsVOMap(query.getUids()));
    }

    @Resource
    private FinancialService financialService;
    @Resource
    private AccountBalanceService accountBalanceService;
}
