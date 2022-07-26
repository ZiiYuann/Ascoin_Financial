package com.tianli.account.controller;

import com.tianli.account.service.AccountBalanceService;
import com.tianli.address.AddressService;
import com.tianli.address.mapper.Address;
import com.tianli.address.vo.AddressVO;
import com.tianli.charge.entity.Order;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.service.ChargeService;
import com.tianli.common.PageQuery;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.common.blockchain.NetworkType;
import com.tianli.currency.enums.CurrencyAdaptType;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.mconfig.ConfigService;
import com.tianli.sso.init.RequestInitService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author chenb
 * @apiNote 用户账户控制器
 * @since 2022-07-06
 **/
@RestController
@RequestMapping("/account")
public class AccountController {

    @Resource
    private AddressService addressService;
    @Resource
    private RequestInitService requestInitService;
    @Resource
    private AccountBalanceService accountBalanceService;
    @Resource
    private ChargeService chargeService;
    @Resource
    private ConfigService configService;

    /**
     * 激活钱包
     */
    @PostMapping("/activate")
    public Result activateWallet() {
        Long uid = requestInitService.uid();
        return Result.success().setData(addressService.activityAccount(uid));
    }

    /**
     * 查看钱包地址
     */
    @GetMapping("/status")
    public Result status() {
        Long uid = requestInitService.uid();
        Address address = addressService.get(uid);
        Map<String,Boolean> result = new HashMap<>();
        result.put("activate",Objects.nonNull(address));
        return Result.success().setData(result);
    }

    /**
     * 查看钱包地址
     */
    @GetMapping("/address")
    public Result address() {
        Long uid = requestInitService.uid();
        Address address = addressService.get(uid);
        if (Objects.isNull(address)) {
            ErrorCodeEnum.ACCOUNT_NOT_ACTIVE.throwException();
        }
        return Result.success(AddressVO.trans(address));
    }

    /**
     * 查看系统钱包地址
     */
    @GetMapping("/address/config")
    public Result addressConfig() {
        return Result.success(AddressVO.trans(addressService.getConfigAddress()));
    }

    /**
     * 手续费
     */
    @GetMapping("/service/rate")
    public Result serviceRate(CurrencyCoin coin, NetworkType networkType) {
        CurrencyAdaptType currencyAdaptType = CurrencyAdaptType.get(coin, networkType);
        String rate = configService.get(currencyAdaptType.name() + "_withdraw_rate");
        HashMap<String, BigDecimal> rateMap = new HashMap<>();
        rateMap.put("serviceRate",BigDecimal.valueOf(Double.parseDouble(rate)));
        return Result.success().setData(rateMap);
    }

    /**
     * 查询用户云钱包余额汇总
     */
    @GetMapping("/balance/summary")
    public Result accountBalance() {
        Long uid = requestInitService.uid();
        return Result.instance().setData(accountBalanceService.getAccountBalanceMainPageVO(uid));
    }

    /**
     * 查询用户云钱包列表信息
     */
    @GetMapping("/balances")
    public Result balances() {
        Long uid = requestInitService.uid();
        return Result.instance().setData(accountBalanceService.getAccountBalanceList(uid));
    }

    /**
     * 查询单个币种余额
     */
    @GetMapping("/balance/{coin}")
    public Result accountBalance(@PathVariable CurrencyCoin coin) {
        Long uid = requestInitService.uid();
        return Result.instance().setData(accountBalanceService.getVO(uid,coin));
    }

    /**
     * 获取余额详情信息
     */
    @GetMapping("/balance/details")
    public Result accountBalanceDetails(PageQuery<Order> query, ChargeType chargeType, CurrencyCoin coin) {
        Long uid = requestInitService.uid();
        return Result.instance().setData(chargeService.pageByChargeType(uid,coin, chargeType, query.page()));
    }

}