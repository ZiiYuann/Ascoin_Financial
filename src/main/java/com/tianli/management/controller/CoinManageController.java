package com.tianli.management.controller;

import com.tianli.chain.entity.Coin;
import com.tianli.chain.service.CoinBaseService;
import com.tianli.chain.service.CoinService;
import com.tianli.common.PageQuery;
import com.tianli.exception.Result;
import com.tianli.management.query.CoinIoUQuery;
import com.tianli.management.query.CoinStatusQuery;
import com.tianli.management.query.CoinWithdrawQuery;
import com.tianli.management.query.CoinsQuery;
import com.tianli.sso.permission.admin.AdminContent;
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
    private CoinBaseService coinBaseService;

    /**
     * 新增或者更新币别信息
     */
    @PostMapping("/save")
    public Result saveOrUpdate(@RequestBody @Valid CoinIoUQuery query) {
        Long uid = AdminContent.get().getAid();
        coinService.saveOrUpdate(uid, query);
        coinService.flushCache();
        return Result.success();
    }

    /**
     * 币别上架
     */
    @PostMapping("/push")
    public Result saveOrUpdate(@RequestBody @Valid CoinStatusQuery query) {
        Long uid = AdminContent.get().getAid();
        coinService.push(uid, query);
        return Result.success();
    }

    /**
     * 币别上架
     */
    @GetMapping("/list")
    public Result list(PageQuery<Coin> pageQuery, CoinsQuery query) {
        return Result.success().setData(coinBaseService.list(pageQuery.page(), query));
    }

    /**
     * 币别提币配置
     */
    @PostMapping("/withdraw")
    public Result saveOrUpdate(@RequestBody @Valid CoinWithdrawQuery query) {
        Long uid = AdminContent.get().getAid();
        coinService.withdrawConfig(uid, query);
        return Result.success();
    }


}
