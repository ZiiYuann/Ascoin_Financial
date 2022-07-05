package com.tianli.robot;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Maps;
import com.tianli.bet.BetService;
import com.tianli.bet.controller.BetDTO;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.kline.FollowCurrencyService;
import com.tianli.kline.mapper.FollowCurrency;
import com.tianli.robot.dtos.RobotCouponAchievementDTO;
import com.tianli.robot.mapper.RobotCoupon;
import com.tianli.robot.mapper.RobotCouponAchievement;
import com.tianli.robot.mapper.RobotOrder;
import com.tianli.robot.mapper.RobotResult;
import com.tianli.user.UserService;
import com.tianli.user.mapper.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;


@Slf4j
@Service
public class RobotService {

    @Resource
    private RobotOrderService robotOrderService;

    @Resource
    private RobotCouponService robotCouponService;

    @Resource
    private RobotCouponAchievementService robotCouponAchievementService;

    @Resource
    private RobotResultService robotResultService;

    @Resource
    private FollowCurrencyService followCurrencyService;

    @Resource
    private UserService userService;



    @Transactional
    public void restartRobot(RobotCoupon robotCoupon, long uid, String coin) {
        if (StringUtils.isBlank(robotCoupon.getInterval_time())) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }
        RobotOrder one = robotOrderService.getOne(Wrappers.lambdaQuery(RobotOrder.class)
                .eq(RobotOrder::getUid, uid)
        );

        if (Objects.nonNull(one) && one.getStatus()) {
            ErrorCodeEnum.throwException("机器人正在运行中");
        }
        if (Objects.nonNull(one)){
            Long robot_code = one.getRobot_code();
            RobotCoupon usedCoupon = robotCouponService.getById(robot_code);

            if((Objects.equals(usedCoupon.getStatus(), 2) && Objects.equals(usedCoupon.getScan(), 0))){
                ErrorCodeEnum.throwException("机器人正在结算中");
            }
        }

        String[] intervalArr = robotCoupon.getInterval_time().split("-");
        if (intervalArr.length < 2) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }
        boolean update = robotCouponService.update(Wrappers.lambdaUpdate(RobotCoupon.class)
                .set(RobotCoupon::getStatus, 1)
                .set(RobotCoupon::getUsed_time, LocalDateTime.now())
                .set(RobotCoupon::getUpdate_time, LocalDateTime.now())
                .eq(RobotCoupon::getId, robotCoupon.getId())
                .eq(RobotCoupon::getStatus, 0)
        );
        if (!update) ErrorCodeEnum.SYSTEM_ERROR.throwException();
        int nextTime = ThreadLocalRandom.current().nextInt(Integer.parseInt(intervalArr[0]), Integer.parseInt(intervalArr[1]) + 1);
        if (Objects.isNull(one)) {
            robotOrderService.saveOne(uid, robotCoupon.getAuto_count(), robotCoupon.getAuto_amount(), robotCoupon.getId(), robotCoupon.getInterval_time(), (System.currentTimeMillis() / 1000) + nextTime, coin);
        } else {
            robotOrderService.update(Wrappers.lambdaUpdate(RobotOrder.class)
                    .set(RobotOrder::getStatus, Boolean.TRUE)
                    .set(RobotOrder::getCoin, coin)
                    .set(RobotOrder::getCount, robotCoupon.getAuto_count())
                    .set(RobotOrder::getAmount, robotCoupon.getAuto_amount())
                    .set(RobotOrder::getNext_bet_time, (System.currentTimeMillis() / 1000) + nextTime)
                    .set(RobotOrder::getInterval_time, robotCoupon.getInterval_time())
                    .set(RobotOrder::getRobot_code, robotCoupon.getId())
                    .eq(RobotOrder::getId, one.getId())
            );
        }
        robotResultService.saveList(robotCoupon.getId(), robotCoupon.getAuto_count(), robotCoupon.getWin_rate());
    }

    @Transactional
    public void stop(Long uid) {
        RobotOrder one = robotOrderService.getOne(Wrappers.lambdaQuery(RobotOrder.class)
                .eq(RobotOrder::getUid, uid)
        );
        if (Objects.isNull(one)) {
            log.warn("用户[" + uid + "]不存在机器人订单 !");
            return;
        }

        if (!one.getStatus()) {
            log.warn("用户[" + uid + "]机器人订单不在运行状态, 不能执行停止操作 !");
            return;
        }

        robotOrderService.update(Wrappers.lambdaUpdate(RobotOrder.class)
                .set(RobotOrder::getStatus, Boolean.FALSE)
                .eq(RobotOrder::getId, one.getId())
        );

        robotCouponService.update(Wrappers.lambdaUpdate(RobotCoupon.class)
                .set(RobotCoupon::getUpdate_time, LocalDateTime.now())
                .set(RobotCoupon::getStatus, 2)
                .eq(RobotCoupon::getId, one.getRobot_code()));

    }

    public RobotOrder getRobotByUid(Long uid) {
        return robotOrderService.getOne(Wrappers.lambdaQuery(RobotOrder.class)
                .eq(RobotOrder::getUid, uid)
        );
    }

    @Resource
    private BetService betService;

    @Transactional
    public Long autoBet(BetDTO betDTO, RobotOrder robotOrder) {
        boolean decrementCount = robotOrderService.decrementCount(robotOrder.getId());
        if (!decrementCount) {
            ErrorCodeEnum.TOO_FREQUENT.throwException();
        }
        Long orderId = betService.submit(betDTO, true);
        Integer befCount = robotOrder.getCount();
        int count = befCount;
        while (count > 0) {
            boolean update = robotResultService.update(Wrappers.lambdaUpdate(RobotResult.class)
                    .set(RobotResult::getBet_index, orderId)
                    .set(RobotResult::getUpdate_time, LocalDateTime.now())
                    .eq(RobotResult::getRobot_code, robotOrder.getRobot_code())
                    .eq(RobotResult::getBet_index, count)
            );
            if (update) {
                break;
            } else {
                count--;
            }
        }

        String interval_time = robotOrder.getInterval_time();

        long nextTime = 100;
        if (StringUtils.isNotBlank(interval_time)) {
            String timeSplitStr = "-";
            String[] intervalSplit = interval_time.split(timeSplitStr);
            if (intervalSplit.length >= 2) {
                nextTime = ThreadLocalRandom.current().nextInt(Integer.parseInt(intervalSplit[0]), Integer.parseInt(intervalSplit[1]) + 1);
            }
        }

        boolean update = robotOrderService.update(Wrappers.lambdaUpdate(RobotOrder.class)
                .set(RobotOrder::getNext_bet_time, (System.currentTimeMillis() / 1000) + nextTime)
                .eq(RobotOrder::getId, robotOrder.getId())
        );
        if (!update) ErrorCodeEnum.SYSTEM_ERROR.throwException();
        return orderId;
    }

    public void bindCode(long uid, String code) {
        User user = userService._get(uid);
        if (Objects.isNull(user)) ErrorCodeEnum.UNLOIGN.throwException();
        robotCouponService.update(Wrappers.lambdaUpdate(RobotCoupon.class)
                .set(RobotCoupon::getUid, uid)
                .set(RobotCoupon::getUsername, user.getUsername())
                .set(RobotCoupon::getUpdate_time, LocalDateTime.now())
                .eq(RobotCoupon::getActivation_code, code)
                .eq(RobotCoupon::getStatus, 0)
                .eq(RobotCoupon::getUid, -1)
        );
    }

    public Map<String, Object> record(long uid, int page, int size) {

        // 查询记录
        Page<RobotCoupon> robotCouponPage = robotCouponService.page(new Page<>(page, size), Wrappers.lambdaQuery(RobotCoupon.class)
                .eq(RobotCoupon::getUid, uid)
                .eq(RobotCoupon::getStatus, 2)
                .eq(RobotCoupon::getScan, 1)
                .orderByDesc(RobotCoupon::getUsed_time));
        List<RobotCoupon> records = robotCouponPage.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return Map.of("total", 0, "list", List.of(), "sum", Map.of());
        }
        CompletableFuture<Map<String, Double>> mapCompletableFuture = CompletableFuture.supplyAsync(() -> {
            Map<String, Double> map = robotCouponAchievementService.getTotalAmount(uid);
            map.put("profitRate", map.get("profitAmount")/map.get("betAmount"));
            return map;
        });
        // 封装记录
        List<Long> idList = records.stream().map(RobotCoupon::getId).collect(Collectors.toList());
        List<RobotCouponAchievement> list = robotCouponAchievementService.list(Wrappers.lambdaQuery(RobotCouponAchievement.class).in(RobotCouponAchievement::getC_id, idList));
        Map<Long, RobotCouponAchievement> map = list.stream().collect(Collectors.toMap(RobotCouponAchievement::getC_id, e -> e));
        List<RobotCouponAchievementDTO> dtoList = records.stream().map(e -> RobotCouponAchievementDTO.convert(e, map.get(e.getId()))).collect(Collectors.toList());
        List<FollowCurrency> followCurrencies = followCurrencyService.list();
        Map<String, String> followCurrencyMap = followCurrencies.stream().collect(Collectors.toMap(FollowCurrency::getSymbol, FollowCurrency::getName));
        dtoList.forEach(e -> {
            e.setSymbol_name(followCurrencyMap.get(e.getSymbol()));
        });
        // 处查询汇总数据
        Map<String, Double> stringDoubleMap;
        try {
            stringDoubleMap = mapCompletableFuture.get();
        } catch (Exception e) {
            e.printStackTrace();
            stringDoubleMap = Maps.newHashMap();
        }
        return Map.of("total", robotCouponPage.getTotal(), "list", dtoList, "sum", stringDoubleMap);
    }
}
