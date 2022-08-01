package com.tianli.borrow.dao;

import com.tianli.borrow.entity.BorrowOrderNumDaily;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.borrow.vo.BorrowOrderStatisticsChartVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 计息中订单每日统计 Mapper 接口
 * </p>
 *
 * @author xianeng
 * @since 2022-08-01
 */
@Mapper
public interface BorrowOrderNumDailyMapper extends BaseMapper<BorrowOrderNumDaily> {

    Integer selectCountByDate(@Param("startDate")LocalDate startDate,@Param("endDate")LocalDate endDate);

    @Select("SELECT DATE_FORMAT(statistical_date,'%Y-%m-%d') time,order_num amount FROM borrow_order_num_daily where statistical_date >= #{startTime}")
    List<BorrowOrderStatisticsChartVO> selectTotalChartByTime(@Param("startTime") LocalDate startTime);

}
