package com.tianli.management.controller;

import com.tianli.address.mapper.Address;
import com.tianli.common.PageQuery;
import com.tianli.exception.Result;
import com.tianli.financial.service.FinancialService;
import com.tianli.sso.permission.AdminPrivilege;
import com.tianli.sso.permission.Privilege;
import org.springframework.web.bind.annotation.GetMapping;
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
public class FinancialUserController {

    /**
     * 理财用户管理 列表数据
     */
    @GetMapping("/list")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result user(PageQuery<Address> page, String uid) {
        return Result.success().setData(financialService.financialUserPage(uid,page.page()));
    }

    /**
     * 理财用户管理 上方累计数据
     */
    @GetMapping("/data")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result data(String uid) {
        return Result.success().setData(financialService.userSummaryData(uid));
    }

    @Resource
    private FinancialService financialService;
}
