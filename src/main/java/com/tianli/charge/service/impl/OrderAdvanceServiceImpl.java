package com.tianli.charge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.chain.dto.TRONTokenReq;
import com.tianli.chain.entity.Coin;
import com.tianli.charge.entity.OrderAdvance;
import com.tianli.charge.enums.AdvanceType;
import com.tianli.charge.mapper.OrderAdvanceMapper;
import com.tianli.charge.query.GenerateOrderAdvanceQuery;
import com.tianli.charge.service.OrderAdvanceProcessor;
import com.tianli.charge.service.OrderAdvanceService;
import com.tianli.charge.vo.OrderBaseVO;
import com.tianli.common.CommonFunction;
import com.tianli.common.annotation.NoRepeatCommit;
import com.tianli.common.webhook.WebHookService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.sso.init.RequestInitService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
public class OrderAdvanceServiceImpl extends ServiceImpl<OrderAdvanceMapper, OrderAdvance>
        implements OrderAdvanceService {

    @Resource
    private RequestInitService requestInitService;
    @Resource
    private WebHookService webHookService;
    @Resource
    private List<OrderAdvanceProcessor<?>> orderAdvanceProcessors;

    private OrderAdvanceProcessor<?> getOrderAdvanceProcessor(AdvanceType advanceType) {
        for (OrderAdvanceProcessor<?> processor : orderAdvanceProcessors) {
            if (advanceType.equals(processor.getType())) {
                return processor;
            }
        }
        throw ErrorCodeEnum.SYSTEM_ERROR.generalException();
    }

    /**
     * 生成预订单
     */
    @Override
    @Transactional
    @NoRepeatCommit(autoUnlock = false)
    public Long generateOrderAdvance(GenerateOrderAdvanceQuery query) {

        Long uid = requestInitService.uid();

        var orderAdvance = OrderAdvance.builder()
                .id(CommonFunction.generalId())
                .amount(query.getAmount())
                .uid(uid)
                .createTime(LocalDateTime.now())
                .advanceType(query.getAdvanceType()).build();

        OrderAdvanceProcessor<?> orderAdvanceProcessor = getOrderAdvanceProcessor(query.getAdvanceType());
        orderAdvanceProcessor.verifier(query);
        orderAdvanceProcessor.preInsertProcess(query, orderAdvance);

        baseMapper.insert(orderAdvance);
        return orderAdvance.getId();
    }

    /**
     * 更新预订单
     */
    @Transactional
    @Override
    public OrderBaseVO updateOrderAdvance(GenerateOrderAdvanceQuery query) {

        if (StringUtils.isBlank(query.getTxid())) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }

        OrderAdvance orderAdvance = baseMapper.selectById(query.getId());
        orderAdvance.setTxid(query.getTxid());
        orderAdvance.setNetwork(query.getNetwork());
        baseMapper.updateById(orderAdvance);

        OrderAdvanceProcessor<?> orderAdvanceProcessor = getOrderAdvanceProcessor(orderAdvance.getAdvanceType());
        return orderAdvanceProcessor.update(orderAdvance);
    }

    /**
     * 处理充值事件
     */
    @Override
    public void handlerRechargeEvent(Long uid, TRONTokenReq req, BigDecimal finalAmount, Coin coin) {
        try {
            // 防止事务未提交
            Thread.sleep(3000);

            var query = new LambdaQueryWrapper<OrderAdvance>()
                    .eq(OrderAdvance::getUid, uid)
                    .eq(OrderAdvance::getTxid, req.getHash())
                    .eq(OrderAdvance::getFinish, 0);
            OrderAdvance orderAdvance = baseMapper.selectOne(query);
            if (Objects.isNull(orderAdvance)) {
                return;
            }

            OrderAdvanceProcessor<?> orderAdvanceProcessor = getOrderAdvanceProcessor(orderAdvance.getAdvanceType());
            orderAdvanceProcessor.handlerRecharge(orderAdvance, req, finalAmount, coin);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            webHookService.dingTalkSend("预购订单异常", e);
        } catch (Exception e) {
            webHookService.dingTalkSend("预购订单异常", e);
        }
    }

    @Override
    public void finish(Long id) {
        int i = baseMapper.finish(id);
        if (i != 1) {
            ErrorCodeEnum.throwException("预订单状态异常:" + id);
        }
    }

    /**
     * 增加尝试的次数
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void addTryTimes(Long id) {
        baseMapper.addTryTimes(id);
    }

}
