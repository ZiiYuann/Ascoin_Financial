package com.tianli.address.controller;

import com.google.common.collect.Lists;
import com.tianli.address.AddressService;
import com.tianli.address.mapper.Address;
import com.tianli.charge.ChargeService;
import com.tianli.sso.RequestInitService;
import com.tianli.currency.CurrencyTypeEnum;
import com.tianli.exception.Result;
import com.tianli.mconfig.ConfigService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
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
}

