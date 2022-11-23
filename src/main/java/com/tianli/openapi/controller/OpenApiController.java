package com.tianli.openapi.controller;

import com.tianli.account.service.AccountBalanceService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.openapi.query.OpenapiOperationQuery;
import com.tianli.openapi.service.OpenApiService;
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
     * 划转
     */
    @GetMapping("/balances/{uid}")
    public Result balance(@PathVariable Long uid,
                          @RequestHeader("sign") String sign,
                          @RequestHeader("timestamp") String timestamp) {

        if (!Crypto.hmacToString(DigestFactory.createSHA256(), "vUfV1n#JdyG^oKCb", timestamp).equals(sign)) {
            throw ErrorCodeEnum.SIGN_ERROR.generalException();
        }

        return Result.success(accountBalanceService.getAccountBalanceList(uid));
    }


}
