package com.tianli.charge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.charge.entity.OrderChargeInfo;
import com.tianli.charge.mapper.OrderChargeInfoMapper;
import com.tianli.exception.ErrorCodeEnum;
import org.springframework.stereotype.Service;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-18
 **/
@Service
public class OrderChargeInfoService extends ServiceImpl<OrderChargeInfoMapper, OrderChargeInfo> {

    public OrderChargeInfo getByTxid(String txid) {
        LambdaQueryWrapper<OrderChargeInfo> queryWrapper =
                new LambdaQueryWrapper<OrderChargeInfo>().eq(OrderChargeInfo::getTxid, txid);
        return baseMapper.selectOne(queryWrapper);
    }

    public OrderChargeInfo getByTxidExcludeUid(Long uid, String txid) {
        LambdaQueryWrapper<OrderChargeInfo> queryWrapper =
                new LambdaQueryWrapper<OrderChargeInfo>()
                        .ne(OrderChargeInfo::getUid, uid)
                        .eq(OrderChargeInfo::getTxid, txid);
        return baseMapper.selectOne(queryWrapper);
    }

    /**
     * 通过交易hash查询 区块链订单信息
     */
    public OrderChargeInfo getOrderChargeByTxid(String txid) {
        return this.baseMapper.selectOne(new LambdaQueryWrapper<OrderChargeInfo>()
                .eq(OrderChargeInfo::getTxid, txid));
    }

    /**
     * 通过交易hash查询 区块链订单信息
     */
    public OrderChargeInfo getOrderChargeByTxid(Long uid, String txid) {
        return this.baseMapper.selectOne(new LambdaQueryWrapper<OrderChargeInfo>()
                .eq(OrderChargeInfo::getUid, uid)
                .eq(OrderChargeInfo::getTxid, txid));
    }

    public void updateTxid(Long id, String txid) {
        int i =this.baseMapper.updateTxid(id,txid);
        if (i != 1) {
            throw ErrorCodeEnum.TRADE_FAIL.generalException();
        }
    }
}
