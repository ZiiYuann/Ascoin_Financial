package com.tianli.rpc.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-28
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LiquidateResponse {
    // 交易对
    private String symbol;
    // 系统的订单ID
    private int orderId;
    // OCO订单ID，否则为 -1
    private int orderListId;
    // 客户自己设置的ID
    private String clientOrderId;
    // 交易的时间戳
    private long transactTime;
    // 订单价格
    private BigDecimal price;
    // 用户设置的原始订单数量
    private BigDecimal origQty;
    // 交易的订单数量
    private BigDecimal executedQty;
    // 累计交易的金额
    private BigDecimal cummulativeQuoteQty;
    // 订单状态
    private String status;
    // 订单的时效方式
    private String timeInForce;
    // 订单类型， 比如市价单，现价单等
    private String type;
    // 订单方向，买还是卖
    private String side;
    // 下单填了参数才会返回
    private int strategyId;
    // 下单填了参数才会返回
    private long strategyType;
    // 订单添加到 order book 的时间
    private long workingTime;
    // 自我交易预防模式
    private String selfTradePreventionMode;
    // 订单中交易的信息
    private List<LiquidateFillResponse> fills;
}

