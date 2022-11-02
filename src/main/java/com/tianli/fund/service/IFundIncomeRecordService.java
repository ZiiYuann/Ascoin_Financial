package com.tianli.fund.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.agent.management.bo.FundAuditBO;
import com.tianli.agent.management.vo.FundReviewVO;
import com.tianli.common.PageQuery;
import com.tianli.fund.dto.FundIncomeAmountDTO;
import com.tianli.fund.entity.FundIncomeRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.fund.query.FundIncomeQuery;
import com.tianli.fund.vo.FundIncomeRecordVO;
import com.tianli.management.dto.AmountDto;
import com.tianli.management.vo.FundIncomeAmountVO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 基金收益记录 服务类
 * </p>
 *
 * @author xianeng
 * @since 2022-08-30
 */
public interface IFundIncomeRecordService extends IService<FundIncomeRecord> {

    BigDecimal amountDollar(Long uid, Long agentId, Integer status);

    BigDecimal amountDollar(Long uid, Integer status, LocalDateTime startTime, LocalDateTime endTime);

    BigDecimal amountDollarYesterday(Long fundId);

    BigDecimal amountDollar(Long uid, Long agentId, List<Integer> status);

    BigDecimal amountDollar(FundIncomeQuery query);

    IPage<FundIncomeRecordVO> getPage(PageQuery<FundIncomeRecord> page, FundIncomeQuery query);

    IPage<FundIncomeRecordVO> getSummaryPage(PageQuery<FundIncomeRecord> page, FundIncomeQuery query);

    List<FundIncomeAmountDTO> getAmount(FundIncomeQuery query);

    FundIncomeAmountVO getIncomeAmount(FundIncomeQuery query);

    Integer getWaitPayCount(Long productId);

    void incomeAudit(FundAuditBO bo);

    List<FundReviewVO> getIncomeAuditRecord(Long id);

    void rollback(Long id);

    /**
     * 基金昨日收益
     */
    BigDecimal yesterdayIncomeAmount(Long id);
}
