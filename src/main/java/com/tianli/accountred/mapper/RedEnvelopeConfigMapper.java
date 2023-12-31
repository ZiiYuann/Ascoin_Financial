package com.tianli.accountred.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.accountred.entity.RedEnvelopeConfig;
import com.tianli.accountred.enums.RedEnvelopeChannel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface RedEnvelopeConfigMapper extends BaseMapper<RedEnvelopeConfig> {


    @Select(" SELECT  * FROM `red_envelope_config`  WHERE coin =#{coin} and channel = #{channel}")
    RedEnvelopeConfig selectByName(@Param("coin") String coin, @Param("channel") RedEnvelopeChannel channel);
}
