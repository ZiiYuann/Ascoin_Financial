package com.tianli.fund.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.common.PageQuery;
import com.tianli.fund.entity.FundTransactionRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.fund.query.FundTransactionQuery;
import com.tianli.fund.vo.FundTransactionRecordVO;
import com.tianli.management.vo.FundTransactionAmountVO;

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

    FundTransactionAmountVO getTransactionAmount(FundTransactionQuery query);


}
