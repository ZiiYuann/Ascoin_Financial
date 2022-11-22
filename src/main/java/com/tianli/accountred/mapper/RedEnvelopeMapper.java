package com.tianli.accountred.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.accountred.entity.RedEnvelope;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface RedEnvelopeMapper extends BaseMapper<RedEnvelope> {

    @Update("UPDATE  red_envelope SET `receive_num` = receive_num + 1 WHERE `id` = #{id} and `receive_num` < `num`  AND `status` = 'PROCESS' ")
    int increaseReceiveNum(@Param("id") Long id);

    @Update("UPDATE  red_envelope SET `status` = 'FINISH'  WHERE `id` = #{id} AND  `status` = 'PROCESS' ")
    int finish(@Param("id") Long id);

    @Update("UPDATE  red_envelope SET `status` = 'OVERDUE'  WHERE `id` = #{id} AND  `status` = 'PROCESS' ")
    int overdue(@Param("id") Long id);

    @Update("UPDATE  red_envelope SET `status` = 'PROCESS',txid = #{txid}  WHERE `id` = #{id} AND  `status` = 'WAIT' AND txid IS NULL  ")
    int process(@Param("id") Long id, @Param("txid") String txid);

    @Select("SELECT id FROM red_envelope ")
    List<Long> listIds();
}
