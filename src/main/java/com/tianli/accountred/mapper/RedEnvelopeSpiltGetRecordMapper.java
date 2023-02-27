package com.tianli.accountred.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.accountred.entity.RedEnvelopeSpiltGetRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface RedEnvelopeSpiltGetRecordMapper extends BaseMapper<RedEnvelopeSpiltGetRecord> {

    @Select("SELECT sum(amount) FROM red_envelope_spilt_get_record WHERE rid = #{rid}")
    BigDecimal receivedAmount(@Param("rid") Long rid);
}
