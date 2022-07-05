package com.tianli.robot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface RobotOrderMapper extends BaseMapper<RobotOrder> {

    @Update("UPDATE `robot_order` SET `count` = `count` - 1 WHERE `id` = #{uid} and `count` > 0")
    long decrementCount(@Param("uid") long uid);

}
