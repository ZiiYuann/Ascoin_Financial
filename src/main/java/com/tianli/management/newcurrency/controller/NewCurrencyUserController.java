package com.tianli.management.newcurrency.controller;

import com.tianli.exception.Result;
import com.tianli.management.newcurrency.entity.NewCurrencyUser;
import com.tianli.management.newcurrency.entity.NewCurrencyUserDTO;
import com.tianli.management.newcurrency.service.INewCurrencyUserService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


//新币用户表接口
@RestController
@RequestMapping("newCurrencyUser")
public class NewCurrencyUserController {

    @Autowired
    private INewCurrencyUserService iNewCurrencyUserService;

    //分页查询新币用户表
    @GetMapping(path = "/page", produces = {"application/json;charset=UTF-8"})
    public Result page(NewCurrencyUserDTO new_currency_user, Long page, Long size) {
        return iNewCurrencyUserService.page(new_currency_user,page,size);
    }
    //统计投入数额和人数
    @GetMapping(path = "/sumNewCurrency", produces = {"application/json;charset=UTF-8"})
    public Result sumNewCurrency() {
        return iNewCurrencyUserService.sumNewCurrency();
    }
}
