package com.tianli.fund.service;

import com.tianli.fund.entity.FundTransactionRecord;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;

/**
 * <p>
 * 基金交易记录 服务类
 * </p>
 *
 * @author xianeng
 * @since 2022-08-30
 */
public interface IFundTransactionRecordService extends IService<FundTransactionRecord> {

    BigDecimal getWaitRedemptionAmount(Long fundId);

}
