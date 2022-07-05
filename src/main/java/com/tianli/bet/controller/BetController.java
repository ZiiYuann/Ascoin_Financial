package com.tianli.bet.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tianli.bet.BetService;
import com.tianli.bet.BetUserLineService;
import com.tianli.bet.mapper.*;
import com.tianli.common.Constants;
import com.tianli.common.init.RequestInitService;
import com.tianli.common.init.TspContent;
import com.tianli.currency.CurrencyService;
import com.tianli.currency.CurrencyTypeEnum;
import com.tianli.currency.DiscountCurrencyService;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.currency.mapper.Currency;
import com.tianli.currency.mapper.DiscountCurrency;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.kline.FollowCurrencyService;
import com.tianli.kline.mapper.FollowCurrency;
import com.tianli.management.ruleconfig.BetDurationVO;
import com.tianli.management.ruleconfig.ConfigConstants;
import com.tianli.management.ruleconfig.mapper.BetDuration;
import com.tianli.mconfig.ConfigService;
import com.tianli.role.annotation.GrcCheckModular;
import com.tianli.role.annotation.GrcPrivilege;
import com.tianli.tool.MapTool;
import com.tianli.user.logs.UserIpLogService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * 押注表 前端控制器
 * </p>
 *
 * @author hd
 * @since 2020-12-04
 */
@RestController
@RequestMapping("/bet")
public class BetController {

    @PostMapping("/submit")
    @GrcPrivilege(mode = GrcCheckModular.下注)
    public Result submit(@RequestBody @Valid BetDTO bet) {
        if (Objects.equals(BetTypeEnum.steady, bet.getBetType())) {
            String betSteadySection = configService.getOrDefaultNoCache(ConfigConstants.BET_STEADY_SECTION, "[0,0,0,0]");
            List<Integer> sectionList = gson.fromJson(betSteadySection, new TypeToken<List<Integer>>() {
            }.getType());
            // 增加稳赚场时间限制!
            int hour = LocalDateTime.now().getHour();
            if (hour < sectionList.get(0) || (hour >= sectionList.get(1) && hour < sectionList.get(2)) || hour >= sectionList.get(3)) {
                ErrorCodeEnum.NOT_BET_STEADY_SECTION_ERROR.throwException();
            }
        }
        if (StringUtils.isBlank(bet.getBetSymbol())) {
            bet.setBetSymbol("ethusdt");
        }
        betService.submit(bet);
        Object prophecy_end_exchange_rate = TspContent.get("prophecy_end_exchange_rate");
        Object time = TspContent.get("triggerTime");
        userIpLogService.updateBehaviorId(GrcCheckModular.下注, (Long) TspContent.get("betId"));
        TspContent.remove();
        return Result.success(MapTool.Map()
                .put("setting", Objects.nonNull(prophecy_end_exchange_rate))
                .put("price", prophecy_end_exchange_rate)
                .put("triggerTime", time)
        );
    }

    @GetMapping("/duration/config")
    public Result duration() {
        List<BetDuration> list = betService.durationConfig();
        return Result.instance().setData(MapTool.Map().put("list", list.stream().map(BetDurationVO::trans).collect(Collectors.toList())));
    }

    @GetMapping("/page")
    public Result page(BetPageDTO bet) throws ExecutionException, InterruptedException {
        // 获取登录人信息
        Long uid = requestInitService.uid();
//        betService.settleBetByUid(uid);
        BetResultEnum result = bet.getResult();
        CompletableFuture<Map<String, Object>> summaryCalculation = CompletableFuture.supplyAsync(() -> {
            Map<String, Object> map = MapTool.Map();
            BigInteger totalAmount = betService.sumAmount(uid, bet.getBetType(), bet.getStartTime(), bet.getEndTime(), result);
            map.put("total", TokenCurrencyType.usdt_omni.money(totalAmount));
//            if(Objects.equals(BetResultEnum.win, result)){
//
////                BigInteger sumWinProfit = betService.sumProfit(uid, bet.getBetType(), bet.getStartTime(), bet.getEndTime(), BetResultEnum.win);
//                double money = TokenCurrencyType.usdt_omni.money(sumWinProfit);
//                map.put("sumWinProfit", money);
//                map.put("sumLoseProfit", 0);
//                map.put("totalProfit", money);
//                return map;
//            }else if(Objects.equals(BetResultEnum.lose, result)){
////                BigInteger sumLoseProfit = betService.sumProfit(uid, bet.getBetType(), bet.getStartTime(), bet.getEndTime(), BetResultEnum.lose);
//                double money = TokenCurrencyType.usdt_omni.money(sumLoseProfit);
//                map.put("sumWinProfit", 0);
//                map.put("sumLoseProfit", money);
//                map.put("totalProfit", money);
//                return map;
//            }else if(Objects.equals(BetResultEnum.wait, result)){
//                map.put("sumWinProfit", 0);
//                map.put("sumLoseProfit", 0);
//                map.put("totalProfit", 0);
//                return map;
//            }
//            BigInteger sumWinProfit = betService.sumProfit(uid, bet.getBetType(), bet.getStartTime(), bet.getEndTime(), BetResultEnum.win);
//            BigInteger sumLoseProfit = betService.sumProfit(uid, bet.getBetType(), bet.getStartTime(), bet.getEndTime(), BetResultEnum.lose);
            Map<String, BigDecimal> winMap = betService.sumProfit(uid, bet.getBetType(), bet.getStartTime(), bet.getEndTime(), bet.getResult());
            BigDecimal totalEarnWin = winMap.get("totalEarnWin");
            BigDecimal totalFee = winMap.get("totalFee");
            BigDecimal totalFeeBF = winMap.get("totalFeeBF");
            BigDecimal totalEarnLose = winMap.get("totalEarnLose");
            BigDecimal totalIncomeBF = winMap.get("totalIncomeBF");
            map.put("totalProfitWin", TokenCurrencyType.usdt_omni.money(totalEarnWin.subtract(totalFee).toBigInteger()));
            map.put("totalFeeBF", TokenCurrencyType.BF_bep20.money(totalFeeBF.toBigInteger()));
            map.put("totalIncomeBF", TokenCurrencyType.BF_bep20.money(totalIncomeBF.toBigInteger()));
            map.put("totalProfitLose", TokenCurrencyType.usdt_omni.money(totalEarnLose.toBigInteger()));
            return map;
        });
        LambdaQueryWrapper<Bet> queryWrapper = new LambdaQueryWrapper<Bet>()
                .orderByDesc(Bet::getId)
                .eq(Bet::getUid, uid)
                .eq(result != null, Bet::getResult, result)
                .eq(bet.getBetType() != null, Bet::getBet_type, bet.getBetType())
                .le(StringUtils.isNotBlank(bet.getEndTime()), Bet::getCreate_time, bet.getEndTime())
                .ge(StringUtils.isNotBlank(bet.getStartTime()), Bet::getCreate_time, bet.getStartTime());
        int count = betService.count(queryWrapper);
        if (count <= 0) {
            return Result.instance().setData(MapTool.Map()
                    .put("list", new ArrayList<>())
                    .put("total", count)
                    .put("stat", summaryCalculation.get())
                    .put("time", System.currentTimeMillis()));
        }
        queryWrapper.lt(bet.getFromId() > 0, Bet::getId, bet.getFromId())
                .last(" limit " + bet.getSize());
        List<Bet> list = betService.list(queryWrapper);
        List<FollowCurrency> followCurrencies = followCurrencyService.list();
        Map<String, String> followCurrencyMap = followCurrencies.stream().collect(Collectors.toMap(FollowCurrency::getSymbol, FollowCurrency::getName));
        List<BetPageVO> vos = list.stream().map(BetPageVO::trans).collect(Collectors.toList());
        vos.forEach(e -> e.setBet_symbol_name(followCurrencyMap.get(e.getBet_symbol())));
        Map<String, Object> map = null;
        try {
            map = summaryCalculation.get();
        } catch (Exception e) {
            ErrorCodeEnum.SYSTEM_ERROR.throwException();
        }
        return Result.instance().setData(MapTool.Map()
                .put("list", vos)
                .put("total", count)
                .put("stat", map)
                .put("time", System.currentTimeMillis()));
    }

    @GetMapping("/steady/section")
    public Result betSteadySection() {
        String betSteadySection = configService.getOrDefaultNoCache(ConfigConstants.BET_STEADY_SECTION, "[10,12,19,21]");
        List<Integer> sectionList = gson.fromJson(betSteadySection, new TypeToken<List<Integer>>() {
        }.getType());
        //当天零点
        LocalDateTime today_start = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        long today_start_time = today_start.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long oneHourMilli = 3_600_000L;
        List<MapTool> collect = sectionList.stream().map(e -> MapTool.Map().put("hour", e).put("milli", today_start_time + oneHourMilli * e)).collect(Collectors.toList());
        return Result.success(collect);
    }

    @GetMapping("result")
    public Result getBetResult() {
        Long uid = requestInitService.uid();
        String redisKey = "bet_result_uid_" + uid;
        BoundListOperations listOps = redisTemplate.boundListOps(redisKey);
        List<Long> ids = new ArrayList<>();
        while (listOps.size() > 0) {
            ids.add((Long) listOps.rightPop());
        }
        if (ids.size() <= 0) {
            return Result.success(Lists.newArrayList());
        }
        List<Bet> list = betService.list(new LambdaQueryWrapper<Bet>().in(Bet::getId, ids));
        List<BetPageVO> vos = list.stream().map(BetPageVO::trans).collect(Collectors.toList());
        return Result.success(vos);
    }

    @GetMapping("/win/fee")
    public Result getBetFee() {
        String plRate1 = configService.getOrDefault(ConfigConstants.BET_RAKE_RATE_NORMAL, "0.2");
        String plRate2 = configService.getOrDefault(ConfigConstants.BET_RAKE_RATE_STEADY, "0.2");
        return Result.success(MapTool.Map()
                .put("rate", 1 - Double.parseDouble(plRate1))
                .put("rate2", 1 - Double.parseDouble(plRate2))
        );
    }


    @GetMapping("/account/balance")
    public Result accountBalance() {
        Long uid;
        Currency currency = currencyService.get((uid = requestInitService.uid()), CurrencyTypeEnum.normal);
        if (Objects.isNull(currency)) {
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }
        DiscountCurrency byId = discountCurrencyService.getById(uid);
        return Result.instance().setData(MapTool.Map()
                .put("remain", TokenCurrencyType.usdt_omni.money(currency.getRemain()))
                .put("freeze", TokenCurrencyType.usdt_omni.money(currency.getFreeze()))
                .put("balance", TokenCurrencyType.usdt_omni.money(currency.getBalance()))
                .put("weak_balance", Objects.isNull(byId) ? 0 : TokenCurrencyType.usdt_omni.money(byId.getBalance()))
        );
    }

    @GetMapping("deferred/result")
    public DeferredResult<Result> getDeferredBetResult() {
        Long uid = requestInitService.uid();
        DeferredResult<Result> output = new DeferredResult<>(58L, () -> {
            String redisKey = "bet_result_uid_" + uid;
            BoundListOperations<String, Object> listOps = redisTemplate.boundListOps(redisKey);
            List<Long> ids = new ArrayList<>();
            while (listOps.size() > 0) ids.add((Long) listOps.rightPop());
            if (ids.size() <= 0) {
                // redis中不存在等待拿取;
                Long newId = (Long) listOps.leftPop(55L, TimeUnit.SECONDS);
                if (Objects.isNull(newId)) {
                    return Result.success(Lists.newArrayList());
                }
                ids.add(newId);
                List<Bet> list = betService.list(new LambdaQueryWrapper<Bet>().in(Bet::getId, ids));
                List<BetPageVO> vos = list.stream().filter(e -> {
                    if (e.getResult() == BetResultEnum.wait) {
                        listOps.leftPush(e.getId());
                        return false;
                    } else {
                        return true;
                    }
                }).map(BetPageVO::trans).collect(Collectors.toList());
                return Result.success(vos);
            } else {
                // redis中存在就拿出来直接返回
                List<Bet> list = betService.list(new LambdaQueryWrapper<Bet>().in(Bet::getId, ids));
                List<BetPageVO> vos = list.stream().filter(e -> {
                    if (e.getResult() == BetResultEnum.wait) {
                        listOps.leftPush(e.getId());
                        return false;
                    } else {
                        return true;
                    }
                }).map(BetPageVO::trans).collect(Collectors.toList());
                return Result.success(vos);
            }
        });
        return output;
    }

    @GetMapping("deferred/result2")
    public DeferredResult<Result> getDeferredBetResult2() {
        Long uid = requestInitService.uid();
        DeferredResult<Result> output = new DeferredResult<>(58L, Result.success(List.of()));
        return output;
    }

    @GetMapping("push")
    public Result pushBetToSettle(String ids) {
        if (StringUtils.isBlank(ids)) {
            return Result.success();
        }
        String[] idArray = ids.split(",");
        for (String id : idArray) {
            CompletableFuture.runAsync(() -> betService.settleBet(Long.parseLong(id)), Constants.COMPLETABLE_FUTURE_EXECUTOR);
        }
        return Result.success();
    }

    @GetMapping("/user/line")
    public Result userKLine(@RequestParam(value = "type") String type,
                            @RequestParam(value = "symbol") String symbol) {
        Long uid = requestInitService.uid();
        BetUserLine byId = betUserLineService.getOne(new LambdaQueryWrapper<BetUserLine>()
                .eq(BetUserLine::getUid, uid)
                .eq(BetUserLine::getSymbol, symbol));
        if (Objects.isNull(byId)) {
            return Result.success(Lists.newArrayList());
        }
        String line_json = byId.getLine_json();
        BetUserLineObj betUserLineObj = new Gson().fromJson(line_json, BetUserLineObj.class);
        return Result.success(betUserLineObj.getUserLine(type));
    }

    @Resource
    private RequestInitService requestInitService;

    @Resource
    private CurrencyService currencyService;

    @Resource
    private DiscountCurrencyService discountCurrencyService;

    @Resource
    private BetService betService;

    @Resource
    private UserIpLogService userIpLogService;

    @Resource
    private ConfigService configService;

    @Resource
    private Gson gson;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private FollowCurrencyService followCurrencyService;

    @Resource
    private BetUserLineService betUserLineService;

}

