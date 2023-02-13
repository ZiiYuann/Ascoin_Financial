package com.tianli.openapi.controller;

import com.tianli.account.service.AccountBalanceService;
import com.tianli.account.service.impl.AccountBalanceServiceImpl;
import com.tianli.charge.enums.ChargeType;
import com.tianli.accountred.entity.RedEnvelope;
import com.tianli.accountred.entity.RedEnvelopeSpiltGetRecord;
import com.tianli.accountred.service.RedEnvelopeService;
import com.tianli.accountred.service.RedEnvelopeSpiltService;
import com.tianli.accountred.vo.RedEnvelopeExternGetDetailsVO;
import com.tianli.charge.service.ChargeService;
import com.tianli.common.Constants;
import com.tianli.common.PageQuery;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.management.query.UidsQuery;
import com.tianli.openapi.dto.IdDto;
import com.tianli.openapi.dto.TransferResultDto;
import com.tianli.openapi.query.OpenapiOperationQuery;
import com.tianli.openapi.query.UserTransferQuery;
import com.tianli.openapi.service.OpenApiService;
import com.tianli.tool.IPUtils;
import com.tianli.tool.crypto.Crypto;
import com.tianli.tool.crypto.PBE;
import org.bouncycastle.crypto.util.DigestFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Objects;

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
    @Resource
    private RedEnvelopeService redEnvelopeService;
    @Resource
    private RedEnvelopeSpiltService redEnvelopeSpiltService;

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
     * 用户间划转
     */
    @PostMapping("/user/transfer")
    public Result<TransferResultDto> userTransfer(@RequestBody @Valid UserTransferQuery query,
                                                  @RequestHeader("sign") String sign,
                                                  @RequestHeader("timestamp") String timestamp) {
        if (!Crypto.hmacToString(DigestFactory.createSHA256(), "vUfV1n#JdyG^oKCb", timestamp).equals(sign)) {
            throw ErrorCodeEnum.SIGN_ERROR.generalException();
        }
        return new Result<>(openApiService.transfer(query));
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
     * 用户资产
     */
    @GetMapping("/assets/{uid}")
    public Result assets(@PathVariable Long uid,
                         @RequestHeader("sign") String sign,
                         @RequestHeader("timestamp") String timestamp) {
        if (!Crypto.hmacToString(DigestFactory.createSHA256(), "vUfV1n#JdyG^oKCb", timestamp).equals(sign)) {
            throw ErrorCodeEnum.SIGN_ERROR.generalException();
        }
        return Result.success().setData(accountBalanceService.getAllUserAssetsVO(uid));
    }

    /**
     * 用户资产
     */
    @PostMapping("/assets/uids")
    public Result assetsUids(@RequestBody(required = false) UidsQuery query,
                             @RequestHeader("sign") String sign,
                             @RequestHeader("timestamp") String timestamp) {

        if (Objects.isNull(query)) {
            return Result.success();
        }
        if (!Crypto.hmacToString(DigestFactory.createSHA256(), "vUfV1n#JdyG^oKCb", timestamp).equals(sign)) {
            throw ErrorCodeEnum.SIGN_ERROR.generalException();
        }
        return Result.success().setData(accountBalanceService.getAllUserAssetsVO(query.getUids()));
    }

    /**
     * 用户资产
     */
    @PostMapping("/assets/map")
    public Result assetsMap(@RequestBody(required = false) UidsQuery query,
                            @RequestHeader("sign") String sign,
                            @RequestHeader("timestamp") String timestamp) {
        if (Objects.isNull(query)) {
            return Result.success();
        }
        if (!Crypto.hmacToString(DigestFactory.createSHA256(), "vUfV1n#JdyG^oKCb", timestamp).equals(sign)) {
            throw ErrorCodeEnum.SIGN_ERROR.generalException();
        }
        return Result.success().setData(accountBalanceService.getUserAssetsVOMap(query.getUids()));
    }

    /**
     * nft返还gas
     */
    @PostMapping("/return/gas")
    public Result returnGas(@RequestBody @Valid OpenapiOperationQuery query,
                            @RequestHeader("sign") String sign,
                            @RequestHeader("timestamp") String timestamp) {

        if (!Crypto.hmacToString(DigestFactory.createSHA256(), "vUfV1n#JdyG^oKCb", timestamp).equals(sign)) {
            throw ErrorCodeEnum.SIGN_ERROR.generalException();
        }
        openApiService.returnGas(query);
        return Result.success();
    }

    /**
     * cpl金币奖励
     */
    @PostMapping("/gold/exchange")
    public Result goldExchange(@RequestBody @Valid OpenapiOperationQuery query,
                            @RequestHeader("sign") String sign,
                            @RequestHeader("timestamp") String timestamp) {

        if (!Crypto.hmacToString(DigestFactory.createSHA256(), "vUfV1n#JdyG^oKCb", timestamp).equals(sign)) {
            throw ErrorCodeEnum.SIGN_ERROR.generalException();
        }
        openApiService.goldExchange(query);
        return Result.success();
    }

    /**
     * 领取站外红包
     */
    @GetMapping("/red/extern/get")
    public Result externRedGet(HttpServletRequest request, String content) {
        String ip = IPUtils.getIpAddress(request);
        // todo 指纹
        String fingerprint;
        String id = PBE.decryptBase64(Constants.RED_SALT, Constants.RED_SECRET_KEY, content);
        return Result.success().setData(redEnvelopeService.getExternCode(Long.parseLong(id)));
    }

    /**
     * 领取站外红包记录
     */
    @GetMapping("/red/extern/record")
    public Result externRedRecord(String content, PageQuery<RedEnvelopeSpiltGetRecord> pageQuery) {

        String rid = PBE.decryptBase64(Constants.RED_SALT, Constants.RED_SECRET_KEY, content);
        RedEnvelope redEnvelope = redEnvelopeService.getWithCache(Long.parseLong(rid));

        RedEnvelopeExternGetDetailsVO redEnvelopeExternGetDetailsVO =
                redEnvelopeSpiltService.getExternDetailsRedis(redEnvelope, pageQuery);
        return Result.success().setData(redEnvelopeExternGetDetailsVO);
    }

}
