package com.tianli.borrow.service;

import cn.hutool.core.date.DateTime;
import com.tianli.borrow.entity.BorrowOrderNumDaily;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.borrow.vo.BorrowOrderStatisticsChartVO;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 计息中订单每日统计 服务类
 * </p>
 *
 * @author xianeng
 * @since 2022-08-01
 */
public interface IBorrowOrderNumDailyService extends IService<BorrowOrderNumDaily> {

    void statisticalOrderNum();

    void increaseNum();

    void reduceNum();

    BorrowOrderNumDaily getByDate(LocalDate localDate);

    Integer getCount(Date startDate,Date endDate);

    List<BorrowOrderStatisticsChartVO> selectTotalChart(DateTime beginOfDay);
}


