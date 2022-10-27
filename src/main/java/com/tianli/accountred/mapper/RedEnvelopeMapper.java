package com.tianli.accountred.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.accountred.entity.RedEnvelope;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface RedEnvelopeMapper extends BaseMapper<RedEnvelope> {

    @Update("UPDATE  red_envelope SET `receive_num` = receive_num + 1 WHERE `id` = #{id} and `receive_num` < `num`  and `status` = 'PROCESS' ")
    int increaseReceiveNum(@Param("id") Long id);

    @Update("UPDATE  red_envelope SET `status` = 'FINISH'  WHERE `id` = #{id} and  `status` = 'PROCESS' ")
    int finish(@Param("id") Long id);
}
