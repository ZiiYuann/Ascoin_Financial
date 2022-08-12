package com.tianli.borrow.service;

import cn.hutool.core.date.DateTime;
import com.tianli.borrow.entity.BorrowInterestRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.borrow.query.BorrowInterestRecordQuery;
import com.tianli.borrow.vo.BorrowOrderStatisticsChartVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 借币利息记录 服务类
 * </p>
 *
 * @author xn
 * @since 2022-07-20
 */
public interface IBorrowInterestRecordService extends IService<BorrowInterestRecord> {

    BigDecimal selectInterestSumByQuery(BorrowInterestRecordQuery query);

    List<BorrowOrderStatisticsChartVO> selectInterestChartByTime(DateTime beginOfDay);
}
