package com.tianli.account.controller;

import com.tianli.exception.Result;
import com.tianli.account.query.AccountActiveQuery;
import com.tianli.wallet.service.AccountActiveService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author chenb
 * @apiNote 钱包Controller
 * @since 2022-07-06
 **/
@RestController
@RequestMapping("/account")
public class AccountController {

    @Resource
    private AccountActiveService accountActiveService;

    /**
     * 激活钱包
     */
    @GetMapping("/activate")
    public Result activateWallet(AccountActiveQuery query){
        accountActiveService.activateWallet(query);
        return Result.success();
    }

    /**
     * 查看激活状态
     */
    @GetMapping("/status")
    public Result walletStatus(){
        return Result.success(accountActiveService.getWalletActiveVo());
    }
}
