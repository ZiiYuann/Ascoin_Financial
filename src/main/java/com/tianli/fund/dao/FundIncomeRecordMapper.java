package com.tianli.fund.dao;

import com.tianli.fund.entity.FundIncomeRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.management.dto.AmountDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 基金收益记录 Mapper 接口
 * </p>
 *
 * @author xianeng
 * @since 2022-08-30
 */
@Mapper
public interface FundIncomeRecordMapper extends BaseMapper<FundIncomeRecord> {
    @Select("select sum(interest_amount) from fund_income_record where uid = #{uid} and status = #{status}")
    List<AmountDto> amountSumByUid(@Param("uid")Long uid, @Param("status")Integer status);

}
