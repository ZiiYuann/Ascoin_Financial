package com.tianli.management.platformfinance;

import com.tianli.bet.mapper.BetResultEnum;
import com.tianli.exception.Result;
import com.tianli.role.annotation.AdminPrivilege;
import com.tianli.role.annotation.Privilege;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("platform/finance")
public class PlatformFinanceController {


    @Resource
    PlatformFinanceService platformFinanceService;

    @Resource
    private RedisTemplate<String, Map<String, Object>> redisTemplate;

    @GetMapping("dividends/details")
    @AdminPrivilege(and = Privilege.平台分红明细)
    public Result dividendsDetails(String phone, BetResultEnum result, String startTime, String endTime,
                                  @RequestParam(value = "page", defaultValue = "1") Integer page,
                                  @RequestParam(value = "size", defaultValue = "10") Integer size){
        return Result.success(platformFinanceService.dividendsDetails(phone, result, startTime, endTime, page, size));

    }


    @GetMapping("fee/exhibition")
//    @AdminPrivilege(and = Privilege.手续费财务展板)
    public Result feeExhibition(@RequestParam(value = "page",defaultValue = "1") Integer page,
                                @RequestParam(value = "size",defaultValue = "10") Integer size){

        BoundValueOperations<String, Map<String, Object>> feeExhibition_ops = redisTemplate.boundValueOps("feeExhibition_list");
        Map<String, Object> map = feeExhibition_ops.get();
        if (!CollectionUtils.isEmpty(map)) return Result.success(map);
        Map<String, Object> exhibitionMap = platformFinanceService.feeExhibition1(page, size);
        feeExhibition_ops.set(exhibitionMap, 5L, TimeUnit.MINUTES);
        return Result.success(exhibitionMap);

    }

    @GetMapping("exhibition")
//    @AdminPrivilege(and = Privilege.平台财务展板)
    public Result financeExhibition(String startTime, String endTime,
                                    @RequestParam(value = "page", defaultValue = "1") Integer page,
                                    @RequestParam(value = "size", defaultValue = "10") Integer size){
        BoundValueOperations<String, Map<String, Object>> financeExhibition_ops = redisTemplate.boundValueOps("financeExhibition_list");
        Map<String, Object> objectMap = financeExhibition_ops.get();
        if (!CollectionUtils.isEmpty(objectMap)) return Result.success(objectMap);
        Map<String, Object> financeExhibitionMap = platformFinanceService.financeExhibition3(startTime, endTime, page, size);
        financeExhibition_ops.set(financeExhibitionMap, 5L, TimeUnit.MINUTES);
        return Result.success(financeExhibitionMap);
    }
}
