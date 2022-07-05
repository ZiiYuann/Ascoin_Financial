package com.tianli.management.spot.dao;

import com.tianli.management.spot.vo.SGRechargeByTypeVo;
import com.tianli.management.spot.vo.SGRechargeListVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author lzy
 * @date 2022/4/24 15:46
 */
@Mapper
public interface SGRechargeMapper {
    Long selectCount(@Param("username") String username,
                     @Param("token") String token,
                     @Param("startTime") String startTime,
                     @Param("endTime") String endTime,
                     @Param("txid") String txid,
                     @Param("salesman_id") Long salesman_id);

    List<SGRechargeListVo> selectPage(@Param("username") String username,
                                      @Param("token") String token,
                                      @Param("startTime") String startTime,
                                      @Param("endTime") String endTime,
                                      @Param("txid") String txid,
                                      @Param("salesman_id") Long salesman_id,
                                      @Param("page") Integer page,
                                      @Param("size") Integer size);

    BigDecimal selectSumAmount(@Param("username") String username,
                               @Param("token") String token,
                               @Param("startTime") String startTime,
                               @Param("endTime") String endTime,
                               @Param("txid") String txid,
                               @Param("salesman_id") Long salesman_id);

    List<SGRechargeByTypeVo> listSumAmount(@Param("token") String token,
                                           @Param("chain") String chain,
                                           @Param("currencyType") String currencyType,
                                           @Param("page") Integer page,
                                           @Param("size") Integer size);

    Long listSumAmountCount(@Param("token") String token,
                            @Param("chain") String chain,
                            @Param("currencyType") String currencyType);
}
