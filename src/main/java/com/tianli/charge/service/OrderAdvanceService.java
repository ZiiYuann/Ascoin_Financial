package com.tianli.charge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.account.enums.AccountChangeType;
import com.tianli.chain.dto.TRONTokenReq;
import com.tianli.charge.entity.Order;
import com.tianli.charge.entity.OrderAdvance;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.mapper.OrderAdvanceMapper;
import com.tianli.charge.query.GenerateOrderAdvanceQuery;
import com.tianli.charge.vo.OrderBaseVO;
import com.tianli.common.CommonFunction;
import com.tianli.common.annotation.NoRepeatCommit;
import com.tianli.currency.enums.TokenAdapter;
import com.tianli.financial.entity.FinancialProduct;
import com.tianli.financial.entity.FinancialRecord;
import com.tianli.financial.enums.ProductStatus;
import com.tianli.financial.enums.RecordStatus;
import com.tianli.financial.query.PurchaseQuery;
import com.tianli.financial.service.FinancialProductService;
import com.tianli.financial.service.FinancialRecordService;
import com.tianli.financial.service.FinancialService;
import com.tianli.sso.init.RequestInitService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author chenb
 * @apiNote
 * @since 2022-08-31
 **/
@Service
public class OrderAdvanceService extends ServiceImpl<OrderAdvanceMapper, OrderAdvance> {

    @Resource
    private RequestInitService requestInitService;
    @Resource
    private FinancialProductService financialProductService;
    @Resource
    private FinancialService financialService;
    @Resource
    private OrderService orderService;
    @Resource
    private FinancialRecordService financialRecordService;
    @Resource
    private ChargeService chargeService;

    /**
     * 生成预订单
     */
    @Transactional
    @NoRepeatCommit(autoUnlock = false)
    public Long generateOrderAdvance(GenerateOrderAdvanceQuery query) {

        Long uid = requestInitService.uid();

        FinancialProduct product = financialProductService.getById(query.getProductId());

        OrderAdvance orderAdvance = OrderAdvance.builder()
                .id(CommonFunction.generalId())
                .amount(query.getAmount())
                .uid(uid)
                .createTime(LocalDateTime.now())
                .productId(query.getProductId())
                .coin(query.getCoin())
                .term(query.getTerm())
                .autoCurrent(query.isAutoCurrent())
                .build();
        baseMapper.insert(orderAdvance);

        // 生成一笔预订单记录
        Order order = Order.builder()
                .uid(requestInitService.uid())
                .coin(product.getCoin())
                .orderNo(AccountChangeType.advance_purchase.getPrefix() + orderAdvance.getId())
                .amount(query.getAmount())
                .type(ChargeType.purchase)
                .status(ChargeStatus.created)
                .createTime(LocalDateTime.now())
                .status(ChargeStatus.chaining)
                .relatedId(orderAdvance.getId())
                .build();
        orderService.save(order);

        FinancialRecord financialRecord =
                financialRecordService.generateFinancialRecord(uid, product, query.getAmount(), query.isAutoCurrent());
        // 预订单
        financialRecord.setStatus(RecordStatus.SUCCESS);
        financialRecordService.updateById(financialRecord);

        return orderAdvance.getId();
    }

    /**
     * 更新预订单
     */
    @Transactional
    public OrderBaseVO updateOrderAdvance(GenerateOrderAdvanceQuery query) {
        OrderAdvance orderAdvance = baseMapper.selectById(query.getId());
        orderAdvance.setTxid(query.getTxid());
        baseMapper.updateById(orderAdvance);

        return chargeService.orderDetails(requestInitService.uid()
                , AccountChangeType.advance_purchase.getPrefix() + query.getId());
    }

    /**
     * 处理充值事件
     */

    public void handlerRechargeEvent(Long uid, TRONTokenReq req, BigDecimal finalAmount, TokenAdapter tokenAdapter) {
        var query = new LambdaQueryWrapper<OrderAdvance>()
                .eq(OrderAdvance::getUid, uid)
                .eq(OrderAdvance::getTxid, req.getHash());
        OrderAdvance orderAdvance = baseMapper.selectOne(query);
        if (Objects.isNull(orderAdvance)) {
            return;
        }

        Long productId = orderAdvance.getProductId();
        FinancialProduct product = financialProductService.getById(productId);
        if (Objects.isNull(product)) {
            log.error("预订单产品不存在，请注意");
            return;
        }

        if (ProductStatus.close.equals(product.getStatus())) {
            log.error("预订单产品处于关闭状态，无法申购");
            return;
        }

        if (!product.getCoin().equals(tokenAdapter.getCurrencyCoin())) {
            log.error("申购产品币别类型与充值币别不符合");
            return;
        }

        if (orderAdvance.getAmount().compareTo(finalAmount) != 0) {
            log.error("充值金额与预订单金额不符合");
            return;
        }

        PurchaseQuery purchaseQuery = PurchaseQuery.builder()
                .coin(orderAdvance.getCoin())
                .term(orderAdvance.getTerm())
                .amount(orderAdvance.getAmount())
                .autoCurrent(orderAdvance.isAutoCurrent())
                .productId(orderAdvance.getProductId()).build();

        financialService.purchase(uid, purchaseQuery);
    }
}
