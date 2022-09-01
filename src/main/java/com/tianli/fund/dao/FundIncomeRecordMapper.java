package com.tianli.fund.dao;

import com.tianli.fund.entity.FundIncomeRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.fund.query.FundIncomeQuery;
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

    List<AmountDto> selectAmount(FundIncomeQuery query);

}
