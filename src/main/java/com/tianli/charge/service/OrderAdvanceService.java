package com.tianli.charge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.chain.dto.TRONTokenReq;
import com.tianli.charge.entity.OrderAdvance;
import com.tianli.charge.mapper.OrderAdvanceMapper;
import com.tianli.charge.query.GenerateOrderAdvanceQuery;
import com.tianli.common.CommonFunction;
import com.tianli.common.annotation.NoRepeatCommit;
import com.tianli.currency.enums.TokenAdapter;
import com.tianli.financial.entity.FinancialProduct;
import com.tianli.financial.service.FinancialProductService;
import com.tianli.sso.init.RequestInitService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * @author chenb
 * @apiNote
 * @since 2022-08-31
 **/
@Service
public class OrderAdvanceService extends ServiceImpl<OrderAdvanceMapper,OrderAdvance> {

    @Resource
    private RequestInitService requestInitService;
    @Resource
    private FinancialProductService financialProductService;

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

    /**
     * 生成预订单
     */
    @Transactional
    public void updateOrderAdvance(GenerateOrderAdvanceQuery query) {
        OrderAdvance orderAdvance = baseMapper.selectById(query.getId());
        orderAdvance.setTxid(query.getTxid());
        baseMapper.updateById(orderAdvance);
    }

    /**
     * 处理充值事件
     */
    public void handlerRechargeEvent(Long uid, TRONTokenReq req, BigDecimal finalAmount, TokenAdapter tokenAdapter) {
        var query = new LambdaQueryWrapper<OrderAdvance>()
                .eq(OrderAdvance::getUid, uid)
                .eq(OrderAdvance :: getTxid,req.getHash());
        OrderAdvance orderAdvance = baseMapper.selectOne(query);
        if(Objects.isNull(orderAdvance)){
            return;
        }

        Long productId = orderAdvance.getProductId();
        FinancialProduct product = financialProductService.getById(productId);
        if(Objects.isNull(product)){
            log.error("预订单产品");
            return;
        }


    }
}
