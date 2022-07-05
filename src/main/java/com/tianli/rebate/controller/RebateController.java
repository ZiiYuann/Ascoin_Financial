package com.tianli.rebate.controller;


import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.common.init.RequestInitService;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.rebate.RebateService;
import com.tianli.rebate.mapper.Rebate;
import com.tianli.tool.MapTool;
import com.tianli.tool.time.TimeTool;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * <p>
 * 返佣表 前端控制器
 * </p>
 *
 * @author hd
 * @since 2020-12-09
 */
@RestController
@RequestMapping("/rebate")
public class RebateController {

    @Resource
    private RebateService rebateService;

    @Resource
    private RequestInitService requestInitService;

    @GetMapping("/page")
    public Result page(@RequestParam(value = "page",defaultValue = "1") Integer page, @RequestParam(value = "size",defaultValue = "10") Integer size){
        Long uid = requestInitService.uid();
        CompletableFuture<Map<String, Object>> asyncCalculation1 = CompletableFuture.supplyAsync(() -> {
            Map<String, LocalDateTime> thisDay = TimeTool.thisDay();
//            BigInteger day = rebateService.totalRebateAmountWithInterval(uid, thisDay.get("start"), thisDay.get("end"));
            // 用户统计同样可以能获取到总返佣数额
            BigInteger total = rebateService.totalRebateAmount(uid);
            BigInteger totalBF = rebateService.totalRebateBFAmount(uid);
//            Pair<DigitalCurrency, Double> cny = new DigitalCurrency(TokenCurrencyType.usdt_omni, total).toOtherAndPrice(TokenCurrencyType.cny);
            return MapTool.Map()
                    .put("totalBF", TokenCurrencyType.BF_bep20.money(totalBF))
                    .put("totalUsdt", TokenCurrencyType.usdt_omni.money(total));
        });
//        CompletableFuture<Map<String, Object>> asyncCalculation2 = CompletableFuture.supplyAsync(() -> {
//            Map<String, LocalDateTime> thisWeek = TimeTool.thisWeekMondayToSunday();
//            Map<String, LocalDateTime> thisMonth = TimeTool.thisMonth();
//            BigInteger week = rebateService.totalRebateAmountWithInterval(uid, thisWeek.get("start"), thisWeek.get("end"));
//            BigInteger month = rebateService.totalRebateAmountWithInterval(uid, thisMonth.get("start"), thisMonth.get("end"));
//            return MapTool.Map().put("week", TokenCurrencyType.usdt_omni.money(week)).put("month", TokenCurrencyType.usdt_omni.money(month));
//        });
        Page<Rebate> rebatePage = rebateService.page(new Page<>(page, size), new LambdaUpdateWrapper<Rebate>().eq(Rebate::getRebate_uid, uid).orderByDesc(Rebate::getId));
        List<RebatePageVO> pageVOS = rebatePage.getRecords().stream().map(RebatePageVO::trans).collect(Collectors.toList());
        Map<String, Object> task = null;
        try {
            task = asyncCalculation1.get();
//            Map<String, Object> task2 = asyncCalculation2.get();
//            task.putAll(task2);
        } catch (Exception e) {
            ErrorCodeEnum.SYSTEM_ERROR.throwException();
        }
        return Result.instance().setData(MapTool.Map().put("list", pageVOS).put("statistics", task));
    }
}

