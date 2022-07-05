package com.tianli.charge.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.gson.Gson;
import com.tianli.bet.BetDividendsService;
import com.tianli.captcha.email.service.CaptchaEmailService;
import com.tianli.captcha.phone.mapper.CaptchaPhoneType;
import com.tianli.charge.ChargeService;
import com.tianli.charge.ChargeType;
import com.tianli.charge.mapper.Charge;
import com.tianli.charge.mapper.ChargeStatus;
import com.tianli.common.init.RequestInitService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.kyc.KycService;
import com.tianli.kyc.mapper.Kyc;
import com.tianli.management.ruleconfig.ConfigConstants;
import com.tianli.mconfig.ConfigService;
import com.tianli.role.annotation.GrcCheckModular;
import com.tianli.role.annotation.GrcPrivilege;
import com.tianli.tool.MapTool;
import com.tianli.tool.crypto.Crypto;
import com.tianli.user.UserService;
import com.tianli.user.mapper.User;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.crypto.util.DigestFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author wangqiyun
 * @Date 2020/3/31 15:19
 */
@Slf4j
@RestController
@RequestMapping("/charge")
public class ChargeController {

    @GrcPrivilege(mode = GrcCheckModular.提现)
    @PostMapping("/withdraw")
    public Result withdraw(@RequestBody @Valid WithdrawDTO withdrawDTO) {
        if (withdrawDTO.getTokenCurrencyType().isFiat())
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        User user = userService.my();
//        captchaPhoneService.verify(user.getUsername(), CaptchaPhoneType.withdraw, withdrawDTO.getCode());
        captchaEmailService.verify(user.getUsername(), CaptchaPhoneType.withdraw, withdrawDTO.getCode());
        chargeService.withdraw(withdrawDTO);
        // TODO ws推送 CurrencyLogVO
        return Result.instance();
    }
    @GetMapping("/kyc/check")
    public Result kycCheck() {
        long uid = requestInitService.uid();
        Charge charge = chargeService.getOne(Wrappers.lambdaQuery(Charge.class)
                .eq(Charge::getCharge_type, ChargeType.withdraw)
                .eq(Charge::getUid, uid).last("LIMIT  1"));
        if (Objects.nonNull(charge)) return Result.success(MapTool.Map().put("hint", false));
        Kyc kyc = kycService.getOne(Wrappers.lambdaQuery(Kyc.class)
                .eq(Kyc::getUid, uid).last(" LIMIT 1"));
        return Result.success(MapTool.Map().put("hint", Objects.isNull(kyc)));
    }


    @PostMapping("/webhooks")
    public void webhooks(@RequestBody String str,
                         @RequestHeader("AppKey") String appKey,
                         @RequestHeader("Sign") String sign,
                         HttpServletResponse httpServletResponse) throws IOException {
        String wallet_app_key = configService.get("wallet_app_key");
        String wallet_app_secret = configService.get("wallet_app_secret");
        //验签
        System.out.println("提现回调 ==> "  + str);
        if (wallet_app_key.equals(appKey) && Crypto.hmacToString(DigestFactory.createSHA256(), wallet_app_secret, str).equals(sign)) {
            ChargeWebhooksDTO chargeWebhooksDTO = gson.fromJson(str, ChargeWebhooksDTO.class);
            chargeService.withdrawWebhooks(chargeWebhooksDTO);

            PrintWriter writer = httpServletResponse.getWriter();
            writer.write("success");
            writer.close();
        } else {
            PrintWriter writer = httpServletResponse.getWriter();
            writer.write("fail");
            writer.close();
        }
    }

    @GetMapping("/withdraw/apply/page")
    public Result withdrawApplyPage(@RequestParam(value = "status", defaultValue = "created") ChargeStatus status,
                                    @RequestParam(value = "page", defaultValue = "1") Integer page,
                                    @RequestParam(value = "size", defaultValue = "10") Integer size) {
        Long uid = requestInitService.uid();
        List<Charge> chargeList = chargeService.withdrawApplyPage(uid, status, page, size);
        return Result.instance().setData(chargeList.stream().map(WithdrawApplyChargeVO::trans).collect(Collectors.toList()));
    }

    @GetMapping("/recharge/daily/rate")
    public Result rechargeDailyRate() {
        String dailyRate = configService.get(ConfigConstants.USER_BALANCE_DAILY_RATE);
        double rate = BetDividendsService.rate(dailyRate);
        return Result.instance().setData(MapTool.Map().put("rate", rate));
    }

    @Resource
    private Gson gson;
    @Resource
    private ConfigService configService;
    @Resource
    private ChargeService chargeService;
    @Resource
    private RequestInitService requestInitService;
    @Resource
    private CaptchaEmailService captchaEmailService;
    @Resource
    private UserService userService;
    @Resource
    private KycService kycService;
}
