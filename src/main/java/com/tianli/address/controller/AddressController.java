package com.tianli.address.controller;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.tianli.address.AddressService;
import com.tianli.address.mapper.Address;
import com.tianli.charge.ChargeService;
import com.tianli.common.init.RequestInitService;
import com.tianli.currency.CurrencyTypeEnum;
import com.tianli.exception.Result;
import com.tianli.mconfig.ConfigService;
import com.tianli.tool.crypto.Crypto;
import org.bouncycastle.crypto.util.DigestFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户充值地址表 前端控制器
 * </p>
 *
 * @author hd
 * @since 2020-12-14
 */
@RestController
@RequestMapping("/address")
public class AddressController {

    @Resource
    private AddressService addressService;

    @Resource
    private RequestInitService requestInitService;

    @Resource
    private ConfigService configService;

    @Resource
    private ChargeService chargeService;

    @GetMapping("")
    public Result myAddress() throws IOException {
        Long uid = requestInitService.uid();
        Address address = addressService.get_(uid, CurrencyTypeEnum.normal);
        return Result.instance().setData(Lists.newArrayList(address).stream().map(AddressVO::trans).collect(Collectors.toList()));
    }

    @PostMapping("/webhooks")
    public void webhooks(@RequestBody String str,
                         @RequestHeader("AppKey") String appKey,
                         @RequestHeader("Sign") String sign,
                         HttpServletResponse httpServletResponse) throws IOException {
        String wallet_app_key = configService.get("wallet_app_key");
        String wallet_app_secret = configService.get("wallet_app_secret");
        //验签
        System.out.println("充值回调webhooks参数 ==> " + str);
        if (wallet_app_key.equals(appKey) && Crypto.hmacToString(DigestFactory.createSHA256(), wallet_app_secret, str).equals(sign)) {
            AddressWebhooksDTO addressWebhooksDTO = new Gson().fromJson(str, AddressWebhooksDTO.class);

            chargeService.receive(addressWebhooksDTO);

            PrintWriter writer = httpServletResponse.getWriter();
            writer.write("success");
            writer.close();
        } else {
            PrintWriter writer = httpServletResponse.getWriter();
            writer.write("fail");
            writer.close();
        }
    }

}

