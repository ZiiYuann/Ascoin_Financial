package com.tianli.openapi.controller;

import com.tianli.account.service.AccountBalanceService;
import com.tianli.account.vo.AccountBalanceVO;
import com.tianli.account.vo.AccountUserTransferVO;
import com.tianli.account.vo.UserAssetsVO;
import com.tianli.charge.service.ChargeService;
import com.tianli.charge.vo.OrderChargeInfoVO;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.management.query.FinancialBoardQuery;
import com.tianli.management.query.UidsQuery;
import com.tianli.openapi.dto.IdDto;
import com.tianli.openapi.dto.TransferResultDto;
import com.tianli.openapi.dto.WalletBoardDTO;
import com.tianli.openapi.enums.WalletBoardType;
import com.tianli.openapi.query.OpenapiOperationQuery;
import com.tianli.openapi.query.UserTransferQuery;
import com.tianli.openapi.service.OpenApiService;
import com.tianli.tool.crypto.Crypto;
import org.bouncycastle.crypto.util.DigestFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

/**
 * @author chenb
 * @apiNote
 * @since 2022-10-24
 **/
@RestController
@RequestMapping("/openapi")
public class OpenApiController {

    private static final String SIGN_KEY = "vUfV1n#JdyG^oKCb";

    @Resource
    private OpenApiService openApiService;
    @Resource
    private AccountBalanceService accountBalanceService;
    @Resource
    private ChargeService chargeService;

    /**
     * 奖励接口
     */
    @PostMapping("/reward")
    public Result<IdDto> reward(@RequestBody @Valid OpenapiOperationQuery query,
                                @RequestHeader("sign") String sign,
                                @RequestHeader("timestamp") String timestamp) {


        if (!Crypto.hmacToString(DigestFactory.createSHA256(), SIGN_KEY, timestamp).equals(sign)) {
            throw ErrorCodeEnum.SIGN_ERROR.generalException();
        }

        return Result.success(openApiService.reward(query));
    }

    /**
     * 划转
     */
    @PostMapping("/transfer")
    public Result<IdDto> transfer(@RequestBody @Valid OpenapiOperationQuery query,
                                  @RequestHeader("sign") String sign,
                                  @RequestHeader("timestamp") String timestamp) {

        if (!Crypto.hmacToString(DigestFactory.createSHA256(), SIGN_KEY, timestamp).equals(sign)) {
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
        if (!Crypto.hmacToString(DigestFactory.createSHA256(), SIGN_KEY, timestamp).equals(sign)) {
            throw ErrorCodeEnum.SIGN_ERROR.generalException();
        }
        return new Result<>(openApiService.transfer(query));
    }

    /**
     * 余额
     */
    @GetMapping({"/balances/{uid}", "/orders/withdraw/{uid}"})
    public Result<List<AccountBalanceVO>> balance(@PathVariable Long uid,
                                                  @RequestHeader("sign") String sign,
                                                  @RequestHeader("timestamp") String timestamp) {

        if (!Crypto.hmacToString(DigestFactory.createSHA256(), SIGN_KEY, timestamp).equals(sign)) {
            throw ErrorCodeEnum.SIGN_ERROR.generalException();
        }
        return Result.success(accountBalanceService.accountList(uid));
    }

    /**
     * 订单信息
     */
    @GetMapping("/order/{id}")
    public Result<OrderChargeInfoVO> order(@PathVariable Long id,
                                           @RequestHeader("sign") String sign,
                                           @RequestHeader("timestamp") String timestamp) {

        if (!Crypto.hmacToString(DigestFactory.createSHA256(), SIGN_KEY, timestamp).equals(sign)) {
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

        if (!Crypto.hmacToString(DigestFactory.createSHA256(), SIGN_KEY, timestamp).equals(sign)) {
            throw ErrorCodeEnum.SIGN_ERROR.generalException();
        }
        return new Result<>(openApiService.transferOrder(externalPk));
    }

    /**
     * 用户资产
     */
    @GetMapping("/assets/{uid}")
    public Result<UserAssetsVO> assets(@PathVariable Long uid,
                                       @RequestHeader("sign") String sign,
                                       @RequestHeader("timestamp") String timestamp) {
        if (!Crypto.hmacToString(DigestFactory.createSHA256(), SIGN_KEY, timestamp).equals(sign)) {
            throw ErrorCodeEnum.SIGN_ERROR.generalException();
        }
        return Result.success(accountBalanceService.getAllUserAssetsVO(uid));
    }

    /**
     * 用户资产
     */
    @PostMapping("/assets/uids")
    public Result<UserAssetsVO> assetsUids(@RequestBody(required = false) UidsQuery query,
                                           @RequestHeader("sign") String sign,
                                           @RequestHeader("timestamp") String timestamp) {

        if (Objects.isNull(query)) {
            return Result.success();
        }
        if (!Crypto.hmacToString(DigestFactory.createSHA256(), SIGN_KEY, timestamp).equals(sign)) {
            throw ErrorCodeEnum.SIGN_ERROR.generalException();
        }
        return Result.success(accountBalanceService.getAllUserAssetsVO(query.getUids()));
    }

    /**
     * 用户资产
     */
    @PostMapping("/assets/map")
    public Result<List<UserAssetsVO>> assetsMap(@RequestBody(required = false) UidsQuery query,
                                                @RequestHeader("sign") String sign,
                                                @RequestHeader("timestamp") String timestamp) {
        if (Objects.isNull(query)) {
            return Result.success();
        }
        if (!Crypto.hmacToString(DigestFactory.createSHA256(), SIGN_KEY, timestamp).equals(sign)) {
            throw ErrorCodeEnum.SIGN_ERROR.generalException();
        }
        return Result.success(accountBalanceService.getUserAssetsVOMap(query.getUids()));
    }

    /**
     * nft返还gas
     */
    @PostMapping("/return/gas")
    public Result<Void> returnGas(@RequestBody @Valid OpenapiOperationQuery query,
                                  @RequestHeader("sign") String sign,
                                  @RequestHeader("timestamp") String timestamp) {

        if (!Crypto.hmacToString(DigestFactory.createSHA256(), SIGN_KEY, timestamp).equals(sign)) {
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

        if (!Crypto.hmacToString(DigestFactory.createSHA256(), SIGN_KEY, timestamp).equals(sign)) {
            throw ErrorCodeEnum.SIGN_ERROR.generalException();
        }
        openApiService.goldExchange(query);
        return new Result<>();
    }


    /**
     * 热钱包展板
     */
    @GetMapping("/wallet/board")
    public Result<WalletBoardDTO> goldExchange(FinancialBoardQuery query,
                                               WalletBoardType walletBoardType,
                                               @RequestHeader("sign") String sign,
                                               @RequestHeader("timestamp") String timestamp) {

        if (!Crypto.hmacToString(DigestFactory.createSHA256(), SIGN_KEY, timestamp).equals(sign)) {
            throw ErrorCodeEnum.SIGN_ERROR.generalException();
        }
        return Result.success(openApiService.walletBoardDTO(walletBoardType,query));
    }


}
