package com.tianli.borrow.dao;

import com.tianli.borrow.entity.BorrowCoinOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.borrow.query.BorrowOrderQuery;
import com.tianli.borrow.vo.BorrowOrderAmountVO;
import com.tianli.borrow.vo.BorrowOrderStatisticsChartVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.time.LocalDate;
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

    @Select("SELECT ifnull(SUM(wait_repay_capital),0.0) FROM borrow_coin_order")
    BigDecimal selectTotalBorrowAmount();

    @Select("SELECT ifnull(SUM(wait_repay_capital),0.0) FROM borrow_coin_order where uid = #{uid}")
    BigDecimal selectBorrowAmountByUid(Long uid);

    @Select("SELECT ifnull(SUM(pledge_amount),0.0) FROM borrow_coin_order where uid = #{uid}")
    BigDecimal selectPledgeAmountByUid(Long uid);

    @Select("SELECT ifnull(SUM(wait_repay_capital),0.0) FROM borrow_coin_order")
    BigDecimal selectBorrowCapitalSum();

    @Select("SELECT ifnull(SUM(pledge_amount),0.0) FROM borrow_coin_order")
    BigDecimal selectPledgeAmountSum();

    @Select("SELECT ifnull(SUM(wait_repay_interest),0.0) FROM borrow_coin_order")
    BigDecimal selectWaitRepayInterestSum();

    @Select("SELECT ifnull(count(*),0) FROM borrow_coin_order where `status` = #{status}")
    Integer selectCountByStatus(Integer status);

    @Select("SELECT DATE_FORMAT(borrow_time,'%Y-%m-%d') time,SUM(borrow_capital) amount FROM borrow_coin_order\n" +
            "WHERE borrow_time >= #{startTime}\n" +
            "GROUP BY DATE_FORMAT(borrow_time,'%Y-%m-%d')")
    List<BorrowOrderStatisticsChartVO> selectBorrowCapitalChartByTime(@Param("startTime")Date startTime);

    @Select("select count(*) from borrow_coin_order where borrow_coin = #{coin}")
    Integer selectCountByBorrowCoin(@Param("coin") String coin);

    @Select("select count(*) from borrow_coin_order where pledge_coin = #{coin}")
    Integer selectCountByPledgeCoin(@Param("coin") String coin);

    @Select("select count(*) from borrow_coin_order where status = #{status} and borrow_time >= #{now}")
    Integer selectCountByStatusAndTime(@Param("status") Integer status, @Param("now") LocalDate now);

    void updatePledgeStatusByPledgeRate(@Param("startPledgeRate") BigDecimal startPledgeRate,
                                        @Param("endPledgeRate") BigDecimal endPledgeRate,
                                        @Param("pledgeStatus")Integer pledgeStatus);

    BorrowOrderAmountVO selectAmountSumByQuery(BorrowOrderQuery query);
}
