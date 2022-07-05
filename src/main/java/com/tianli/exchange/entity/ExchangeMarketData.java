package com.tianli.exchange.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 成交记录表
 * </p>
 *
 * @author lzy
 * @since 2022-06-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("exchange_market_data")
public class ExchangeMarketData extends Model<ExchangeMarketData> {

    private static final long serialVersionUID=1L;

    private Long id;

    /**
     * 买方id
     */
    private Long buy_order_id;

    /**
     * 卖方id
     */
    private Long sell_order_id;


    /**
     * 成交价格
     */
    private BigDecimal price;

    /**
     * 成交量
     */
    private BigDecimal volume;

    /**
     * 成交时间
     */
    private Long transaction_time;

    /**
     * 币对
     */
    private String symbol;

    /**
     * 法币
     */
    private String token_fiat;

    /**
     * 货币
     */
    private String token_stock;

    private LocalDateTime create_time;

    private LocalDateTime update_time;


    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
