package com.tianli.borrow.dao;

import com.tianli.borrow.entity.BorrowPledgeRecord;
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
 * 借币质押记录 Mapper 接口
 * </p>
 *
 * @author xn
 * @since 2022-07-20
 */
@Mapper
public interface BorrowPledgeRecordMapper extends BaseMapper<BorrowPledgeRecord> {

    BigDecimal selectAmountSumByTime(@Param("startTime") Date startTime, @Param("endTime") Date endTime);

    @Select("SELECT DATE_FORMAT(pledge_time,'%Y-%m-%d') time,SUM(amount)amount FROM borrow_pledge_record\n" +
            "WHERE pledge_time >= #{startTime}\n" +
            "GROUP BY DATE_FORMAT(pledge_time,'%Y-%m-%d')")
    List<BorrowOrderStatisticsChartVO> selectAmountChartByTime(@Param("startTime")Date startTime);


}
