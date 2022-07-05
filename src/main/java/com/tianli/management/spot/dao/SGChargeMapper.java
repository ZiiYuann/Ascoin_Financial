package com.tianli.management.spot.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.management.spot.entity.SGCharge;
import com.tianli.management.spot.vo.SGWithdrawListVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author lzy
 * @date 2022/4/15 2:56 下午
 */
@Mapper
public interface SGChargeMapper extends BaseMapper<SGCharge> {

    Integer withdrawCount(@Param("username") String username,
                          @Param("status") String status,
                          @Param("startTime") String startTime,
                          @Param("endTime") String endTime);

    List<SGWithdrawListVo> withdrawPage(@Param("username") String username,
                                        @Param("status") String status,
                                        @Param("startTime") String startTime,
                                        @Param("endTime") String endTime,
                                        @Param("page") Integer page,
                                        @Param("size") Integer size);

    BigDecimal sumAmount(@Param("username") String username,
                         @Param("status") String status,
                         @Param("startTime") String startTime,
                         @Param("endTime") String endTime);
}
