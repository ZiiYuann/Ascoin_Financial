package com.tianli.fund.service;

import com.tianli.fund.entity.FundIncomeRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.management.dto.AmountDto;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

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

    @Select("select sum(interest_amount) from fund_income_record where uid = #{uid} and status = #{status}")
    List<AmountDto> amountSumByUid(@Param("uid")Long uid, @Param("status")Integer status);

}
