package com.tianli.management.ruleconfig.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigInteger;
import java.util.List;

@Mapper
public interface BetDurationMapper {
    @Select("SELECT `id`, `duration`, `min_bet_amount`, `max_bet_amount`, `extra_percentage` FROM bet_duration_config")
    List<BetDuration> selectAll();

    @Update("UPDATE bet_duration_config SET `duration` = #{duration}, `min_bet_amount` = #{min_bet_amount}, `max_bet_amount` = #{max_bet_amount}, `extra_percentage` = #{extra_percentage} WHERE `id` = #{id}")
    int updateDuration(@Param("id") Integer id,
                       @Param("duration") Double duration,
                       @Param("min_bet_amount") BigInteger min_bet_amount,
                       @Param("max_bet_amount") BigInteger max_bet_amount,
                       @Param("extra_percentage") Double extra_percentage);


}
