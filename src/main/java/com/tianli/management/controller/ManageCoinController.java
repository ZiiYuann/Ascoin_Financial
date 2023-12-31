package com.tianli.management.controller;

import com.tianli.chain.entity.Coin;
import com.tianli.chain.entity.CoinBase;
import com.tianli.chain.query.CoinReviewConfigIoUQuery;
import com.tianli.chain.service.CoinBaseService;
import com.tianli.chain.service.CoinReviewConfigService;
import com.tianli.chain.service.CoinService;
import com.tianli.common.PageQuery;
import com.tianli.exception.Result;
import com.tianli.management.query.*;
import com.tianli.sso.permission.AdminPrivilege;
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
public class ManageCoinController {

    @Resource
    private CoinService coinService;
    @Resource
    private CoinBaseService coinBaseService;
    @Resource
    private CoinReviewConfigService coinReviewConfigService;

    /**
     * 新增或者更新币别信息
     */
    @AdminPrivilege(api = "/management/coin/id")
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) throws InterruptedException {
        coinService.delete(id);
        Thread.sleep(1000);
        coinBaseService.flushPushListCache();
        coinService.deletePushListCache();
        return Result.success();
    }

    /**
     * 新增或者更新币别信息
     */
    @PostMapping("/save")
    @AdminPrivilege
    public Result saveOrUpdate(@RequestBody @Valid CoinIoUQuery query) {
        String nickname = AdminContent.get().getNickname();
        coinService.saveOrUpdate(nickname, query);
        coinBaseService.deleteCache(query.getName());
        coinBaseService.flushPushListCache();
        return Result.success();
    }

    /**
     * 币别上架
     */
    @AdminPrivilege
    @PostMapping("/push")
    public Result<Void> push(@RequestBody @Valid CoinStatusQuery query) {
        String nickname = AdminContent.get().getNickname();
        coinService.push(nickname, query);
        return new Result<>();
    }

    @AdminPrivilege
    @PostMapping("/close")
    public Result<Void> close(@RequestBody @Valid CoinStatusQuery query) {
        String nickname = AdminContent.get().getNickname();
        coinService.close(nickname, query);
        return new Result<>();
    }

    /**
     * 币别列表
     */
    @AdminPrivilege
    @GetMapping("/list")
    public Result list(PageQuery<Coin> pageQuery, CoinsQuery query) {
        return Result.success().setData(coinBaseService.list(pageQuery.page(), query));
    }

    /**
     * 基础币别列表
     */
    @AdminPrivilege
    @GetMapping("/base/list")
    public Result<Object> baseList(PageQuery<CoinBase> pageQuery, CoinsQuery query) {
        return Result.success().setData(coinBaseService.baseList(pageQuery.page(), query));
    }

    /**
     * 币别提币配置
     */
    @AdminPrivilege
    @PostMapping("/withdraw")
    public Result saveOrUpdate(@RequestBody @Valid CoinWithdrawQuery query) {
        String nickname = AdminContent.get().getNickname();
        coinService.withdrawConfig(nickname, query);
        coinService.deletePushListCache();
        return Result.success();
    }

    /**
     * Assure提币配置
     */
    @AdminPrivilege
    @PostMapping("/base/withdraw")
    public Result updateConfig(@RequestBody @Valid CoinBaseWithdrawQuery query) {
        coinBaseService.updateConfig(query);
        return Result.success();
    }

    /**
     * 保存币别审核配置
     */
    @AdminPrivilege
    @PostMapping("/review/config")
    public Result reviewConfigSave(@RequestBody @Valid CoinReviewConfigIoUQuery query) {
        String nickname = AdminContent.get().getNickname();
        coinReviewConfigService.reviewConfig(nickname, query);
        return Result.success();
    }

    /**
     * 获取币别审核配置
     */
    @AdminPrivilege
    @GetMapping("/review/config")
    public Result reviewConfigGet() {
        return Result.success(coinReviewConfigService.reviewConfig());
    }

}
