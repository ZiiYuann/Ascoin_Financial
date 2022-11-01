package com.tianli.openapi.controller;

import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.openapi.query.RewardQuery;
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

    /**
     * 奖励接口
     */
    @PostMapping("/reward")
    public Result reward(@RequestBody @Valid RewardQuery query,
                         @RequestHeader("sign") String sign,
                         @RequestHeader("timestamp") String timestamp) {

        if (!Crypto.hmacToString(DigestFactory.createSHA256(), "vUfV1n#JdyG^oKUp", timestamp).equals(sign)) {
            throw ErrorCodeEnum.SIGN_ERROR.generalException();
        }

        openApiService.reward(query);
        return Result.success();
    }


}
