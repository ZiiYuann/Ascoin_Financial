package com.tianli.robot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface RobotCouponAchievementMapper extends BaseMapper<RobotCouponAchievement> {

    @Select("SELECT  ifnull(SUM(`total_amount`), 0) as betAmount, ifnull(SUM(`profit_amount`), 0) as profitAmount FROM `robot_coupon_achievement` WHERE `uid` = #{uid}")
    Map<String, Double> selectSummaryByUid(@Param("uid") long uid);
}
