package com.tianli.borrow.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.borrow.bo.AdjustPledgeBO;
import com.tianli.borrow.bo.BorrowOrderBO;
import com.tianli.borrow.bo.BorrowOrderRepayBO;
import com.tianli.borrow.entity.BorrowCoinOrder;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.borrow.entity.BorrowInterestRecord;
import com.tianli.borrow.entity.BorrowPledgeRecord;
import com.tianli.borrow.entity.BorrowRepayRecord;
import com.tianli.borrow.enums.BorrowStatisticsChartDay;
import com.tianli.borrow.enums.BorrowStatisticsType;
import com.tianli.borrow.query.BorrowInterestRecordQuery;
import com.tianli.borrow.query.BorrowOrderQuery;
import com.tianli.borrow.query.BorrowPledgeRecordQuery;
import com.tianli.borrow.query.BorrowRepayQuery;
import com.tianli.borrow.vo.*;
import com.tianli.common.PageQuery;
import com.tianli.common.blockchain.CurrencyCoin;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 借币订单 服务类
 * </p>
 *
 * @author xn
 * @since 2022-07-20
 */
public interface IBorrowCoinOrderService extends IService<BorrowCoinOrder> {
    BorrowCoinMainPageVO mainPage();

    IPage<BorrowCoinOrderVO> pageList(PageQuery<BorrowCoinOrder> pageQuery, BorrowOrderQuery query);

    BorrowApplePageVO applyPage(CurrencyCoin coin);

    void borrowCoin(BorrowOrderBO bo);

    BorrowCoinOrderVO info(Long orderId);

    BorrowRecordVO borrowRecord(Long orderId);

    IPage<BorrowPledgeRecordVO> pledgeRecord (PageQuery<BorrowPledgeRecord> pageQuery, BorrowPledgeRecordQuery query);

    IPage<BorrowInterestRecordVO> interestRecord(PageQuery<BorrowInterestRecord> pageQuery, BorrowInterestRecordQuery query);

    AmountVO interestAmount(BorrowInterestRecordQuery query);

    IPage<BorrowRepayRecordVO> repayRecord(PageQuery<BorrowRepayRecord> pageQuery, BorrowRepayQuery query);

    AmountVO repayAmount(BorrowRepayQuery query);

    BorrowLiquidationRecordVO liquidationRecord(Long orderId);

    BorrowRepayPageVO repayPage(Long orderId,CurrencyCoin coin);

    void orderRepay(BorrowOrderRepayBO bo);

    BorrowAdjustPageVO adjustPage(Long orderId,CurrencyCoin coin);

    void adjustPledge(AdjustPledgeBO bo);

    void forcedLiquidation(Long orderId);

    BorrowOrderStatisticsVO statistics(BorrowStatisticsChartDay chartDay,Date startTime, Date endTime);

    List<BorrowOrderStatisticsChartVO> statisticsChart(BorrowStatisticsType statisticsType);

}
