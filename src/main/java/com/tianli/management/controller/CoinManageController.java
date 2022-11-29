package com.tianli.management.controller;

import com.tianli.chain.entity.Coin;
import com.tianli.chain.service.CoinService;
import com.tianli.common.PageQuery;
import com.tianli.exception.Result;
import com.tianli.management.query.CoinIoUQuery;
import com.tianli.management.query.CoinStatusQuery;
import com.tianli.management.query.CoinsQuery;
import com.tianli.sso.init.RequestInitService;
import com.tianli.sso.permission.AdminPrivilege;
import com.tianli.sso.permission.Privilege;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * @author chenb
 * @apiNote
 * @since 2022-11-22
 **/
@RestController
@RequestMapping("/management/coin")
public class CoinManageController {

    @Resource
    private CoinService coinService;
    @Resource
    private RequestInitService requestInitService;

    /**
     * 新增或者更新币别信息
     */
    @PostMapping("/save")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result saveOrUpdate(@RequestBody @Valid CoinIoUQuery query) {
        Long uid = requestInitService.uid();
        coinService.saveOrUpdate(uid, query);
        coinService.flushCache();
        return Result.success();
    }

    /**
     * 币别上架
     */
    @PostMapping("/push")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result saveOrUpdate(@RequestBody @Valid CoinStatusQuery query) {
        Long uid = requestInitService.uid();
        coinService.push(uid, query);
        return Result.success();
    }

    /**
     * 币别上架
     */
    @GetMapping("/list")
    @AdminPrivilege(and = Privilege.理财管理)
    public Result list(PageQuery<Coin> pageQuery, CoinsQuery query) {
        return Result.success().setData(coinService.list(pageQuery.page(), query));
    }
}
