package com.tianli.borrow.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.borrow.entity.BorrowPledgeRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.borrow.query.BorrowPledgeRecordQuery;
import com.tianli.borrow.vo.BorrowOrderStatisticsChartVO;
import com.tianli.borrow.vo.BorrowPledgeRecordVO;
import com.tianli.common.PageQuery;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 借币质押记录 服务类
 * </p>
 *
 * @author xn
 * @since 2022-07-20
 */
public interface IBorrowPledgeRecordService extends IService<BorrowPledgeRecord> {
    BigDecimal selectAmountSumByTime(Date startTime, Date endTime);

    List<BorrowOrderStatisticsChartVO> selectAmountChartByTime(Date startTime);

    IPage<BorrowPledgeRecordVO> pledgeRecordPage(PageQuery<BorrowPledgeRecord> pageQuery, BorrowPledgeRecordQuery query);
}
