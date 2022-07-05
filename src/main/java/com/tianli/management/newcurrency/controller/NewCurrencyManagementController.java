package com.tianli.management.newcurrency.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tianli.common.CommonFunction;
import com.tianli.currency_token.token.mapper.TokenList;
import com.tianli.currency_token.transfer.mapper.TokenContract;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.management.newcurrency.entity.*;
import com.tianli.management.newcurrency.service.INewCurrencyManagementService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("newCurrencyManagement")
public class NewCurrencyManagementController {

    @Autowired
    private INewCurrencyManagementService iNewCurrencyManagementService;

    //分页查询新币管理端")
    @GetMapping(path = "/page", produces = {"application/json;charset=UTF-8"})
    public Result page(NewCurrencyManagementDTO newCurrencyManagement, Long page, Long size) {
        return iNewCurrencyManagementService.page(newCurrencyManagement, page, size);
    }

    //新增新币管理端")
    @PostMapping(path = "/list", produces = {"application/json;charset=UTF-8"})
    public Result insert(@RequestBody @Valid NewCurrencyManagement list) {
        List<NewCurrencyManagement> one = iNewCurrencyManagementService.list(
                new LambdaQueryWrapper<NewCurrencyManagement>()
                        .eq(NewCurrencyManagement::getCurrency_name, list.getCurrency_name())
                        .eq(NewCurrencyManagement::getIs_delete, "0")
        );
        if(one.size()!=0){
            ErrorCodeEnum.NEW_CURRENCY_NAME_EXIT.throwException();
        }
        List<NewCurrencyManagement> two = iNewCurrencyManagementService.list(
                new LambdaQueryWrapper<NewCurrencyManagement>()
                        .eq(NewCurrencyManagement::getCurrency_name_short, list.getCurrency_name_short())
                        .eq(NewCurrencyManagement::getIs_delete, "0")
        );
        if(two.size()!=0){
            ErrorCodeEnum.NEW_CURRENCY_SHORT_NAME_EXIT.throwException();
        }
        return iNewCurrencyManagementService.insert(list);
    }

    //更新新币管理端")
    @PostMapping(path = "/update", produces = {"application/json;charset=UTF-8"})
    public Result updateByEntity(@RequestBody @Valid NewCurrencyManagement list) {
        return iNewCurrencyManagementService.updateByEntity(list);
    }

    //逻辑删除新币管理端
    @DeleteMapping(path = "/list", produces = {"application/json;charset=UTF-8"})
    public Result deleteById(@RequestParam("id") Long id) {
        return iNewCurrencyManagementService.deleteById(id);
    }

    //由id获得新币管理端
    @GetMapping(path = "/getListById", produces = {"application/json;charset=UTF-8"})
    public Result getListById(@RequestParam("id") Long id) {
        return iNewCurrencyManagementService.getListById(id);
    }


}
