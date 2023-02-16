package com.tianli.charge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.charge.entity.OrderChargeInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-15
 **/
@Mapper
public interface OrderChargeInfoMapper extends BaseMapper<OrderChargeInfo> {

    @Update("UPDATE  `order_charge_info` SET `txid` = #{txid} WHERE `id` =#{id} AND `txid` IS NULL")
    int updateTxid(@Param("id") Long id, @Param("txid") String txid);
}