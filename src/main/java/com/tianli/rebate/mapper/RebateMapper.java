package com.tianli.rebate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * <p>
 * 返佣表 Mapper 接口
 * </p>
 *
 * @author hd
 * @since 2020-12-09
 */
@Mapper
public interface RebateMapper extends BaseMapper<Rebate> {

    @Select("select ifnull(sum(`rebate_amount`), 0) from `rebate` where `rebate_uid` = #{uid} and `create_time` between #{startTime} and #{endTime}")
    BigInteger selectTotalRebateAmountWithInterval(@Param("uid") long uid, @Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    @Select("select ifnull(sum(`rebate_amount`), 0) from `rebate` where `rebate_uid` = #{uid} and `token` = 'BF' ")
    BigInteger selectTotalRebateBFAmount(@Param("uid") long uid);

    @Select("select ifnull(sum(`rebate_amount`), 0) from `rebate` where `rebate_uid` = #{uid} and `token` = 'usdt' ")
    BigInteger selectTotalRebateUsdtAmount(@Param("uid") long uid);
}
