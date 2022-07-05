package com.tianli.management.fundmanagement;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.tianli.bet.BetService;
import com.tianli.bet.KlineDirectionEnum;
import com.tianli.bet.mapper.Bet;
import com.tianli.bet.mapper.BetBhvPO;
import com.tianli.bet.mapper.BetPO;
import com.tianli.bet.mapper.BetResultEnum;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.kline.FollowCurrencyService;
import com.tianli.kline.mapper.FollowCurrency;
import com.tianli.management.channel.entity.Channel;
import com.tianli.management.channel.entity.ChannelUser;
import com.tianli.management.channel.service.ChannelService;
import com.tianli.management.channel.service.ChannelUserService;
import com.tianli.role.annotation.AdminPrivilege;
import com.tianli.role.annotation.GrcCheckModular;
import com.tianli.role.annotation.Privilege;
import com.tianli.tool.MapTool;
import com.tianli.tool.time.TimeTool;
import com.tianli.user.logs.UserIpLogService;
import com.tianli.user.logs.mapper.UserIpLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("betManage")
public class BetManageController {
    @Resource
    private BetService betService;
    @Resource
    private FollowCurrencyService followCurrencyService;
    @Resource
    private UserIpLogService userIpLogService;

    @Resource
    ChannelUserService channelUserService;

    @Resource
    ChannelService channelService;

    @GetMapping("/old/page")
    @AdminPrivilege(and = Privilege.押注管理)
    public Result page(String phone, BetResultEnum result, String startTime, String endTime,
                       @RequestParam(value = "page", defaultValue = "1") Integer page,
                       @RequestParam(value = "size", defaultValue = "10") Integer size) {
        CompletableFuture<Map<String, Object>> mapCompletableFuture = CompletableFuture.supplyAsync(() -> {
            Map<String, BigDecimal> stat = betService.betStatistics(phone, result, startTime, endTime);
            BigInteger totalAmount = stat.get("totalAmount").toBigInteger();
            BigInteger totalEarn = stat.get("totalEarn").toBigInteger();
            BigInteger totalFee = stat.get("totalFee").toBigInteger();
            BigInteger totalFeeBF = stat.get("totalFeeBF").toBigInteger();
            BigInteger totalIncomeBF = stat.get("totalIncomeBF").toBigInteger();
            return MapTool.Map()
                    .put("totalAmount", TokenCurrencyType.usdt_omni.money(totalAmount))
                    .put("totalIncomeBF", TokenCurrencyType.BF_bep20.money(totalIncomeBF))
                    .put("totalProfit", TokenCurrencyType.usdt_omni.money(totalEarn.subtract(totalFee)))
                    .put("totalFeeBF", TokenCurrencyType.BF_bep20.money(totalFeeBF));
        });
        LambdaQueryWrapper<Bet> queryWrapper = new LambdaQueryWrapper<Bet>()
                .orderByDesc(Bet::getId)
                .like(StringUtils.isNotBlank(phone), Bet::getUid_username, phone)
                .eq(Objects.nonNull(result), Bet::getResult, result)
                .ge(StringUtils.isNotBlank(startTime), Bet::getCreate_time, startTime)
                .le(StringUtils.isNotBlank(endTime), Bet::getCreate_time, endTime);
        Page<Bet> betPage = betService.page(new Page<>(page, size), queryWrapper);
        List<BetManageVO> betManageVOS = betPage.getRecords().stream().map(BetManageVO::trans).collect(Collectors.toList());
        List<FollowCurrency> followCurrencies = followCurrencyService.list();
        Map<String, String> followCurrencyMap = followCurrencies.stream().collect(Collectors.toMap(FollowCurrency::getSymbol, FollowCurrency::getName));
        if(!CollectionUtils.isEmpty(betManageVOS)){
            List<Long> betIdList = betManageVOS.stream().map(BetManageVO::getId).collect(Collectors.toList());
            List<UserIpLog> list = userIpLogService.list(Wrappers.lambdaQuery(UserIpLog.class)
                    .eq(UserIpLog::getBehavior, GrcCheckModular.下注)
                    .in(UserIpLog::getBehavior_id, betIdList));
            Map<Long, UserIpLog> longUserIpLogMap = list.stream().collect(Collectors.toMap(UserIpLog::getBehavior_id, Function.identity(), (a, b) -> a));
            betManageVOS.forEach(e -> {
                UserIpLog userIpLog = longUserIpLogMap.get(e.getId());
                if(Objects.isNull(userIpLog)){
                    return;
                }
                e.fillOtherProperties(userIpLog);
            });
        }
        betManageVOS.forEach(e -> e.setBet_symbol_name(followCurrencyMap.get(e.getBet_symbol())));
        Map<String, Object> stat = null;
        try {
            stat = mapCompletableFuture.get();
        } catch (Exception e) {
            ErrorCodeEnum.SYSTEM_ERROR.throwException();
        }
        return Result.instance().setData(MapTool.Map()
                .put("total", betPage.getTotal())
                .put("list", betManageVOS)
                .put("stat", stat));
    }


    @GetMapping("page")
    @AdminPrivilege(or = {Privilege.押注管理,Privilege.推广统计cps})
    public Result newPage(String phone,
                          BetResultEnum result,
                          @RequestParam(value = "ip", required = false) String ip,
                          @RequestParam(value = "equipment", required = false) String equipment,
                          @RequestParam(value = "grc_result", required = false) Boolean grc_result,
                          String startTime,
                          String endTime,
                          Long channelId,
                          @RequestParam(value = "page", defaultValue = "1") Integer page,
                          @RequestParam(value = "size", defaultValue = "10") Integer size) {

        Set<Long> userIds = getUserIds(channelId);
        if (ObjectUtil.isNotNull(channelId) && CollUtil.isEmpty(userIds)) {
            return Result.instance().setData(MapTool.Map()
                    .put("total", 0)
                    .put("list", Lists.newArrayList())
                    .put("stat", MapTool.Map()
                            .put("totalAmount", 0.0)
                            .put("totalIncomeBF", 0.0)
                            .put("totalProfit", 0.0)
                            .put("totalFeeBF", 0.0)));
        }
        CompletableFuture<Map<String, Object>> mapCompletableFuture = CompletableFuture.supplyAsync(() -> {
            Map<String, BigDecimal> stat = betService.betStatistics(ip, equipment, grc_result, phone, result, startTime, endTime,userIds);
            BigInteger totalAmount = stat.get("totalAmount").toBigInteger();
            BigInteger totalEarn = stat.get("totalEarn").toBigInteger();
            BigInteger totalFee = stat.get("totalFee").toBigInteger();
            BigInteger totalFeeBF = stat.get("totalFeeBF").toBigInteger();
            BigInteger totalIncomeBF = stat.get("totalIncomeBF").toBigInteger();
            return MapTool.Map()
                    .put("totalAmount", TokenCurrencyType.usdt_omni.money(totalAmount))
                    .put("totalIncomeBF", TokenCurrencyType.BF_bep20.money(totalIncomeBF))
                    .put("totalProfit", TokenCurrencyType.usdt_omni.money(totalEarn.subtract(totalFee)))
                    .put("totalFeeBF", TokenCurrencyType.BF_bep20.money(totalFeeBF));
        });
        int total = betService.betCount(ip, equipment, grc_result, phone, result, startTime, endTime,userIds);
        if(total <= 0){
            return Result.instance().setData(MapTool.Map()
                    .put("total", total)
                    .put("list", Lists.newArrayList())
                    .put("stat", MapTool.Map()
                            .put("totalAmount", 0.0)
                            .put("totalIncomeBF", 0.0)
                            .put("totalProfit", 0.0)
                            .put("totalFeeBF", 0.0)));
        }
        List<FollowCurrency> followCurrencies = followCurrencyService.list();
        Map<String, String> followCurrencyMap = followCurrencies.stream().collect(Collectors.toMap(FollowCurrency::getSymbol, FollowCurrency::getName));

        List<BetPO> poList = betService.betList(ip, equipment, grc_result, phone, result, startTime, endTime,userIds, page, size);

        Map<String, Object> stat = null;
        try {
            stat = mapCompletableFuture.get();
        } catch (Exception e) {
            ErrorCodeEnum.SYSTEM_ERROR.throwException();
        }
        List<BetManageVO> voList = poList.stream().map(BetManageVO::convert).collect(Collectors.toList());
        Set<Long> uids = voList.stream().map(BetManageVO::getUid).collect(Collectors.toSet());
        Map<Long,Channel> channelUserMap = new HashMap<>();
        List<ChannelUser> channelUsers = channelUserService.list(Wrappers.lambdaQuery(ChannelUser.class)
                .in(ChannelUser::getUser_id, uids));
        if (CollUtil.isNotEmpty(channelUsers)) {
            Set<Long> channelIds = channelUsers.stream().map(ChannelUser::getChannel_id).collect(Collectors.toSet());
            List<Channel> channels = channelService.listByIds(channelIds);
            Map<Long, Channel> channelMap = channels.stream().collect(Collectors.toMap(Channel::getId, Function.identity()));
            for (ChannelUser channelUser : channelUsers) {
                channelUserMap.put(channelUser.getUser_id(),channelMap.get(channelUser.getChannel_id()));
            }
        }
        voList.forEach(e -> {
            e.setBet_symbol_name(followCurrencyMap.get(e.getBet_symbol()));
            Channel channel = channelUserMap.get(e.getUid());
            if (ObjectUtil.isNotNull(channel)) {
                e.setChannel_name(channel.getAdmin_username());
            }
        });
        return Result.instance().setData(MapTool.Map()
                .put("total", total)
                .put("list", voList)
                .put("stat", stat));
    }

    /**
     *
     * @param channelId
     * @return
     */
    private Set<Long> getUserIds(Long channelId) {
        Set<Long> userIds = new HashSet<>();
        if (ObjectUtil.isNotNull(channelId)) {
            List<Long> channelIds = channelUserService.getChannelIds(channelId);
            List<ChannelUser> channelUsers = channelUserService.findByChannelId(channelIds);
            if (CollUtil.isNotEmpty(channelUsers)) {
                userIds = channelUsers.stream().map(ChannelUser::getUser_id).collect(Collectors.toSet());
            }
        }
        return userIds;
    }

    /**
     * 用户行为内部押注管理
     */
    @GetMapping("/behavior/page")
    @AdminPrivilege(and = Privilege.押注管理)
    public Result newPage_(String phone,
                          BetResultEnum result,
                          @RequestParam(value = "ip", required = false) String ip,
                          @RequestParam(value = "equipment", required = false) String equipment,
                          @RequestParam(value = "grc_result", required = false) Boolean grc_result,
                          String startTime,
                          String endTime,
                          @RequestParam(value = "page", defaultValue = "1") Integer page,
                          @RequestParam(value = "size", defaultValue = "5") Integer size) {
        CompletableFuture<Map<String, Object>> mapCompletableFuture = CompletableFuture.supplyAsync(() -> {
            Map<String, BigDecimal> stat = betService.betStatistics(ip, equipment, grc_result, phone, result, startTime, endTime,null);
            BigInteger totalAmount = stat.get("totalAmount").toBigInteger();
            BigInteger totalEarn = stat.get("totalEarn").toBigInteger();
            BigInteger totalFee = stat.get("totalFee").toBigInteger();
            BigInteger totalFeeBF = stat.get("totalFeeBF").toBigInteger();
            BigInteger totalIncomeBF = stat.get("totalIncomeBF").toBigInteger();
            return MapTool.Map()
                    .put("totalAmount", TokenCurrencyType.usdt_omni.money(totalAmount))
                    .put("totalIncomeBF", TokenCurrencyType.BF_bep20.money(totalIncomeBF))
                    .put("totalProfit", TokenCurrencyType.usdt_omni.money(totalEarn.subtract(totalFee)))
                    .put("totalFeeBF", TokenCurrencyType.BF_bep20.money(totalFeeBF));
        });
        int total = betService.betCount(ip, equipment, grc_result, phone, result, startTime, endTime,null);
        if(total <= 0){
            return Result.instance().setData(MapTool.Map()
                    .put("total", total)
                    .put("list", Lists.newArrayList())
                    .put("stat", MapTool.Map()
                            .put("totalAmount", 0.0)
                            .put("totalIncomeBF", 0.0)
                            .put("totalProfit", 0.0)
                            .put("totalFeeBF", 0.0)));
        }
        List<BetPO> poList = betService.betList(ip, equipment, grc_result, phone, result, startTime, endTime,null, page, size);
        BetPO betPOFirst = poList.get(0);
        LocalDateTime firstTime = betPOFirst.getCreate_time();
        LocalDateTime localDateTime = firstTime.minusSeconds(2L);
        String dateTimeDisplayString = TimeTool.getDateTimeDisplayString(localDateTime);
        BetPO betPOLast = poList.get(poList.size() - 1);
        LocalDateTime lastTime = betPOLast.getCreate_time();
        LocalDateTime localDateTime2 = lastTime.plusSeconds(2L);
        String dateTimeDisplayString2 = TimeTool.getDateTimeDisplayString(localDateTime2);
        List<BetPO> poList2 = betService.betList(null, null, null, null, null, null, dateTimeDisplayString, dateTimeDisplayString2, null, null);
        Map<String, List<BetPO>> listMap = poList2.stream().collect(Collectors.groupingBy(e -> TimeTool.getDateTimeDisplayString(e.getCreate_time()) + "-" +  e.getBet_direction().name()));

        List<FollowCurrency> followCurrencies = followCurrencyService.list();
        Map<String, String> followCurrencyMap = followCurrencies.stream().collect(Collectors.toMap(FollowCurrency::getSymbol, FollowCurrency::getName));

        // 汇总计算
        List<BetBhvPO> betBhvPOS = poList.parallelStream().map(e -> {
            LocalDateTime create_time1 = e.getCreate_time();
            KlineDirectionEnum opposeDirection = e.getBet_direction().oppose();
            List<BetPO> collect = listMap.entrySet().parallelStream().filter(ee -> {
                String key = ee.getKey();
                return key.equals(TimeTool.getDateTimeDisplayString(create_time1.plusSeconds(1)) + "-" + opposeDirection.name())
                        || key.equals(TimeTool.getDateTimeDisplayString(create_time1.plusSeconds(2)) + "-" + opposeDirection.name())
                        || key.equals(TimeTool.getDateTimeDisplayString(create_time1) + "-" + opposeDirection.name())
                        || key.equals(TimeTool.getDateTimeDisplayString(create_time1.minusSeconds(1)) + "-" + opposeDirection.name())
                        || key.equals(TimeTool.getDateTimeDisplayString(create_time1.minusSeconds(2)) + "-" + opposeDirection.name());
            }).map(Map.Entry::getValue).flatMap(Collection::stream).collect(Collectors.toList());
            BetBhvPO betBhvPO = BetBhvPO.builder().build();
            BeanUtils.copyProperties(e, betBhvPO);
            betBhvPO.setCount(collect.size());
            betBhvPO.setList(collect);
            betBhvPO.setBet_symbol_name(followCurrencyMap.get(e.getBet_symbol()));
            return betBhvPO;
        }).collect(Collectors.toList());
        Map<String, Object> stat = null;
        try {
            stat = mapCompletableFuture.get();
        } catch (Exception e) {
            ErrorCodeEnum.SYSTEM_ERROR.throwException();
        }
        List<BetManageVO> voList = poList.stream().map(BetManageVO::convert).collect(Collectors.toList());
        voList.stream().forEach(e -> {
            e.setBet_symbol_name(followCurrencyMap.get(e.getBet_symbol()));
        });
        return Result.instance().setData(MapTool.Map()
                .put("total", total)
                .put("list", betBhvPOS)
                .put("stat", stat));
    }
}
