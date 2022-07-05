package com.tianli.rake.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.bet.BetService;
import com.tianli.bet.mapper.Bet;
import com.tianli.common.init.RequestInitService;
import com.tianli.currency.DigitalCurrency;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.currency.log.CurrencyLog;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.currency.log.CurrencyLogService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.tool.MapTool;
import com.tianli.tool.time.TimeTool;
import org.javatuples.Pair;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 *
 * 抽水表
 *
 * @author linyifan
 * @date 2/19/21 2:18 PM
 */

@RestController
@RequestMapping("/rake")
public class RakeController {

    @GetMapping("/page")
    public Result page(@RequestParam(value = "page",defaultValue = "1") Integer page, @RequestParam(value = "size",defaultValue = "10") Integer size){
        Long uid = requestInitService.uid();
        /**判断是否登录？*/
        //统计总、每日、周、月的抽水
        CompletableFuture<Map<String, Object>> asyncCalculation1 = CompletableFuture.supplyAsync(() -> {
            BigInteger total = currencyLogService.totalRebateAmount(uid);

            Map<String, LocalDateTime> thisDay = TimeTool.thisDay();
            BigInteger day = currencyLogService.totalRebateAmountWithInterval(uid, thisDay.get("start"), thisDay.get("end"));

            Pair<DigitalCurrency, Double> cny = new DigitalCurrency(TokenCurrencyType.usdt_omni, total).toOtherAndPrice(TokenCurrencyType.cny);
            return MapTool.Map()
                    .put("total", TokenCurrencyType.usdt_omni.money(total))
                    .put("cny", cny.getValue0().getMoney())
                    .put("day", TokenCurrencyType.usdt_omni.money(day));
        });
        CompletableFuture<Map<String, Object>> asyncCalculation2 = CompletableFuture.supplyAsync(() -> {
            Map<String, LocalDateTime> thisWeek = TimeTool.thisWeekMondayToSunday();
            BigInteger week = currencyLogService.totalRebateAmountWithInterval(uid, thisWeek.get("start"), thisWeek.get("end"));
            Map<String, LocalDateTime> thisMonth = TimeTool.thisMonth();
            BigInteger month = currencyLogService.totalRebateAmountWithInterval(uid, thisMonth.get("start"), thisMonth.get("end"));
            return MapTool.Map().put("week", TokenCurrencyType.usdt_omni.money(week)).put("month", TokenCurrencyType.usdt_omni.money(month));
        });

        //获取抽水明细
        List<RakePageVO> rakePageVOS = new ArrayList<>();
        Page<CurrencyLog> currencyLogPage = currencyLogService.page(new Page<>(page, size), new LambdaQueryWrapper<CurrencyLog>()
                .orderByDesc(CurrencyLog::getCreate_time)
                .eq(CurrencyLog::getDes, CurrencyLogDes.抽水.name())
                .eq(CurrencyLog::getUid, uid));

        if (Objects.isNull(currencyLogPage.getRecords())) ErrorCodeEnum.INSUFFICIENT_USER_RIGHTS.throwException();

        currencyLogPage.getRecords().stream().forEach(currencyLog -> {
            BigInteger rakeAmount = currencyLog.getAmount();
            LocalDateTime create_time = currencyLog.getCreate_time();
            //currencyLog中sn = "rake_"+bet.id
            Long betId = Long.parseLong(currencyLog.getSn().split("_")[1]);
            Bet bet = betService.getById(betId);
            RakePageVO rakePageVO = RakePageVO.builder()
                    .create_time(create_time)
                    .amount(TokenCurrencyType.usdt_omni.money(bet.getAmount()))
                    .uid_username(bet.getUid_username())
                    .uid_nick(bet.getUid_nick())
                    .uid_avatar(bet.getUid_avatar())
                    .rake_amount(TokenCurrencyType.usdt_omni.money(rakeAmount)).build();
            rakePageVOS.add(rakePageVO);
        });

        Map<String, Object> task = null;
        try {
            task = asyncCalculation1.get();
            Map<String, Object> task2 = asyncCalculation2.get();
            task.putAll(task2);
        } catch (Exception e) {
            ErrorCodeEnum.SYSTEM_ERROR.throwException();
        }
        return Result.instance().setData(MapTool.Map().put("list", rakePageVOS).put("statistics", task));
    }


    @Resource
    private RequestInitService requestInitService;

    @Resource
    private BetService betService;

    @Resource
    private CurrencyLogService currencyLogService;

}
