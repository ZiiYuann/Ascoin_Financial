package com.tianli.robot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

@Mapper
public interface RobotCouponMapper extends BaseMapper<RobotCoupon> {

    @Update("UPDATE `robot_coupon` SET `used_count` = `used_count` + 1, used_time = CASE WHEN `used_time` is null THEN #{time} ELSE `used_time` END WHERE `id` = #{id}")
    long incrementUsedCount(@Param("id") long couponId,@Param("time") LocalDateTime time);
}
