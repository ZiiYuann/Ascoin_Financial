package com.tianli.account.controller;

import cn.hutool.json.JSONUtil;
import com.google.common.base.MoreObjects;
import com.tianli.account.query.AccountDetailsQuery;
import com.tianli.account.query.IdsQuery;
import com.tianli.account.service.impl.AccountBalanceServiceImpl;
import com.tianli.address.AddressService;
import com.tianli.address.mapper.Address;
import com.tianli.address.vo.AddressVO;
import com.tianli.chain.entity.Coin;
import com.tianli.chain.service.CoinService;
import com.tianli.charge.entity.Order;
import com.tianli.charge.service.ChargeService;
import com.tianli.common.PageQuery;
import com.tianli.common.blockchain.NetworkType;
import com.tianli.common.webhook.WebHookService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.sso.init.RequestInitService;
import org.apache.commons.lang3.StringUtils;
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
    private AccountBalanceServiceImpl accountBalanceServiceImpl;
    @Resource
    private ChargeService chargeService;
    @Resource
    private WebHookService webHookService;
    @Resource
    private CoinService coinService;


    /**
     * 激活钱包
     */
    @PostMapping("/activate")
    public Result activateWallet() {
        Long uid = requestInitService.uid();
        return Result.success().setData(addressService.activityAccount(uid));
    }

    /**
     * 激活钱包
     */
    @PostMapping("/activate/uid")
    public Result activateWalletByUid(@RequestBody(required = false) String str) {
        if (StringUtils.isBlank(str)) {
            ErrorCodeEnum.ACCOUNT_ACTIVATE_UID_NULL.throwException();
        }
        Long uid = JSONUtil.parse(str).getByPath("uid", Long.class);
        if (Objects.isNull(uid)) {
            ErrorCodeEnum.ACCOUNT_ACTIVATE_UID_NULL.throwException();
        }
        return Result.success().setData(addressService.activityAccount(uid));
    }

    /**
     * 激活钱包
     */
    @PostMapping("/activate/uids")
    public Result activateWalletByUid(@RequestBody IdsQuery idsQuery) {
        try {
            addressService.activityAccount(idsQuery);
        } catch (Exception e) {
            webHookService.dingTalkSend("激活程序异常", e);
            throw e;
        }
        return Result.success();
    }

    /**
     * 钱包激活状态
     */
    @GetMapping("/status")
    public Result status() {
        Long uid = requestInitService.uid();
        Address address = addressService.get(uid);
        Map<String, Boolean> result = new HashMap<>();
        result.put("activate", Objects.nonNull(address));
        return Result.success().setData(result);
    }

    /**
     * 用户钱包地址
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
     * 主钱包地址
     */
    @GetMapping("/address/config")
    public Result addressConfig() {
        return Result.success(AddressVO.trans(addressService.getConfigAddress()));
    }

    /**
     * 手续费
     */
    @GetMapping("/service/amount")
    public Result serviceRate(String coin, NetworkType networkType) {
        if (StringUtils.isBlank(coin)) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }
        Coin coinEntity = coinService.getByNameAndNetwork(coin, networkType);
        BigDecimal withdrawFixedAmount = coinEntity.getWithdrawFixedAmount();
        HashMap<String, String> rateMap = new HashMap<>();
        rateMap.put("serviceAmount", withdrawFixedAmount.toPlainString());
        return Result.success().setData(rateMap);
    }

    /**
     * 最低提币
     */
    @GetMapping("/withdraw/limit")
    public Result withdrawLimit(String coin, NetworkType networkType) {
        if (StringUtils.isBlank(coin)) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }
        Coin coinEntity = coinService.getByNameAndNetwork(coin, networkType);
        var withdrawMin = coinEntity.getWithdrawMin();
        HashMap<String, String> rateMap = new HashMap<>();
        rateMap.put("withdrawLimitAmount", withdrawMin.toPlainString());
        return Result.success().setData(rateMap);
    }

    /**
     * 【云钱包】总资产 + 账户列表
     */
    @GetMapping("/balance/summary")
    public Result accountBalance() {
        Long uid = requestInitService.uid();
        return Result.instance().setData(accountBalanceServiceImpl.accountSummary(uid, true));
    }

    /**
     * 【云钱包】总资产 + 账户列表
     */
    @GetMapping("/balance/summary/dynamic")
    public Result accountBalanceDynamic() {
        Long uid = requestInitService.uid();
        return Result.instance().setData(accountBalanceServiceImpl.accountSummary(uid));
    }

    /**
     * 【云钱包】 账户列表
     */
    @GetMapping("/balances")
    public Result balances() {
        Long uid = requestInitService.uid();
        return Result.instance().setData(accountBalanceServiceImpl.accountList(uid));
    }

    /**
     * 【云钱包】币别详情账户余额
     */
    @GetMapping("/balance/{coin}")
    public Result accountBalance(@PathVariable String coin) {
        Long uid = requestInitService.uid();
        return Result.instance().setData(accountBalanceServiceImpl.accountSingleCoin(uid, coin));
    }

    /**
     * 【云钱包】币别详情下方详情列表
     */
    @GetMapping("/balance/details")
    public Result accountBalanceDetails(PageQuery<Order> pageQuery, AccountDetailsQuery query) {
        Long uid = requestInitService.uid();
        query = MoreObjects.firstNonNull(query, new AccountDetailsQuery());
        return Result.instance().setData(chargeService.pageByChargeGroup(uid, query, pageQuery.page()));
    }

    /**
     * 【云钱包】交易类型
     */
    @GetMapping("/transaction/type")
    public Result transactionType() {
        Long uid = requestInitService.uid();
        return Result.instance().setData(chargeService.listTransactionGroupType(uid));
    }

}
