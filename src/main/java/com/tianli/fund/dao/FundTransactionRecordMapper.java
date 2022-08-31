package com.tianli.fund.dao;

import com.tianli.fund.entity.FundTransactionRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.fund.enums.FundTransactionType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

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

    @Select("select sum(transaction_amount) from fund_transaction_record where fund_id = #{fundId} and type = #{type} and status = #{status}")
    BigDecimal TransactionAmountSum(@Param("fundId")Long fundId, @Param("type")FundTransactionType type,@Param("status") Integer status);

}
