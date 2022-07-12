package com.tianli.charge.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.tianli.charge.ChargeService;
import com.tianli.charge.mapper.Charge;
import com.tianli.charge.mapper.ChargeStatus;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.mconfig.ConfigService;
import com.tianli.sso.init.RequestInit;
import com.tianli.sso.init.RequestInitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author wangqiyun
 * @Date 2020/3/31 15:19
 */
@Slf4j
@RestController
@RequestMapping("/charge")
public class ChargeController {

    @PostMapping("/withdraw")
    public Result withdraw(@RequestBody @Valid WithdrawDTO withdrawDTO) {
        if (withdrawDTO.getTokenCurrencyType().isFiat())
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        RequestInit requestInit = requestInitService.get();
        JsonObject userInfo = requestInit.getUserInfo();
//        captchaPhoneService.verify(user.getUsername(), CaptchaPhoneType.withdraw, withdrawDTO.getCode());
//        captchaEmailService.verify(userInfo.get("username"), CaptchaPhoneType.withdraw, withdrawDTO.getCode());
        chargeService.withdraw(withdrawDTO);
        // TODO ws推送 CurrencyLogVO
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


    @Resource
    private Gson gson;
    @Resource
    private ConfigService configService;
    @Resource
    private ChargeService chargeService;
    @Resource
    private RequestInitService requestInitService;
}
