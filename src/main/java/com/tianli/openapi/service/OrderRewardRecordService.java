package com.tianli.openapi.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.openapi.entity.OrderRewardRecord;
import com.tianli.openapi.mapper.OrderRewardRecordMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-10-28
 **/
@Service
public class OrderRewardRecordService extends ServiceImpl<OrderRewardRecordMapper, OrderRewardRecord> {

    /**
     * 获取一段时间内的奖励
     *
     * @param uid 用户id
     * @return 数量
     */
    public int recordCountThisHour(Long uid, LocalDateTime begin) {
        LocalDateTime end = begin.plusHours(1).plusSeconds(-1);

        LambdaQueryWrapper<OrderRewardRecord> query = new LambdaQueryWrapper<OrderRewardRecord>()
                .eq(OrderRewardRecord::getUid, uid)
                .between(OrderRewardRecord::getGiveTime, begin, end);
        return this.count(query);
    }

}
