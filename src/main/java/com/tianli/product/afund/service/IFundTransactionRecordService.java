package com.tianli.product.afund.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.agent.management.bo.FundAuditBO;
import com.tianli.agent.management.vo.FundReviewVO;
import com.tianli.common.PageQuery;
import com.tianli.product.afund.dto.FundTransactionAmountDTO;
import com.tianli.product.afund.entity.FundTransactionRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.product.afund.query.FundTransactionQuery;
import com.tianli.product.afund.vo.FundTransactionRecordVO;
import com.tianli.management.vo.FundTransactionAmountVO;

import java.util.List;

/**
 * <p>
 * 基金交易记录 服务类
 * </p>
 *
 * @author xianeng
 * @since 2022-08-30
 */
public interface IFundTransactionRecordService extends IService<FundTransactionRecord> {

    IPage<FundTransactionRecordVO> getTransactionPage(PageQuery<FundTransactionRecord> page, FundTransactionQuery query);

    List<FundTransactionAmountDTO> getFundTransactionAmountDTO(FundTransactionQuery query);

    FundTransactionAmountVO getTransactionAmount(FundTransactionQuery query);

    void redemptionAudit(FundAuditBO bo);

    List<FundReviewVO> getRedemptionAuditRecord(Long id);

    Integer getWaitRedemptionCount(Long productId);



}
