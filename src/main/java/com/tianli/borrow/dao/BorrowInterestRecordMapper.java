package com.tianli.borrow.dao;

import com.tianli.borrow.entity.BorrowInterestRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.borrow.query.BorrowInterestRecordQuery;
import com.tianli.borrow.vo.BorrowOrderStatisticsChartVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 借币利息记录 Mapper 接口
 * </p>
 *
 * @author xn
 * @since 2022-07-20
 */
@Mapper
public interface BorrowInterestRecordMapper extends BaseMapper<BorrowInterestRecord> {
    BigDecimal selectInterestSumByQuery(BorrowInterestRecordQuery query);

    @Select("SELECT DATE_FORMAT(interest_accrual_time,'%Y-%m-%d') time,SUM(interest_accrual)amount FROM borrow_interest_record\n" +
            "WHERE interest_accrual_time >= #{startTime}\n" +
            "GROUP BY DATE_FORMAT(interest_accrual_time,'%Y-%m-%d');")
    List<BorrowOrderStatisticsChartVO> selectInterestChartByTime(@Param("startTime")Date startTime);
}
