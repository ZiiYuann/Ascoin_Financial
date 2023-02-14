package com.tianli.openapi.controller;

import com.tianli.account.service.AccountBalanceService;
import com.tianli.account.service.impl.AccountBalanceServiceImpl;
import com.tianli.account.vo.AccountUserTransferVO;
import com.tianli.accountred.entity.RedEnvelope;
import com.tianli.accountred.entity.RedEnvelopeSpiltGetRecord;
import com.tianli.accountred.service.RedEnvelopeService;
import com.tianli.accountred.service.RedEnvelopeSpiltService;
import com.tianli.accountred.vo.ORedEnvelopVO;
import com.tianli.accountred.vo.RedEnvelopeExchangeCodeVO;
import com.tianli.accountred.vo.RedEnvelopeExternGetDetailsVO;
import com.tianli.charge.service.ChargeService;
import com.tianli.common.Constants;
import com.tianli.common.PageQuery;
import com.tianli.common.RedisConstants;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.management.query.UidsQuery;
import com.tianli.openapi.dto.TransferResultDto;
import com.tianli.openapi.query.OpenapiOperationQuery;
import com.tianli.openapi.query.OpenapiRedQuery;
import com.tianli.openapi.query.UserTransferQuery;
import com.tianli.openapi.service.OpenApiService;
import com.tianli.rpc.RpcService;
import com.tianli.rpc.dto.UserInfoDTO;
import com.tianli.tool.IPUtils;
import com.tianli.tool.crypto.Crypto;
import com.tianli.tool.crypto.PBE;
import org.bouncycastle.crypto.util.DigestFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
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
    @Resource
    private RpcService rpcService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

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
     * 订单信息
     */
    @GetMapping("/transferInfo/{externalPk}")
    public Result<AccountUserTransferVO> transferOrder(@PathVariable Long externalPk,
                                                       @RequestHeader("sign") String sign,
                                                       @RequestHeader("timestamp") String timestamp) {

        if (!Crypto.hmacToString(DigestFactory.createSHA256(), "vUfV1n#JdyG^oKCb", timestamp).equals(sign)) {
            throw ErrorCodeEnum.SIGN_ERROR.generalException();
        }
        return new Result<>(openApiService.transferOrder(externalPk));
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
    public Result<Void> returnGas(@RequestBody @Valid OpenapiOperationQuery query,
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
    public Result<Void> goldExchange(@RequestBody @Valid OpenapiOperationQuery query,
                                     @RequestHeader("sign") String sign,
                                     @RequestHeader("timestamp") String timestamp) {

        if (!Crypto.hmacToString(DigestFactory.createSHA256(), "vUfV1n#JdyG^oKCb", timestamp).equals(sign)) {
            throw ErrorCodeEnum.SIGN_ERROR.generalException();
        }
        openApiService.goldExchange(query);
        return new Result<>();
    }

    /**
     * 领取站外红包记录
     */
    @GetMapping("/red/extern/record")
    public Result<RedEnvelopeExternGetDetailsVO> externRedRecord(OpenapiRedQuery query
            , PageQuery<RedEnvelopeSpiltGetRecord> pageQuery) {

        String rid = PBE.decryptBase64(Constants.RED_SALT, Constants.RED_SECRET_KEY, query.getContext());
        RedEnvelope redEnvelope = redEnvelopeService.getWithCache(Long.parseLong(rid));

        var vo =
                redEnvelopeSpiltService.getExternDetailsRedis(redEnvelope, pageQuery);
        UserInfoDTO userInfoDTO = rpcService.userInfoDTO(redEnvelope.getUid());
        vo.setNickname(userInfoDTO.getNickname());
        return new Result<>(vo);
    }

    /**
     * 领取站外红包
     */
    @GetMapping("/red/extern/get")
    public Result<RedEnvelopeExchangeCodeVO> externRedGet(@RequestHeader("fingerprint") String fingerprint
            , HttpServletRequest request, OpenapiRedQuery query) {
        String ip = IPUtils.getIpAddress(request);
        String id = PBE.decryptBase64(Constants.RED_SALT, Constants.RED_SECRET_KEY, query.getContext());

        String key = RedisConstants.RED_ENVELOPE_LIMIT + id;
        // todo 过期时间
        Long count = stringRedisTemplate.opsForSet().add(key, ip + ":" + fingerprint);
        if (Objects.isNull(count) || count == 0) {
            throw ErrorCodeEnum.RED_EXTERN_LIMIT.generalException();
        }

        return new Result<>(redEnvelopeService.getExternCode(Long.parseLong(id)));
    }

    /**
     * 没必要的接口，前端不愿意加缓存非要后端加
     */
    @GetMapping("/red/extern/{exchangeCode}")
    public Result<RedEnvelopeExchangeCodeVO> externRedGet(@PathVariable String exchangeCode) {
        return new Result<>(redEnvelopeService.getExternCode(exchangeCode));
    }

    /**
     * 站外红包信息
     */
    @GetMapping("/red/extern/info")
    public Result<ORedEnvelopVO> externRedGet(OpenapiRedQuery query) {
        String rid = PBE.decryptBase64(Constants.RED_SALT, Constants.RED_SECRET_KEY, query.getContext());
        RedEnvelope redEnvelope = redEnvelopeService.getWithCache(Long.parseLong(rid));
        UserInfoDTO userInfoDTO = rpcService.userInfoDTO(redEnvelope.getUid());
        ORedEnvelopVO vo = ORedEnvelopVO.builder()
                .coin(redEnvelope.getCoin())
                .nickname(userInfoDTO.getNickname())
                .remarks(redEnvelope.getRemarks())
                .build();
        return new Result<>(vo);
    }


}
