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
import com.tianli.charge.vo.OrderChargeInfoVO;
import com.tianli.charge.vo.OrderSettleRecordVO;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.currency.service.CurrencyService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.financial.enums.ProductType;
import com.tianli.financial.vo.OrderFinancialVO;
import com.tianli.management.query.FinancialChargeQuery;
import com.tianli.management.query.FinancialOrdersQuery;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
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
public class OrderService extends ServiceImpl<OrderMapper,Order> {


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

    public IPage<OrderFinancialVO> selectByPage(IPage<OrderFinancialVO> page, FinancialOrdersQuery financialOrdersQuery) {
        var orderFinancialVOIPage = orderMapper.selectByPage(page, financialOrdersQuery);
        EnumMap<CurrencyCoin, BigDecimal> dollarRateMap = currencyService.getDollarRateMap();

        return orderFinancialVOIPage.convert(orderFinancialVO -> {
            orderFinancialVO.setDollarAmount(orderFinancialVO.getAmount().multiply(dollarRateMap.getOrDefault(orderFinancialVO.getCoin(),BigDecimal.ONE)));
            return orderFinancialVO;
        });
    }

    public IPage<OrderSettleRecordVO> OrderSettleInfoVOPage(IPage<OrderSettleRecordVO> page, Long uid, ProductType productType) {
        return orderMapper.selectOrderSettleInfoVOPage(page, uid, productType);
    }

    public IPage<OrderChargeInfoVO> selectOrderChargeInfoVOPage(IPage<OrderChargeInfoVO> page, FinancialChargeQuery query) {
        return orderMapper.selectOrderChargeInfoVOPage(page, query);
    }


    /**
     * 获取不同用户不同订单类型的汇总金额
     */
    public Map<Long,BigDecimal> getSummaryOrderAmount(List<Long> uids,ChargeType type){
        LambdaQueryWrapper<Order> orderQuery = new LambdaQueryWrapper<Order>()
                .in(Order::getUid, uids)
                .eq(Order::getType, type)
                .eq(Order :: getStatus, ChargeStatus.chain_success);

        List<Order> orders = Optional.ofNullable(orderMapper.selectList(orderQuery)).orElse(new ArrayList<>());

        Map<Long, List<Order>> orderMapByUid = orders.stream().collect(Collectors.groupingBy(Order::getUid));
        EnumMap<CurrencyCoin, BigDecimal> dollarRateMap = currencyService.getDollarRateMap();
        return orderMapByUid.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().stream().map( order -> {
                    BigDecimal amount = order.getAmount();
                    BigDecimal rate = dollarRateMap.getOrDefault(order.getCoin(), BigDecimal.ONE);
                    return amount.multiply(rate);
                }).reduce(BigDecimal.ZERO,BigDecimal::add)
        ));
    }


    public BigDecimal orderAmountSum(FinancialChargeQuery query) {
        return Optional.ofNullable(orderMapper.orderAmountSum(query)).orElse(BigDecimal.ZERO);
    }

    public BigDecimal orderAmountSumByCompleteTime(ChargeType chargeType, LocalDateTime startTime, LocalDateTime endTime) {
        return Optional.ofNullable(orderMapper.orderAmountSumByCompleteTime(chargeType,startTime,endTime))
                .orElse(BigDecimal.ZERO);
    }


    @Resource
    private OrderMapper orderMapper;
    @Resource
    private OrderChargeInfoMapper orderChargeInfoMapper;
    @Resource
    private CurrencyService currencyService;


}
