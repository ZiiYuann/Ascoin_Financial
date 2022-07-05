package com.tianli.management.actvccode;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.common.CommonFunction;
import com.tianli.common.init.admin.AdminContent;
import com.tianli.common.init.admin.AdminInfo;
import com.tianli.exception.ErrCodeException;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.robot.RobotCouponAchievementService;
import com.tianli.robot.RobotCouponService;
import com.tianli.robot.mapper.RobotCoupon;
import com.tianli.robot.mapper.RobotCouponAchievement;
import com.tianli.role.annotation.AdminPrivilege;
import com.tianli.role.annotation.Privilege;
import com.tianli.tool.MapTool;
import com.tianli.tool.captcha.Randoms;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/mgt/atc")
public class AtcMgtController {

    @Resource
    private RobotCouponService robotCouponService;

    @Resource
    private RobotCouponAchievementService robotCouponAchievementService;

    @GetMapping("/page")
    @AdminPrivilege(and = Privilege.激活码管理)
    public Result page(AtcMgtPageReq req) {
        Page<RobotCoupon> page = robotCouponService.page(new Page<>(req.getPage(), req.getSize()),
                Wrappers.lambdaQuery(RobotCoupon.class)
                        .eq(StringUtils.isNotBlank(req.getUsername()), RobotCoupon::getUsername, req.getUsername())
                        .eq(StringUtils.isNotBlank(req.getActivation_code()), RobotCoupon::getActivation_code, req.getActivation_code())
                        .eq(Objects.nonNull(req.getStatus()), RobotCoupon::getStatus, req.getStatus())
                        .orderByDesc(RobotCoupon::getId)
        );
        List<RobotCoupon> records = page.getRecords();
        List<AtcPageVO> vos = new ArrayList<>();
        if (CollUtil.isNotEmpty(records)) {
            Set<Long> robotCouponIds = records.stream().map(RobotCoupon::getId).collect(Collectors.toSet());
            List<RobotCouponAchievement> robotCouponAchievements = robotCouponAchievementService.list(Wrappers.lambdaQuery(RobotCouponAchievement.class)
                    .in(RobotCouponAchievement::getC_id, robotCouponIds));
            Map<Long, RobotCouponAchievement> robotCouponAchievementMap = new HashMap<>();
            if (CollUtil.isNotEmpty(robotCouponAchievements)) {
                robotCouponAchievementMap = robotCouponAchievements.stream().collect(Collectors.toMap(RobotCouponAchievement::getC_id, Function.identity(), (v1, v2) -> v1));
            }
            Map<Long, RobotCouponAchievement> finalRobotCouponAchievementMap = robotCouponAchievementMap;
            vos = records.stream().map(robotCoupon -> AtcPageVO.convert(robotCoupon, finalRobotCouponAchievementMap.get(robotCoupon.getId()))).collect(Collectors.toList());
        }

        return Result.instance().setData(MapTool.Map()
                .put("total", page.getTotal())
                .put("list", vos));
    }

    @GetMapping("/info/{id}")
    @AdminPrivilege(and = {Privilege.激活码管理})
    public Result info(@PathVariable("id") Long id) {
        RobotCoupon robotCoupon = robotCouponService.getById(id);
        if (Objects.isNull(robotCoupon)) ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        RobotCouponAchievement robotCouponAchievement = robotCouponAchievementService.getOne(Wrappers.lambdaQuery(RobotCouponAchievement.class)
                .eq(RobotCouponAchievement::getC_id, id));
        return Result.success(AtcPageVO.convert(robotCoupon, robotCouponAchievement));
    }

    @PostMapping("/save")
    @AdminPrivilege(and = {Privilege.激活码管理})
    public Result audit(@RequestBody @Valid RobotCouponSaveReq req) {
        AdminInfo adminInfo = AdminContent.get();
//
//        if (!Objects.equals(req.getAuto_amount() * req.getAuto_count(), req.getTotal_amount())) {
//            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
//        }

        String code = Randoms.alphaString();
        while (Objects.nonNull(robotCouponService.getByCode(code))) {
            code = Randoms.alphaString();
        }

        LocalDateTime now = LocalDateTime.now();
        robotCouponService.save(RobotCoupon.builder()
                .id(CommonFunction.generalId())
                .create_time(now)
                .update_time(now)
                .activation_code(code)
                .total_amount(req.getTotal_amount())
                .auto_count(req.getAuto_count())
                .auto_amount(req.getAuto_amount())
                .interval_time(req.getInterval_time())
                .win_rate(req.getWin_rate())
                .profit_rate(req.getProfit_rate())
                .status(0)
                .uid(-1L)
                .used_count(0)
                .opt_admin(StringUtils.isNotBlank(adminInfo.getUsername()) ? adminInfo.getUsername() : "local")
                .build());
        return Result.success();
    }

    @PostMapping("/update")
    @AdminPrivilege(and = {Privilege.激活码管理})
    public Result audit(@RequestBody @Valid RobotCouponUpdateReq req) {
        AdminInfo adminInfo = AdminContent.get();
        RobotCoupon robotCoupon = robotCouponService.getById(req.getId());
        if (Objects.isNull(robotCoupon)) ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        if (robotCoupon.getStatus() != 0) ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
//        if (!Objects.equals(req.getAuto_amount() * req.getAuto_count(), req.getTotal_amount())) {
//            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
//        }

        boolean update = robotCouponService.update(Wrappers.lambdaUpdate(RobotCoupon.class)
                .set(RobotCoupon::getOpt_admin, StringUtils.isNotBlank(adminInfo.getUsername()) ? adminInfo.getUsername() : "local")
                .set(RobotCoupon::getUpdate_time, LocalDateTime.now())
                .set(RobotCoupon::getTotal_amount, req.getTotal_amount())
                .set(RobotCoupon::getAuto_count, req.getAuto_count())
                .set(RobotCoupon::getAuto_amount, req.getAuto_amount())
                .set(RobotCoupon::getInterval_time, req.getInterval_time())
                .set(RobotCoupon::getWin_rate, req.getWin_rate())
                .set(RobotCoupon::getProfit_rate, req.getProfit_rate())
                .eq(RobotCoupon::getId, req.getId())
                .eq(RobotCoupon::getStatus, 0)
        );
        if (!update) ErrorCodeEnum.SYSTEM_ERROR.throwException();
        return Result.success();
    }

    @PostMapping("/delete/{id}")
    public Result delete(@PathVariable Long id) {
        RobotCoupon robotCoupon = robotCouponService.getById(id);
        if (Objects.isNull(robotCoupon)) {
            return Result.success();
        }
        if (ObjectUtil.notEqual(robotCoupon.getStatus(), 0)) {
            throw new ErrCodeException("只能删除未使用的激活码");
        }
        robotCouponService.removeById(id);
        return Result.success();
    }


}
