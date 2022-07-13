package com.tianli.financial.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.financial.entity.AccrueIncomeLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AccrueIncomeLogMapper extends BaseMapper<AccrueIncomeLog> {

    @Select("select * from accrue_income where uid=#{uid}")
    List<AccrueIncomeLog> selectListByUid(@Param("uid") Long uid);


    @Select("select * from accrue_income where record_id=#{recordId}")
    List<AccrueIncomeLog> selectListByRecordId(@Param("recordId") Long recordId);
}
