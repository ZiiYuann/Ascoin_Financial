package com.tianli.openapi.controller;

import com.tianli.account.service.AccountBalanceService;
import com.tianli.account.service.impl.AccountBalanceServiceImpl;
import com.tianli.charge.service.ChargeService;
import com.tianli.common.PageQuery;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.management.query.UidsQuery;
import com.tianli.openapi.query.OpenapiAccountQuery;
import com.tianli.openapi.query.OpenapiOperationQuery;
import com.tianli.openapi.service.OpenApiService;
import com.tianli.openapi.vo.StatisticsData;
import com.tianli.sso.permission.AdminPrivilege;
import com.tianli.sso.permission.Privilege;
import com.tianli.tool.crypto.Crypto;
import org.bouncycastle.crypto.util.DigestFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * @author chenb
 * @apiNote
 * @since 2022-10-24
 **/
@RestController
@RequestMapping("/openapi")
public class OpenApiController {

    @Resource
    private OpenApiService openApiService;
    @Resource
    private AccountBalanceServiceImpl accountBalanceServiceImpl;
    @Resource
    private ChargeService chargeService;
    @Resource
    private AccountBalanceService accountBalanceService;

    /**
     * 奖励接口
     */
    @PostMapping("/reward")
    public Result reward(@RequestBody @Valid OpenapiOperationQuery query,
                         @RequestHeader("sign") String sign,
                         @RequestHeader("timestamp") String timestamp) {

        if (!Crypto.hmacToString(DigestFactory.createSHA256(), "vUfV1n#JdyG^oKCb", timestamp).equals(sign)) {
            throw ErrorCodeEnum.SIGN_ERROR.generalException();
        }

        return Result.success(openApiService.reward(query));
    }

    /**
     * 划转
     */
    @PostMapping("/transfer")
    public Result transfer(@RequestBody @Valid OpenapiOperationQuery query,
                           @RequestHeader("sign") String sign,
                           @RequestHeader("timestamp") String timestamp) {

        if (!Crypto.hmacToString(DigestFactory.createSHA256(), "vUfV1n#JdyG^oKCb", timestamp).equals(sign)) {
            throw ErrorCodeEnum.SIGN_ERROR.generalException();
        }

        return Result.success(openApiService.transfer(query));
    }

    /**
     * 余额
     */
    @GetMapping("/balances/{uid}")
    public Result balance(@PathVariable Long uid,
                          @RequestHeader("sign") String sign,
                          @RequestHeader("timestamp") String timestamp) {

        if (!Crypto.hmacToString(DigestFactory.createSHA256(), "vUfV1n#JdyG^oKCb", timestamp).equals(sign)) {
            throw ErrorCodeEnum.SIGN_ERROR.generalException();
        }

        return Result.success(accountBalanceServiceImpl.accountList(uid));
    }

    /**
     * 提现记录
     */
    @GetMapping("/orders/withdraw/{uid}")
    public Result withdrawRecords(@PathVariable Long uid,
                                  @RequestHeader("sign") String sign,
                                  @RequestHeader("timestamp") String timestamp) {

        if (!Crypto.hmacToString(DigestFactory.createSHA256(), "vUfV1n#JdyG^oKCb", timestamp).equals(sign)) {
            throw ErrorCodeEnum.SIGN_ERROR.generalException();
        }

        return Result.success(accountBalanceServiceImpl.accountList(uid));
    }

    /**
     * 订单信息
     */
    @GetMapping("/order/{id}")
    public Result order(@PathVariable Long id,
                        @RequestHeader("sign") String sign,
                        @RequestHeader("timestamp") String timestamp) {

        if (!Crypto.hmacToString(DigestFactory.createSHA256(), "vUfV1n#JdyG^oKCb", timestamp).equals(sign)) {
            throw ErrorCodeEnum.SIGN_ERROR.generalException();
        }
        return Result.success(chargeService.chargeOrderDetails(id));
    }

    /**
     * 账户信息
     */
    @GetMapping("/account")
    public Result statisticsData(OpenapiAccountQuery query) {
        return Result.success(openApiService.accountData(query));
    }

    /**
     * 账户信息
     */
    @GetMapping("/account/sub")
    public Result statisticsData(Long chatId, PageQuery<StatisticsData> pageQuery) {
        return Result.success(openApiService.accountSubData(chatId, pageQuery));
    }


    /**
     * 用户资产
     */
    @GetMapping("/assets/{uid}")
    public Result assets(@PathVariable Long uid) {
        return Result.success().setData(accountBalanceService.getUserAssetsVO(uid));
    }

    /**
     * 用户资产
     */
    @PostMapping("/assets/uids")
    public Result assetsUids(@RequestBody UidsQuery query) {
        return Result.success().setData(accountBalanceService.getUserAssetsVO(query.getUids()));
    }

    /**
     * 用户资产
     */
    @PostMapping("/assets/map")
    public Result assetsMap(@RequestBody UidsQuery query) {
        return Result.success().setData(accountBalanceService.getUserAssetsVOMap(query.getUids()));
    }

}
