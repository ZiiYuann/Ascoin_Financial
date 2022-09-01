package com.tianli.fund.dao;

import com.tianli.fund.entity.FundTransactionRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.fund.query.FundTransactionQuery;
import com.tianli.management.dto.AmountDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * 基金交易记录 Mapper 接口
 * </p>
 *
 * @author xianeng
 * @since 2022-08-30
 */
@Mapper
public interface FundTransactionRecordMapper extends BaseMapper<FundTransactionRecord> {
    List<AmountDto> getTransactionAmount(FundTransactionQuery query);
}
