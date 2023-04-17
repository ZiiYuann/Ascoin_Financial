package com.tianli.charge.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.charge.entity.Order;
import com.tianli.charge.entity.OrderChargeInfo;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.mapper.OrderChargeInfoMapper;
import com.tianli.charge.mapper.OrderMapper;
import com.tianli.charge.query.OrderMQuery;
import com.tianli.charge.query.ServiceAmountQuery;
import com.tianli.charge.vo.OrderChargeInfoVO;
import com.tianli.charge.vo.OrderSettleRecordVO;
import com.tianli.currency.service.CurrencyService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.product.afinancial.enums.ProductType;
import com.tianli.product.afinancial.vo.OrderFinancialVO;
import com.tianli.management.dto.AmountDto;
import com.tianli.management.query.FinancialChargeQuery;
import com.tianli.management.query.FinancialOrdersQuery;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-14
 **/

@Slf4j
@Service
public class OrderService extends ServiceImpl<OrderMapper, Order> {

    /**
     * 全局保存订单的入口，保存订单的时候请不要调用其他的接口
     */
    @Override
    public boolean save(Order order) {
        int i = orderMapper.insert(order);
        if (i <= 0) {
            log.error("订单order插入失败，数据{}", JSONUtil.parse(order).toString());
            ErrorCodeEnum.SYSTEM_ERROR.throwException();
        }

        return true;
    }

    @Override
    public boolean saveOrUpdate(Order entity) {
        boolean success = super.saveOrUpdate(entity);
        if (!success) {
            log.error("订单order更新失败，数据{}", JSONUtil.parse(entity).toString());
            ErrorCodeEnum.SYSTEM_ERROR.throwException();
        }
        return true;
    }

    public Order getOrderByHashExcludeUid(Long uid, String hash, ChargeType chargeType) {
        OrderChargeInfo orderChargeInfo = orderChargeInfoService.getByTxidExcludeUid(uid, hash);

        if (Objects.isNull(orderChargeInfo)) {
            return null;
        }

        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getRelatedId, orderChargeInfo.getId())
                .eq(Order::getType, chargeType);

        return orderMapper.selectOne(queryWrapper);
    }

    public Order getOrderByHash(String hash, ChargeType chargeType) {
        OrderChargeInfo orderChargeInfo = orderChargeInfoService.getOrderChargeByTxid(hash);

        if (Objects.isNull(orderChargeInfo)) {
            return null;
        }

        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getRelatedId, orderChargeInfo.getId())
                .eq(Order::getType, chargeType);

        return orderMapper.selectOne(queryWrapper);
    }

    public void insert(OrderChargeInfo orderChargeInfo) {
        int i = orderChargeInfoMapper.insert(orderChargeInfo);
        if (i <= 0) {
            log.error("订单orderChargeInfo插入失败，数据{}", JSONUtil.parse(orderChargeInfo).toString());
            ErrorCodeEnum.SYSTEM_ERROR.throwException();
        }
    }

    public IPage<OrderFinancialVO> selectByPage(IPage<OrderFinancialVO> page, FinancialOrdersQuery financialOrdersQuery) {
        var orderFinancialVOIPage = orderMapper.selectByPage(page, financialOrdersQuery);

        return orderFinancialVOIPage.convert(orderFinancialVO -> {
            orderFinancialVO.setDollarAmount(orderFinancialVO.getAmount()
                    .multiply(currencyService.getDollarRate(orderFinancialVO.getCoin())));
            return orderFinancialVO;
        });
    }

    public IPage<OrderSettleRecordVO> orderSettleInfoVOPage(IPage<OrderSettleRecordVO> page, Long uid, ProductType productType) {
        return orderMapper.selectOrderSettleInfoVOPage(page, uid, productType);
    }

    public IPage<OrderChargeInfoVO> selectOrderChargeInfoVOPage(IPage<OrderChargeInfoVO> page, FinancialChargeQuery query) {
        IPage<OrderChargeInfoVO> orderChargeInfoVOIPage = orderMapper.selectOrderChargeInfoVOPage(page, query);
        orderChargeInfoVOIPage.convert(orderChargeInfoVO -> {
            BigDecimal amount = Optional.ofNullable(orderChargeInfoVO.getAmount()).orElse(BigDecimal.ZERO);
            BigDecimal serviceAmount = Optional.ofNullable(orderChargeInfoVO.getServiceAmount()).orElse(BigDecimal.ZERO);
            orderChargeInfoVO.setAccountAmount(amount.subtract(serviceAmount));
            orderChargeInfoVO.setUsdtAmount(currencyService.getDollarRate(orderChargeInfoVO.getCoin()).multiply(amount));
            return orderChargeInfoVO;
        });
        return orderChargeInfoVOIPage;
    }


    /**
     * 获取不同用户不同订单类型的汇总金额
     */
    public Map<Long, BigDecimal> getSummaryOrderAmount(List<Long> uids, ChargeType type) {
        LambdaQueryWrapper<Order> orderQuery = new LambdaQueryWrapper<Order>()
                .in(Order::getUid, uids)
                .eq(Order::getType, type)
                .eq(Order::getStatus, ChargeStatus.chain_success);

        List<Order> orders = Optional.ofNullable(orderMapper.selectList(orderQuery)).orElse(new ArrayList<>());

        Map<Long, List<Order>> orderMapByUid = orders.stream().collect(Collectors.groupingBy(Order::getUid));
        return orderMapByUid.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().stream().map(order -> {
                    BigDecimal amount = order.getAmount();
                    BigDecimal rate = currencyService.getDollarRate(order.getCoin());
                    return amount.multiply(rate);
                }).reduce(BigDecimal.ZERO, BigDecimal::add)
        ));
    }


    public BigDecimal orderAmountDollarSum(FinancialChargeQuery query) {
        List<AmountDto> amountDtos = orderMapper.orderAmountSum(query);
        return currencyService.calDollarAmount(amountDtos);
    }

    public BigDecimal amountDollarSumByCompleteTime(ChargeType chargeType, LocalDateTime startTime, LocalDateTime endTime) {
        List<AmountDto> amountDtos = orderMapper.amountSumByCompleteTime(chargeType, startTime, endTime);
        return currencyService.calDollarAmount(amountDtos);
    }

    public BigDecimal serviceAmountDollar(ServiceAmountQuery serviceAmountQuery) {
        List<AmountDto> amountDtos = orderMapper.serviceAmountSumByCompleteTime(serviceAmountQuery);
        return currencyService.calDollarAmount(amountDtos);
    }

    public BigDecimal uAmount(Long uid, ChargeType chargeType) {
        OrderMQuery query = OrderMQuery.builder().uid(uid).type(chargeType).build();
        List<AmountDto> amountDtos = orderMapper.amounts(query);
        return currencyService.calDollarAmount(amountDtos);
    }

    public BigDecimal uAmount(List<Long> uids, ChargeType chargeType) {
        if (CollectionUtils.isEmpty(uids)) {
            return BigDecimal.ZERO;
        }
        OrderMQuery query = OrderMQuery.builder().uids(uids).type(chargeType).build();
        List<AmountDto> amountDtos = orderMapper.amounts(query);
        return currencyService.calDollarAmount(amountDtos);
    }

    public BigDecimal uAmount(OrderMQuery query) {
        List<AmountDto> amountDtos = orderMapper.amounts(query);
        return currencyService.calDollarAmount(amountDtos);
    }

    public List<AmountDto> amount(OrderMQuery query) {
        return orderMapper.amounts(query);
    }

    public Order getByOrderNo(String orderNo) {
        LambdaQueryWrapper<Order> query = new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo);
        return orderMapper.selectOne(query);
    }

    public void addAmount(Long id, BigDecimal amount) {
        orderMapper.addAmount(id, amount);
    }


    public void reviewOrder(String orderNo, Long orderReviewId) {
        int i = orderMapper.reviewOrder(orderNo, orderReviewId);
        if (i != 1) {
            throw ErrorCodeEnum.TRADE_FAIL.generalException();
        }
    }

    public void reviewOrderRollback(String orderNo) {
        int i = orderMapper.reviewOrderRollback(orderNo);
        if (i != 1) {
            throw ErrorCodeEnum.TRADE_FAIL.generalException();
        }
    }

    public void chainFail(String orderNo,LocalDateTime now) {
        int i = orderMapper.chainFail(orderNo,now);
        if (i != 1) {
            throw ErrorCodeEnum.TRADE_FAIL.generalException();
        }
    }

    @Resource
    private OrderMapper orderMapper;
    @Resource
    private OrderChargeInfoService orderChargeInfoService;
    @Resource
    private OrderChargeInfoMapper orderChargeInfoMapper;
    @Resource
    private CurrencyService currencyService;



}
