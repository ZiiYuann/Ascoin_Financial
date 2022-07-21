package com.tianli.financial.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.financial.entity.FinancialRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

@Mapper
public interface FinancialRecordMapper extends BaseMapper<FinancialRecord> {


    @Update("")
    int reduce(Long recordId, BigDecimal amount);
}
