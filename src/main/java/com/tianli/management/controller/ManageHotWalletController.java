package com.tianli.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.common.PageQuery;
import com.tianli.exception.Result;
import com.tianli.management.entity.HotWalletDetailed;
import com.tianli.management.query.HotWalletDetailedIoUQuery;
import com.tianli.management.query.HotWalletDetailedPQuery;
import com.tianli.management.service.HotWalletDetailedService;
import com.tianli.management.vo.HotWalletBalanceVO;
import com.tianli.management.vo.HotWalletDetailedSummaryDataVO;
import com.tianli.management.vo.HotWalletDetailedVO;
import com.tianli.sso.permission.AdminPrivilege;
import com.tianli.sso.permission.Privilege;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2022-08-17
 **/
@RestController
@RequestMapping("/management/hot/wallet/")
public class ManageHotWalletController {

    @Resource
    private HotWalletDetailedService hotWalletDetailedService;

    /**
     * 【热钱包管理】添加明细 或 修改明细
     */
    @PostMapping("/detailed")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result<Void> insertOrUpdate(@RequestBody @Valid HotWalletDetailedIoUQuery ioUQuery) {
        hotWalletDetailedService.insertOrUpdate(ioUQuery);
        return new Result<>();
    }

    /**
     * 【热钱包管理】添加明细 或 修改明细
     */
    @DeleteMapping("/detailed")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result<Void> insertOrUpdate(Long id) {
        hotWalletDetailedService.delete(id);
        return new Result<>();
    }

    /**
     * 【热钱包管理】明细列表
     */
    @GetMapping("/detailed")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result<IPage<HotWalletDetailedVO>> page(PageQuery<HotWalletDetailed> pageQuery, HotWalletDetailedPQuery query) {
        return new Result<>(hotWalletDetailedService.pageByQuery(pageQuery.page(), query));
    }

    /**
     * 【热钱包管理】明细列表统计数据
     */
    @GetMapping("/detailed/data")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result<HotWalletDetailedSummaryDataVO> summaryData(HotWalletDetailedPQuery query) {
        return new Result<>(hotWalletDetailedService.summaryData(query));
    }

    /**
     * 【热钱包管理】余额
     */
    @GetMapping("/detailed/balance")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result<List<HotWalletBalanceVO>> balance() {
        return new Result<>(hotWalletDetailedService.balance());
    }


    /**
     * 【热钱包管理】主币余额
     */
    @GetMapping("/detailed/main/balance")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result<List<HotWalletBalanceVO>> mainBalance() {
        return new Result<>(hotWalletDetailedService.mainBalance());
    }

}
