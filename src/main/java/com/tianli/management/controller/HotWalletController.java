package com.tianli.management.controller;

import com.tianli.common.PageQuery;
import com.tianli.exception.Result;
import com.tianli.management.query.HotWalletDetailedIoUQuery;
import com.tianli.management.query.HotWalletDetailedPQuery;
import com.tianli.management.service.HotWalletDetailedService;
import com.tianli.management.entity.HotWalletDetailed;
import com.tianli.sso.permission.AdminPrivilege;
import com.tianli.sso.permission.Privilege;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * @author chenb
 * @apiNote
 * @since 2022-08-17
 **/
@RestController
@RequestMapping("/management/hot/wallet/")
public class HotWalletController {

    @Resource
    private HotWalletDetailedService hotWalletDetailedService;

    /**
     * 【热钱包管理】添加明细 或 修改明细
     */
    @PostMapping("/detailed")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result insertOrUpdate(@RequestBody @Valid HotWalletDetailedIoUQuery ioUQuery) {
        hotWalletDetailedService.insertOrUpdate(ioUQuery);
        return Result.success();
    }

    /**
     * 【热钱包管理】添加明细 或 修改明细
     */
    @DeleteMapping("/detailed")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result insertOrUpdate(Long id) {
        hotWalletDetailedService.delete(id);
        return Result.success();
    }

    /**
     * 【热钱包管理】明细列表
     */
    @GetMapping("/detailed")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result page(PageQuery<HotWalletDetailed> pageQuery, HotWalletDetailedPQuery query) {
        return Result.success().setData(hotWalletDetailedService.pageByQuery(pageQuery.page(), query));
    }

    /**
     * 【热钱包管理】明细列表统计数据
     */
    @GetMapping("/detailed/data")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result summaryData(HotWalletDetailedPQuery query) {
        return Result.success().setData(hotWalletDetailedService.SummaryData(query));
    }

    /**
     * 【热钱包管理】余额
     */
    @GetMapping("/detailed/balance")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result balance() {
        return Result.success().setData(hotWalletDetailedService.balance());
    }

}
