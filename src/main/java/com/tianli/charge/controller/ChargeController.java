package com.tianli.charge.controller;

import com.google.gson.Gson;
import com.tianli.address.query.RechargeCallbackQuery;
import com.tianli.charge.service.ChargeService;
import com.tianli.charge.entity.Order;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.query.WithdrawQuery;
import com.tianli.charge.vo.WithdrawApplyChargeVO;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.mconfig.ConfigService;
import com.tianli.sso.init.RequestInitService;
import com.tianli.tool.crypto.Crypto;
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

    /**
     * 充值回调
     */
    @PostMapping("/recharge")
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
     * 提现申请
     */
    @PostMapping("/withdraw/apply")
    public Result withdraw(@RequestBody @Valid WithdrawQuery withdrawDTO) {
        if (withdrawDTO.getCurrencyAdaptType().isFiat())
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        chargeService.withdraw(withdrawDTO);
        return Result.instance();
    }



}
