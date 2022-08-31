package com.tianli.fund.service;

import com.tianli.fund.entity.FundIncomeRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.management.dto.AmountDto;
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

    List<AmountDto> amountSumByUid(Long uid, Integer status);

}
