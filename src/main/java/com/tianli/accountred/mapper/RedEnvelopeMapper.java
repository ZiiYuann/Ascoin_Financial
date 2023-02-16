package com.tianli.accountred.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.accountred.entity.RedEnvelope;
import com.tianli.accountred.enums.RedEnvelopeStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface RedEnvelopeMapper extends BaseMapper<RedEnvelope> {

    @Update("UPDATE  red_envelope SET `receive_num` = receive_num + 1 ,`receive_amount` = `receive_amount` + #{receiveAmount}" +
            " WHERE `id` = #{id} and `receive_num` < `num` AND `status` = 'PROCESS' ")
    int increaseReceive(@Param("id") Long id, @Param("receiveAmount") BigDecimal receiveAmount);

    @Update("UPDATE  red_envelope SET `status` = 'FINISH'  WHERE `id` = #{id} AND  `status` = 'PROCESS' ")
    int finish(@Param("id") Long id, @Param("finishTime") LocalDateTime finishTime);

    @Update("UPDATE  red_envelope SET `status` = 'OVERDUE'  WHERE `id` = #{id} AND  `status` = 'PROCESS' ")
    int overdue(@Param("id") Long id, @Param("finishTime") LocalDateTime finishTime);

    @Update("UPDATE  red_envelope SET `status` = #{status},finish_time = #{finishTime}  WHERE `id` = #{id} AND  `status` = 'PROCESS' " +
            "AND `receive_num` = #{receiveNum} ")
    int statusProcess(@Param("id") Long id, @Param("status") RedEnvelopeStatus status
            , @Param("receiveNum") int receiveNum
            , @Param("finishTime") LocalDateTime finishTime);

    @Update("UPDATE  red_envelope SET `status` = 'PROCESS',txid = #{txid}  WHERE `id` = #{id} AND  `status` = 'WAIT' AND txid IS NULL  ")
    int process(@Param("id") Long id, @Param("txid") String txid);

    @Select("SELECT id FROM red_envelope ")
    List<Long> listIds();
}
