package com.tianli.robot.controller;


import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tianli.common.init.RequestInitService;
import com.tianli.common.lock.RedisLock;
import com.tianli.currency.CurrencyService;
import com.tianli.currency.CurrencyTypeEnum;
import com.tianli.currency.DiscountCurrencyService;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.currency.mapper.Currency;
import com.tianli.currency.mapper.DiscountCurrency;
import com.tianli.currency_token.mapper.CurrencyCoinEnum;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.loan.entity.LoanCurrency;
import com.tianli.loan.service.ILoanCurrencyService;
import com.tianli.robot.RobotCouponAchievementService;
import com.tianli.robot.RobotCouponService;
import com.tianli.robot.RobotService;
import com.tianli.robot.mapper.RobotCoupon;
import com.tianli.robot.mapper.RobotCouponAchievement;
import com.tianli.robot.task.RobotBetTask;
import com.tianli.tool.MapTool;
import com.tianli.user.UserService;
import com.tianli.user.mapper.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 返佣表 前端控制器
 * </p>
 *
 * @author hd
 * @since 2020-12-09
 */
@RestController
@RequestMapping("/robot")
public class RobotOrderController {

    @Resource
    private RobotService robotService;

    @Resource
    private RequestInitService requestInitService;

    @Resource
    private UserService userService;

    @Resource
    private CurrencyService currencyService;

    @Resource
    private DiscountCurrencyService discountCurrencyService;

    @Resource
    private RobotCouponService robotCouponService;

    @Resource
    private RobotCouponAchievementService robotCouponAchievementService;

    @Resource
    ILoanCurrencyService loanCurrencyService;

    @Resource
    private RedisLock redisLock;

    @PostMapping("/start/{coin}/code/{code}")
    public Result start(@PathVariable("coin") String coin, @PathVariable("code") String code, @RequestParam("username") String username) {
        Long uid = requestInitService._uid();
        if (Objects.isNull(uid)) {
            if (StringUtils.isBlank(username)) {
                ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
            }
            User user = userService._getByUsername(username);
            if (Objects.isNull(user)) {
                ErrorCodeEnum.USER_NOT_EXIST.throwException();
            }
            uid = user.getId();
        }
        RobotCoupon robotCoupon = robotCouponService.getByCode(code);
        if (Objects.isNull(robotCoupon)) {
            ErrorCodeEnum.throwException("请输入正确的激活码");
        }
        if (!Objects.equals(robotCoupon.getUid(), uid)) {
            ErrorCodeEnum.throwException("请使用已拥有的激活码");
        }
        if (!Objects.equals(robotCoupon.getStatus(), 0)) {
            ErrorCodeEnum.throwException("激活码已使用");
        }

        Currency currency = currencyService.get(uid, CurrencyTypeEnum.normal);
        LoanCurrency loanCurrency = loanCurrencyService.findByUid(uid, CurrencyCoinEnum.usdt);
        BigDecimal remain = TokenCurrencyType.usdt_omni._money(currency.getRemain()).add(loanCurrency.getBalance());
        if (remain.compareTo(Convert.toBigDecimal(robotCoupon.getTotal_amount())) < 0) {
            ErrorCodeEnum.throwException("可用余额不足，无法使用激活码");
        }
        redisLock.lock("restartRobot_" + robotCoupon.getUid(), 3L, TimeUnit.MINUTES);
        try {
            robotService.restartRobot(robotCoupon, uid, coin);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            redisLock.unlock("restartRobot_" + robotCoupon.getUid());
        }

        return Result.success();
    }

    @PostMapping("/check/{code}")
    public Result checkCode(@PathVariable("code") String code, @RequestParam("username") String username) {
        Long uid = requestInitService._uid();
        if (Objects.isNull(uid)) {
            if (StringUtils.isBlank(username)) {
                ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
            }
            User user = userService._getByUsername(username);
            if (Objects.isNull(user)) {
                ErrorCodeEnum.USER_NOT_EXIST.throwException();
            }
            uid = user.getId();
        }
        RobotCoupon robotCoupon = robotCouponService.getByCode(code);
        if (Objects.isNull(robotCoupon)) {
            ErrorCodeEnum.throwException("请输入正确的激活码");
        }
        if (!Objects.equals(robotCoupon.getStatus(), 0)) {
            ErrorCodeEnum.throwException("激活码已使用");
        }
        if (!Objects.equals(robotCoupon.getUid(), -1L)) {
            if (!Objects.equals(robotCoupon.getUid(), uid)) ErrorCodeEnum.throwException("激活码已使用");
            else return Result.success(robotCoupon);
        }

        redisLock.lock("robot_check_code_" + code, 3L, TimeUnit.MINUTES);
        try {
            robotService.bindCode(uid, code);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            redisLock.unlock("robot_check_code_" + code);
        }

        return Result.success(robotCoupon);
    }

    //    @PostMapping("/stop")
    public Result stop() {
        Long uid = requestInitService.uid();
        robotService.stop(uid);
        return Result.success();
    }

    @GetMapping("/my")
    public Result my(@RequestParam("username") String username,
                     @RequestParam(value = "code",defaultValue = "--") String code) {
        Long uid = requestInitService._uid();
        if (Objects.isNull(uid)) {
            if (StringUtils.isBlank(username)) {
                ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
            }
            User user = userService._getByUsername(username);
            if (Objects.isNull(user)) {
                ErrorCodeEnum.USER_NOT_EXIST.throwException();
            }
            uid = user.getId();
        }
        MapTool res;
        RobotCoupon robotCoupon = robotCouponService.getByCode(code);
        Currency currency = currencyService.get(uid, CurrencyTypeEnum.normal);
        LoanCurrency loanCurrency = loanCurrencyService.findByUid(uid, CurrencyCoinEnum.usdt);
        BigDecimal remain = ObjectUtil.isNull(currency) ? BigDecimal.ZERO : TokenCurrencyType.usdt_omni._money(currency.getRemain());
        remain = remain.add(loanCurrency.getBalance());
        DiscountCurrency byId = discountCurrencyService.getById(uid);
        if (Objects.nonNull(robotCoupon)) {
            int count = robotCoupon.getAuto_count() - robotCoupon.getUsed_count();
            int status = robotCoupon.getStatus();

           /* if (Objects.equals(robotCoupon.getStatus(), 1)) {
                RobotOrder order = robotService.getRobotByUid(uid);
                if (Objects.nonNull(order)) {
                    count = order.getCount();
                }
            }*/
            res = MapTool.Map()
                    .put("remain", remain)
                    .put("weak_balance", Objects.isNull(byId) ? 0 : TokenCurrencyType.usdt_omni.money(byId.getBalance()))
                    .put("count", count)
                    .put("amount", robotCoupon.getAuto_amount())
                    .put("total_amount", robotCoupon.getTotal_amount())
                    .put("robot", robotCoupon.getWin_rate())
                    .put("reason", 0)
                    .put("status", status);

            if (status == 2) {
                if (robotCoupon.getScan() == 1) {
                    // 判断结束的原因
                    if (robotCoupon.getUsed_count() < robotCoupon.getAuto_count()) {
                        res.put("reason", 1);
                    }
                } else {
                    res.put("status", 1);
                }
            }

        } else {
            res = MapTool.Map()
                    .put("remain", remain)
                    .put("weak_balance", Objects.isNull(byId) ? 0 : TokenCurrencyType.usdt_omni.money(byId.getBalance()))
                    .put("count", 0)
                    .put("amount", 0)
                    .put("total_amount", 0)
                    .put("robot", 0)
                    .put("status", 0);
        }
        return Result.success(res);
    }

    @Resource
    RobotBetTask robotBetTask;

    @GetMapping("/task/{name}")
    public Result taskTrigger(@PathVariable("name") String name) {
        if ("reset".equals(name)) {
            robotBetTask.reset();
            return Result.success("reset触发成功");
        } else if ("robotBet".equals(name)) {
            robotBetTask.robotBet();
            return Result.success("robotBet触发成功");
        }
        return Result.fail("没有对应task");
    }

    @GetMapping("/record")
    public Result record(@RequestParam("username") String username,
                         @RequestParam(value = "page", defaultValue = "1") Integer page,
                         @RequestParam(value = "size", defaultValue = "10") Integer size) {
        if (StringUtils.isBlank(username)) ErrorCodeEnum.SYSTEM_ERROR.throwException();
        User user = userService._getByUsername(username);
        if (Objects.isNull(user)) ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        Map<String, Object> res = robotService.record(user.getId(), page, size);
        return Result.success(res);
    }

    @GetMapping("/achievements")
    public Result taskTrigger(@RequestParam("username") String username,
                              @RequestParam(value = "code", required = false) String code) {
        if (StringUtils.isBlank(username)) ErrorCodeEnum.SYSTEM_ERROR.throwException();
        User user = userService._getByUsername(username);
        if (Objects.isNull(user)) ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        RobotCoupon robotCoupon;
        if (StringUtils.isBlank(code)) {
            // 拿最新的激活码数据
            robotCoupon = robotCouponService.getOne(Wrappers.lambdaQuery(RobotCoupon.class)
                    .eq(RobotCoupon::getUid, user.getId())
                    .orderByDesc(RobotCoupon::getId)
                    .last("LIMIT 1"));
            if (Objects.isNull(robotCoupon)) {
                return Result.success();
            }
        } else {
            // 拿当前的数据
            // 拿最新的激活码数据
            robotCoupon = robotCouponService.getOne(Wrappers.lambdaQuery(RobotCoupon.class)
                    .eq(RobotCoupon::getUid, user.getId())
                    .eq(RobotCoupon::getActivation_code, code)
                    .orderByDesc(RobotCoupon::getId)
                    .last("LIMIT 1"));
            if (Objects.isNull(robotCoupon)) {
                ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
            }
        }
        RobotCouponAchievement achievement = robotCouponAchievementService.getOne(Wrappers.lambdaQuery(RobotCouponAchievement.class)
                .eq(RobotCouponAchievement::getC_id, robotCoupon.getId())
                .eq(RobotCouponAchievement::getUid, user.getId()));
        if (Objects.isNull(achievement)) ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        return Result.success(MapTool.Map()
                .put("symbol", achievement.getSymbol())
                .put("total_amount", achievement.getTotal_amount())
                .put("profit_amount", achievement.getProfit_amount())
                .put("profit_rate", achievement.getProfit_rate())
                .put("win_count", achievement.getWin_count())
                .put("lose_count", achievement.getLose_count()));
    }

}

