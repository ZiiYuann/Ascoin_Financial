package com.tianli.charge.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.account.enums.AccountChangeType;
import com.tianli.charge.entity.OrderChargeInfo;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.entity.Order;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.mapper.OrderChargeInfoMapper;
import com.tianli.charge.mapper.OrderMapper;
import com.tianli.charge.vo.OrderChargeInfoVO;
import com.tianli.charge.vo.OrderSettleInfoVO;
import com.tianli.common.CommonFunction;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.financial.entity.FinancialProduct;
import com.tianli.financial.enums.ProductType;
import com.tianli.financial.vo.OrderFinancialVO;
import com.tianli.management.query.FinancialOrdersQuery;
import com.tianli.management.query.FinancialRechargeQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-14
 **/

@Slf4j
@Service
public class OrderService extends ServiceImpl<OrderMapper,Order> {

    @Resource
    private OrderMapper orderMapper;
    @Resource
    private OrderChargeInfoMapper orderChargeInfoMapper;

    public void saveOrder(Order order) {
        int i = orderMapper.insert(order);
        if (i <= 0) {
            log.error("订单order插入失败，数据{}", JSONUtil.parse(order).toString());
            ErrorCodeEnum.SYSTEM_ERROR.throwException();
        }
    }

    /**
     * 通过交易hash查询 区块链订单信息
     */
    public OrderChargeInfo getOrderChargeByTxid(String txid){
       return orderChargeInfoMapper.selectOne(new LambdaQueryWrapper<OrderChargeInfo>().eq(OrderChargeInfo :: getTxid, txid));
    }

    public void insert(OrderChargeInfo orderChargeInfo) {
        int i = orderChargeInfoMapper.insert(orderChargeInfo);
        if (i <= 0) {
            log.error("订单orderChargeInfo插入失败，数据{}", JSONUtil.parse(orderChargeInfo).toString());
            ErrorCodeEnum.SYSTEM_ERROR.throwException();
        }
    }

    public IPage<OrderFinancialVO> selectByPage(IPage<OrderFinancialVO> page,
                                         Long uid,
                                         ProductType productType,
                                         ChargeType chargeType) {

        FinancialOrdersQuery query = new FinancialOrdersQuery();
        query.setUid(uid);
        query.setProductType(productType);
        query.setChargeType(chargeType);
        return this.selectByPage(page,query);
    }

    public IPage<OrderFinancialVO> selectByPage(IPage<OrderFinancialVO> page, FinancialOrdersQuery financialOrdersQuery) {
        return orderMapper.selectByPage(page,financialOrdersQuery);
    }


    public void updateStatus(Order order){
        orderMapper.updateById(order);
    }


    private Order generateFinancialOrder(Long uid, FinancialProduct financialProduct, ChargeType chargeType
            , ChargeStatus chargeStatus, BigDecimal amount) {
        Order order = new Order();
        order.setUid(uid);
        order.setCoin(financialProduct.getCoin());
        order.setOrderNo( AccountChangeType.financial.getPrefix() + CommonFunction.generalSn(CommonFunction.generalId()));
        order.setRelatedId(financialProduct.getId());
        order.setType(chargeType);
        order.setStatus(chargeStatus);
        order.setAmount(amount);
        order.setCreateTime(LocalDateTime.now());
        return order;
    }

    /**
     *
     */
    public IPage<OrderSettleInfoVO> OrderSettleInfoVOPage(IPage<OrderSettleInfoVO> page, Long uid, ProductType productType) {
        return orderMapper.selectOrderSettleInfoVOPage(page, uid, productType);
    }

    public IPage<OrderChargeInfoVO> selectOrderChargeInfoVOPage(IPage<OrderChargeInfoVO> page, FinancialRechargeQuery query) {
        return orderMapper.selectOrderChargeInfoVOPage(page, query);
    }
}
