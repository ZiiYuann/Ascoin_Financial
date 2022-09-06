package com.tianli.fund.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.common.PageQuery;
import com.tianli.fund.dto.FundIncomeAmountDTO;
import com.tianli.fund.entity.FundIncomeRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.fund.query.FundIncomeQuery;
import com.tianli.fund.vo.FundIncomeRecordVO;
import com.tianli.management.dto.AmountDto;
import com.tianli.management.vo.FundIncomeAmountVO;

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

    List<AmountDto> getAmountByUidAndStatus(Long uid, Integer status);

    IPage<FundIncomeRecordVO> getPage(PageQuery<FundIncomeRecord> page , FundIncomeQuery query);

    List<FundIncomeAmountDTO> getAmount(FundIncomeQuery query);

    FundIncomeAmountVO getIncomeAmount(FundIncomeQuery query);

    boolean existWaitInterest(Long uid);
}
