package com.tianli.charge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.charge.entity.OrderChargeInfo;
import com.tianli.charge.mapper.OrderChargeInfoMapper;
import org.springframework.stereotype.Service;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-18
 **/
@Service
public class OrderChargeInfoService extends ServiceImpl<OrderChargeInfoMapper, OrderChargeInfo> {

    public OrderChargeInfo getByTxid(String txid){
        LambdaQueryWrapper<OrderChargeInfo> queryWrapper =
                new LambdaQueryWrapper<OrderChargeInfo>().eq(OrderChargeInfo::getTxid, txid);
        return baseMapper.selectOne(queryWrapper);
    }
}
