package com.tianli.currency_token.order.mapper;

import com.tianli.currency_token.mapper.CurrencyCoinEnum;
import com.tianli.currency_token.mapper.TokenOrderType;
import com.tianli.currency_token.mapper.TradeDirectionEnum;
import com.tianli.exchange.enums.PlatformTypeEnum;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户委托单
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class CurrencyTokenOrder {
    private Long id;

    private Long uid;
    /**
     * 订单类型
     */
    private TokenOrderType type;
    /**
     * 法币
     */
    private CurrencyCoinEnum token_fiat;
    /**
     * 货币
     */
    private CurrencyCoinEnum token_stock;
    /**
     * 价格
     */
    private BigDecimal price;
    /**
     * 平均成交价格
     */
    private BigDecimal deal_price;
    /**
     * 订单买卖方向
     */
    private TradeDirectionEnum direction;
    /**
     * 买卖量
     */
    private BigDecimal amount;
    /**
     * 买卖数量单位,永远是token_stock
     */
    private CurrencyCoinEnum amount_unit;
    /**
     * 已成交量 单位是永远是token_stock
     */
    private BigDecimal deal_amount;
    /**
     * 订单状态
     */
    private CurrencyTokenOrderStatus status;
    /**
     * 手续费
     */
    private BigDecimal handling_fee;
    /**
     * 手续费单位
     */
    private CurrencyCoinEnum handling_fee_unit;
    /**
     * 交易额 单位是token_fiat 法币
     */
    private BigDecimal tr_amount;

    /**
     * 成交额 单位是token_fiat 法币
     */
    private BigDecimal deal_tr_amount;

    private LocalDateTime create_time;

    private LocalDateTime deal_time;

    private Long create_time_ms;

    private Long deal_time_ms;

    private LocalDateTime update_time;

    private PlatformTypeEnum platform_type;
    /**
     * 市价单类型 0-数量 1-交易额
     */
    private Integer market_price_type;
}
