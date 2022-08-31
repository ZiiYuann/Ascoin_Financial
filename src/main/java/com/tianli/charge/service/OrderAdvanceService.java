package com.tianli.charge.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.charge.entity.OrderAdvance;
import com.tianli.charge.mapper.OrderAdvanceMapper;
import com.tianli.charge.query.GenerateOrderAdvanceQuery;
import com.tianli.common.CommonFunction;
import com.tianli.common.annotation.NoRepeatCommit;
import com.tianli.sso.init.RequestInitService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-08-31
 **/
@Service
public class OrderAdvanceService extends ServiceImpl<OrderAdvanceMapper,OrderAdvance> {

    @Resource
    private RequestInitService requestInitService;

    /**
     * 生成预订单
     */
    @Transactional
    @NoRepeatCommit(autoUnlock = false)
    public Long generateOrderAdvance(GenerateOrderAdvanceQuery query) {
        OrderAdvance orderAdvance =  OrderAdvance.builder()
                .id(CommonFunction.generalId())
                .amount(query.getAmount())
                .uid(requestInitService.uid())
                .createTime(LocalDateTime.now())
                .productId(query.getProductId())
                .build();
        baseMapper.insert(orderAdvance);
        return orderAdvance.getId();
    }
}
