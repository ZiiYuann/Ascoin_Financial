package com.tianli.account.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.account.entity.AccountBalance;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.address.AddressService;
import com.tianli.address.mapper.Address;
import com.tianli.address.vo.AddressVO;
import com.tianli.charge.ChargeService;
import com.tianli.charge.ChargeType;
import com.tianli.charge.entity.Charge;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.sso.init.RequestInitService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
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

    /**
     * 激活钱包
     */
    @GetMapping("/activate")
    public Result activateWallet() {
        Long uid = requestInitService.uid();
        addressService.getAndInit(uid);
        return Result.success();
    }

    /**
     * 查看钱包地址
     */
    @GetMapping("/address")
    public Result walletStatus() {
        Long uid = requestInitService.uid();
        Address address = addressService.get(uid);
        if (Objects.isNull(address)) {
            ErrorCodeEnum.ACCOUNT_NOT_ACTIVE.throwException();
        }
        return Result.success(AddressVO.trans(address));
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
     * 查询单个币种余额
     */
    @GetMapping("/balance")
    public Result accountBalance(CurrencyCoin currencyCoin) {
        Long uid = requestInitService.uid();
        return Result.instance().setData(accountBalanceService.getVO(uid,currencyCoin));
    }

    /**
     * 获取余额详情信息
     */
    @GetMapping("/balance/details")
    public Result accountBalanceDetails(int page, int size, ChargeType chargeType, CurrencyCoin currencyCoin) {
        Long uid = requestInitService.uid();
        Page<Charge> pageQuery = new Page<>(page, size);
        return Result.instance().setData(chargeService.pageByChargeType(uid,currencyCoin, chargeType, pageQuery));
    }

}
