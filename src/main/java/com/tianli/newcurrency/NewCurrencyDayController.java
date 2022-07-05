package com.tianli.newcurrency;

import com.tianli.common.CommonFunction;
import com.tianli.common.async.AsyncService;
import com.tianli.common.init.RequestInitService;
import com.tianli.common.lock.RedisLock;
import com.tianli.exception.Result;
import com.tianli.management.newcurrency.entity.NewCurrencyManagement;
import com.tianli.management.newcurrency.entity.NewCurrencyUser;
import com.tianli.management.newcurrency.service.INewCurrencyManagementService;
import com.tianli.tool.ApplicationContextTool;
import com.tianli.user.UserService;
import com.tianli.user.mapper.User;
import com.tianli.user.userinfo.UserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.mountcloud.graphql.GraphqlClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;


@RestController
@RequestMapping("/newCurrencyDay")
public class NewCurrencyDayController {

    @Autowired
    private INewCurrencyDayService iNewCurrencyDayService;
    @Autowired
    private INewCurrencyManagementService iNewCurrencyManagementService;
    @Resource
    RequestInitService requestInitService;
    @Autowired
    private UserService userService;



//    @ApiOperation("手动分发")
    @GetMapping(path = "/computed", produces = {"application/json;charset=UTF-8"})
    public Result computed() {
        iNewCurrencyDayService.syncComputedCurrency();
        return null;
    }


//    @ApiOperation("查询币种")
    @GetMapping(path = "/getNewCurrency", produces = {"application/json;charset=UTF-8"})
    public Result getNewCurrency() {
        return iNewCurrencyDayService.getNewCurrency();
    }

//    @ApiOperation("查询币种配置数据")
    @GetMapping(path = "/selectNewCurrecy", produces = {"application/json;charset=UTF-8"})
    public Result selectNewCurrecy(Long currencyId,Long uid) {
        try{
            uid = requestInitService.uid();
        }catch (Exception e){
            uid=null;
        }
        return iNewCurrencyDayService.selectNewCurrecy(currencyId,uid);
    }
//    @ApiOperation("laungchpad列表")
    @GetMapping(path = "/laungchpad", produces = {"application/json;charset=UTF-8"})
    public Result laungchpad(Long page,Long size) {
        return iNewCurrencyDayService.laungchpad(page,size);
    }

//    @ApiOperation("成交记录列表")
    @GetMapping(path = "/tradingRecord", produces = {"application/json;charset=UTF-8"})
    public Result tradingRecord(Long uid,Long page, Long size) {
        uid = requestInitService.uid();
        return iNewCurrencyDayService.tradingRecord(uid,page,size);
    }

//    @ApiOperation("确定投入")
    @PostMapping(path = "/inputConfirm", produces = {"application/json;charset=UTF-8"})
    public Result inputConfirm(@RequestBody NewCurrencyUser newCurrencyUser) {
        Long uid = requestInitService.uid();
        User user = userService._get(uid);
        newCurrencyUser.setUser_type(user.getUser_type());//用户类型
        newCurrencyUser.setEmail(user.getUsername());//email
        newCurrencyUser.setUid(uid);//uid
        return iNewCurrencyDayService.inputConfirm(newCurrencyUser);
    }

    //测试接口
    @PostMapping(path = "/test", produces = {"application/json;charset=UTF-8"})
    public Result inputConfirmTest(@RequestBody NewCurrencyDay day) {
        try{
            day.setId(CommonFunction.generalId());
            day.setToken("usdt");
            day.setType("actual");
            iNewCurrencyDayService.save(day);
        }catch (Exception e){
            return Result.fail("失败");
        }
        return Result.success();
    }

}
