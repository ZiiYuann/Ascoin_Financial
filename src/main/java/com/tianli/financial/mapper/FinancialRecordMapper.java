package com.tianli.financial.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.financial.entity.FinancialRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Mapper
public interface FinancialRecordMapper extends BaseMapper<FinancialRecord> {

    int reduce(@Param("recordId") Long recordId, @Param("amount") BigDecimal amount,@Param("now") LocalDateTime now);
}
