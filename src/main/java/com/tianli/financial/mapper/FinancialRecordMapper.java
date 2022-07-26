package com.tianli.financial.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.financial.entity.FinancialRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Mapper
public interface FinancialRecordMapper extends BaseMapper<FinancialRecord> {

    int reduce(@Param("recordId") Long recordId, @Param("amount") BigDecimal amount,@Param("now") LocalDateTime now);

    @Select("SELECT ifnull(SUM(hold_amount),0.0) FROM financial_record")
    BigDecimal selectTotalHoldAmount();

    @Select("SELECT ifnull(SUM(hold_amount),0.0) FROM financial_record where uid = #{uid}")
    BigDecimal selectHoldAmountByUid(Long uid);
    @Select("SELECT ifnull(SUM(hold_amount - pledge_amount),0.0) FROM financial_record where uid = #{uid} and coin = #{coin}")
    BigDecimal selectAvailableAmountByUid(Long uid, CurrencyCoin coin);
}
