package com.tianli.robot.task;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tianli.bet.BetService;
import com.tianli.bet.mapper.Bet;
import com.tianli.bet.mapper.BetResultEnum;
import com.tianli.common.CommonFunction;
import com.tianli.common.async.AsyncService;
import com.tianli.common.lock.RedisLock;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.robot.RobotCouponAchievementService;
import com.tianli.robot.RobotCouponService;
import com.tianli.robot.RobotOrderService;
import com.tianli.robot.RobotResultService;
import com.tianli.robot.mapper.RobotCoupon;
import com.tianli.robot.mapper.RobotCouponAchievement;
import com.tianli.robot.mapper.RobotOrder;
import com.tianli.robot.mapper.RobotResult;
import com.tianli.tool.MapTool;
import com.tianli.tool.WebSocketUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RobotStatTask {

    private static final String LOCK_KEY = "ROBOT:STAT:REDIS:LOCK";

    @Resource
    private RedisLock redisLock;

    @Resource
    private RobotOrderService robotOrderService;

    @Resource
    private RobotCouponAchievementService robotCouponAchievementService;

    @Resource
    private RobotCouponService robotCouponService;

    @Resource
    private RobotResultService robotResultService;

    @Resource
    private AsyncService asyncService;

    @Resource
    private BetService betService;

    @Resource
    private RobotStatTask robotStatTask;

    @Scheduled(cron = "0/1 * * * * ?")
    public void robotBet() {
        asyncService.async(() -> {
            boolean lock = redisLock._lock(LOCK_KEY, 5L, TimeUnit.MINUTES);
            if (!lock) {
                return;
            }
            // TODO 执行统计机器人押注记录
            List<RobotCoupon> list = robotCouponService.list(Wrappers.lambdaQuery(RobotCoupon.class)
                    .eq(RobotCoupon::getScan, 0)
                    .eq(RobotCoupon::getStatus, 2)
                    .orderByAsc(RobotCoupon::getId)
                    .last("LIMIT 10"));
            if (CollUtil.isNotEmpty(list)) {
                for (RobotCoupon coupon : list) {
                    try {
                        robotStatTask.calculate(coupon);
                    } catch (Exception e) {
                        log.warn("订单{}, 计算统计数据异常", coupon.getId(), e);
                    } finally {
                        redisLock.unlock(LOCK_KEY);
                    }
                }
            }
        });
    }

    @Transactional
    public void calculate(RobotCoupon coupon) {
        RobotOrder order = robotOrderService.getOne(Wrappers.lambdaQuery(RobotOrder.class)
                .eq(RobotOrder::getRobot_code, coupon.getId()));
        if (Objects.isNull(order)) {
            // TODO 更新当前订单为scan  = 1
            return;
        }
        List<RobotResult> robotResults = robotResultService.list(Wrappers.lambdaQuery(RobotResult.class)
                .gt(RobotResult::getBet_index, 10000L)
                .eq(RobotResult::getRobot_code, coupon.getId()));
        if (CollectionUtils.isEmpty(robotResults)) {
            robotCouponAchievementService.save(RobotCouponAchievement.builder()
                    .id(CommonFunction.generalId())
                    .create_time(coupon.getUpdate_time())
                    .update_time(coupon.getUpdate_time())
                    .uid(coupon.getUid())
                    .c_id(coupon.getId())
                    .c_code(coupon.getActivation_code())
                    .symbol(order.getCoin())
                    .win_count(0)
                    .lose_count(0)
                    .total_amount(0.0)
                    .profit_amount(0.0)
                    .profit_rate(0.0)
                    .build());
        } else {
            List<Long> betIdList = robotResults.stream().map(RobotResult::getBet_index).collect(Collectors.toList());
            List<Bet> waitList = betService.list(Wrappers.lambdaQuery(Bet.class).eq(Bet::getResult, BetResultEnum.wait).in(Bet::getId, betIdList));
            if (!CollectionUtils.isEmpty(waitList)) return;
            double totalBetAmount = 0.0;
            double totalProfitAmount = 0.0;
            int win = 0;
            int lose = 0;
            for (RobotResult result : robotResults) {
                Bet byId = betService.getById(result.getBet_index());

                totalBetAmount += TokenCurrencyType.usdt_omni.money(byId.getAmount());
                totalProfitAmount += (TokenCurrencyType.usdt_omni.money(byId.getIncome()) - TokenCurrencyType.usdt_omni.money(byId.getAmount()));
                Integer bet_result = result.getBet_result();
                if (bet_result == 0) {
                    lose += 1;
                } else {
                    win += 1;
                }
            }
            robotCouponAchievementService.save(RobotCouponAchievement.builder()
                    .id(CommonFunction.generalId())
                    .create_time(coupon.getUpdate_time())
                    .update_time(coupon.getUpdate_time())
                    .uid(coupon.getUid())
                    .c_id(coupon.getId())
                    .c_code(coupon.getActivation_code())
                    .symbol(order.getCoin())
                    .win_count(win)
                    .lose_count(lose)
                    .total_amount(coupon.getTotal_amount())
                    .profit_amount(totalProfitAmount)
                    .profit_rate(totalProfitAmount / coupon.getTotal_amount())
                    .build());
        }
        boolean update = robotCouponService.update(Wrappers.lambdaUpdate(RobotCoupon.class)
                .set(RobotCoupon::getUpdate_time, LocalDateTime.now())
                .set(RobotCoupon::getScan, 1)
                .eq(RobotCoupon::getId, coupon.getId())
                .eq(RobotCoupon::getScan, 0));
        if (!update) ErrorCodeEnum.SYSTEM_ERROR.throwException();
        RobotCouponAchievement achievement = robotCouponAchievementService.getOne(Wrappers.lambdaQuery(RobotCouponAchievement.class)
                .eq(RobotCouponAchievement::getC_id, coupon.getId())
                .eq(RobotCouponAchievement::getUid, coupon.getUid()));
        WebSocketUtils.convertAndSend("robot_" + coupon.getActivation_code(), MapTool.Map()
                .put("symbol", achievement.getSymbol())
                .put("total_amount", achievement.getTotal_amount())
                .put("profit_amount", achievement.getProfit_amount())
                .put("profit_rate", achievement.getProfit_rate())
                .put("win_count", achievement.getWin_count())
                .put("lose_count", achievement.getLose_count())
                .put("reason", (coupon.getUsed_count() < coupon.getAuto_count()) ? 1 : 0)
        );
    }

}