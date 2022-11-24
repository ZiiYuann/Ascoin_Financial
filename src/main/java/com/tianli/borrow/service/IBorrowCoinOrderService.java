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
import com.tianli.borrow.enums.BorrowStatisticsType;
import com.tianli.borrow.query.BorrowInterestRecordQuery;
import com.tianli.borrow.query.BorrowOrderQuery;
import com.tianli.borrow.query.BorrowPledgeRecordQuery;
import com.tianli.borrow.query.BorrowRepayQuery;
import com.tianli.borrow.vo.*;
import com.tianli.common.PageQuery;

import java.math.BigDecimal;
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

    BorrowApplePageVO applyPage(String coin);

    void borrowCoin(BorrowOrderBO bo);

    BorrowCoinOrderVO info(Long orderId);

    BorrowRecordVO borrowRecord(Long orderId);

    IPage<BorrowPledgeRecordVO> pledgeRecord (PageQuery<BorrowPledgeRecord> pageQuery, BorrowPledgeRecordQuery query);

    IPage<BorrowInterestRecordVO> interestRecord(PageQuery<BorrowInterestRecord> pageQuery, BorrowInterestRecordQuery query);

    AmountVO interestAmount(BorrowInterestRecordQuery query);

    BorrowOrderAmountVO cumulativeAmount(BorrowOrderQuery query);

    IPage<BorrowRepayRecordVO> repayRecord(PageQuery<BorrowRepayRecord> pageQuery, BorrowRepayQuery query);

    AmountVO repayAmount(BorrowRepayQuery query);

    BorrowLiquidationRecordVO liquidationRecord(Long orderId);

    BorrowRepayPageVO repayPage(Long orderId, BigDecimal repayAmount, String coin);

    void orderRepay(BorrowOrderRepayBO bo);

    BorrowAdjustPageVO adjustPage(Long orderId,Integer pledgeType,BigDecimal adjustAmount,String coin);

    void adjustPledge(AdjustPledgeBO bo);

    void forcedLiquidation(Long orderId);

    BorrowOrderStatisticsVO statistics();

    List<BorrowOrderStatisticsChartVO> statisticsChart(BorrowStatisticsType statisticsType);

    Integer selectCountByBorrowCoin(String coin);

    void updatePledgeStatusByPledgeRateRange(BigDecimal startPledgeRate, BigDecimal endPledgeRate, Integer pledgeStatus);

    Integer selectCountByPledgeCoin(String coin);

    Integer selectCountByStatus(Integer interestAccrual);
}
