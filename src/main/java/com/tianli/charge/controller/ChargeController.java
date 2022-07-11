package com.tianli.charge.controller;

import com.google.gson.Gson;
import com.tianli.address.query.RechargeCallbackQuery;
import com.tianli.charge.ChargeService;
import com.tianli.charge.entity.Charge;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.vo.WithdrawApplyChargeVO;
import com.tianli.charge.mapper.Charge;
import com.tianli.charge.mapper.ChargeStatus;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.mconfig.ConfigService;
import com.tianli.sso.RequestInitService;
import com.tianli.tool.crypto.Crypto;
import com.tianli.sso.init.RequestInit;
import com.tianli.sso.init.RequestInitService;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.util.DigestFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author  wangqiyun
 * @since  2020/3/31 15:19
 */
@Slf4j
@RestController
@RequestMapping("/charge")
public class ChargeController {

    @Resource
    private Gson gson;
    @Resource
    private ConfigService configService;
    @Resource
    private ChargeService chargeService;
    @Resource
    private RequestInitService requestInitService;

    @PostMapping("/withdraw")
    public Result withdraw(@RequestBody @Valid WithdrawDTO withdrawDTO) {
        if (withdrawDTO.getCurrencyAdaptType().isFiat())
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        chargeService.withdraw(withdrawDTO);
        return Result.instance();
    }

    @GetMapping("/withdraw/apply/page")
    public Result withdrawApplyPage(@RequestParam(value = "status", defaultValue = "created") ChargeStatus status,
                                    @RequestParam(value = "page", defaultValue = "1") Integer page,
                                    @RequestParam(value = "size", defaultValue = "10") Integer size) {
        Long uid = requestInitService.uid();
        List<Charge> chargeList = chargeService.withdrawApplyPage(uid, status, page, size);
        return Result.instance().setData(chargeList.stream().map(WithdrawApplyChargeVO::trans).collect(Collectors.toList()));
    }

    /**
     * 充值回调
     */
    @PostMapping("/webhooks/recharge")
    public Result rechargeCallback(@RequestBody String str, @RequestHeader("AppKey") String appKey
            , @RequestHeader("Sign") String sign) {
        String walletAppKey = configService.get("wallet_app_key");
        String walletAppSecret = configService.get("wallet_app_secret");

        log.info("充值回调参数 ==> {}", gson.toJson(str));

        if (walletAppKey.equals(appKey) && Crypto.hmacToString(DigestFactory.createSHA256(), walletAppSecret, str).equals(sign)) {
            ErrorCodeEnum.SIGN_ERROR.throwException();
        }

        RechargeCallbackQuery query = gson.fromJson(str, RechargeCallbackQuery.class);
        chargeService.rechargeCallback(query);
        return Result.success();
    }

    /**
     * 提现回调
     */
    @PostMapping("/webhooks/withdraw")
    public Result webhooks(@RequestBody String str, @RequestHeader("AppKey") String appKey,
                           @RequestHeader("Sign") String sign) {
        String walletAppKey = configService.get("wallet_app_key");
        String walletAppSecret = configService.get("wallet_app_secret");
        //验签
        log.info("提现回调参数 ==> {}", gson.toJson(str));
        if (walletAppKey.equals(appKey) && Crypto.hmacToString(DigestFactory.createSHA256(), walletAppSecret, str).equals(sign)) {
            ErrorCodeEnum.SIGN_ERROR.throwException();
        }
        ChargeWebhooksDTO chargeWebhooksDTO = gson.fromJson(str, ChargeWebhooksDTO.class);
        chargeService.withdrawWebhooks(chargeWebhooksDTO);
        return Result.success();
    }


}
