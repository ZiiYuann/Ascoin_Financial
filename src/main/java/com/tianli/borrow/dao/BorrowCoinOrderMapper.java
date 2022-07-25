package com.tianli.borrow.dao;

import com.tianli.borrow.entity.BorrowCoinOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.borrow.vo.BorrowOrderStatisticsChartVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 借币订单 Mapper 接口
 * </p>
 *
 * @author xn
 * @since 2022-07-20
 */
@Mapper
public interface BorrowCoinOrderMapper extends BaseMapper<BorrowCoinOrder> {

    @Select("SELECT SUM(borrow_capital) FROM borrow_coin_order")
    BigDecimal selectTotalBorrowAmount();

    @Select("SELECT SUM(borrow_capital) FROM borrow_coin_order where uid = #{uid}")
    BigDecimal selectBorrowAmountByUid(Long uid);

    @Select("SELECT SUM(pledge_amount) FROM borrow_coin_order where uid = #{uid}")
    BigDecimal selectPledgeAmountByUid(Long uid);

    @Select("SELECT SUM(borrow_capital) FROM borrow_coin_order where borrow_time >= #{startTime} and borrow_time<= #{endTime}")
    BigDecimal selectBorrowCapitalSumByBorrowTime(@Param("startTime")Date startTime,@Param("endTime") Date endTime);

    @Select("SELECT count(*) FROM borrow_coin_order where status = #{status} and borrow_time >= #{startTime} and borrow_time<= #{endTime}")
    Integer selectCountByBorrowTime(@Param("status") Integer status , @Param("startTime")Date startTime,@Param("endTime") Date endTime);

    @Select("SELECT DATE_FORMAT(borrow_time,'%Y-%m-%d') time,SUM(borrow_capital) amount FROM borrow_coin_order\n" +
            "WHERE borrow_time >= #{startTime}\n" +
            "GROUP BY DATE_FORMAT(borrow_time,'%Y-%m-%d')")
    List<BorrowOrderStatisticsChartVO> selectBorrowCapitalChartByTime(@Param("startTime")Date startTime);


    @Select("SELECT DATE_FORMAT(borrow_time,'%Y-%m-%d') time,count(*) amount FROM borrow_coin_order\n" +
            "WHERE borrow_time >= #{startTime} and status = #{status} \n" +
            "GROUP BY DATE_FORMAT(borrow_time,'%Y-%m-%d')")
    List<BorrowOrderStatisticsChartVO> selectTotalChartByTime(@Param("status") Integer status ,@Param("startTime")Date startTime);
}
