package com.tianli.financial.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
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

    @Update("")
    int reduce(Long recordId, BigDecimal amount);

    @Select("SELECT SUM(hold_amount) FROM financial_record")
    BigDecimal selectTotalHoldAmount();

    @Select("SELECT SUM(hold_amount) FROM financial_record where uid = #{uid}")
    BigDecimal selectHoldAmountByUid(Long uid);
    @Select("SELECT SUM(hold_amount - lock_amount) FROM financial_record where uid = #{uid}")
    BigDecimal selectAvailableAmountByUid(Long uid);
}
