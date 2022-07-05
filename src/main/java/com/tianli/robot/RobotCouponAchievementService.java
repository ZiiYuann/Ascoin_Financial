package com.tianli.robot;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.robot.mapper.RobotCouponAchievement;
import com.tianli.robot.mapper.RobotCouponAchievementMapper;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service
public class RobotCouponAchievementService extends ServiceImpl<RobotCouponAchievementMapper, RobotCouponAchievement> {

    public Map<String, Double> getTotalAmount(long uid) {
        return baseMapper.selectSummaryByUid(uid);
    }
}
