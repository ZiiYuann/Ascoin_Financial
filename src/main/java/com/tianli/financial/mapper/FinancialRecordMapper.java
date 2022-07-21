package com.tianli.financial.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.financial.entity.FinancialRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface FinancialRecordMapper extends BaseMapper<FinancialRecord> {

    @Select("SELECT SUM(hold_amount) FROM financial_record")
    BigDecimal selectTotalHoldAmount();

    @Select("SELECT SUM(hold_amount) FROM financial_record where uid = #{uid}}")
    BigDecimal selectHoldAmountByUid(Long uid);

    @Select("ELECT SUM(hold_amount - lock_amount) FROM financial_record where uid = #{uid}}")
    BigDecimal selectAvailableAmountByUid(Long uid);

}
