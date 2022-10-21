package com.tianli.accountred.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.accountred.entity.RedEnvelopeSpilt;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

@Mapper
public interface RedEnvelopeSpiltMapper extends BaseMapper<RedEnvelopeSpilt> {


    /**
     * 更新信息
     */
    @Update("UPDATE  red_envelope_spilt set receive = true , receive_time = #{receiveTime} WHERE rid = #{rid} and  id =#{id} " +
            "and receive = false ")
    int receive(@Param("rid") Long rid, @Param("id") String id, @Param("receiveTime") LocalDateTime receiveTime);
}
