package com.tianli.currency_token.mapper;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.tianli.common.CommonFunction;
import com.tianli.currency_token.order.mapper.CurrencyTokenOrder;
import com.tianli.exchange.dto.ExchangeCoreResponseConvertDTO;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 用户交易记录表
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class TradeTokenLog extends Model<TradeTokenLog> {
    /**
     * 主键
     */
    private Long id;

    /**
     * 用户id
     */
    private Long uid;

    /**
     * 委托单id
     */
    private Long order_id;

    /**
     * 法币
     */
    private CurrencyCoinEnum token_fiat;

    /**
     * 货物
     */
    private CurrencyCoinEnum token_stock;

    /**
     * 入的token
     */
    private CurrencyCoinEnum token_in;

    /**
     * 入的金额
     */
    private BigDecimal token_in_amount;

    /**
     * 出的token
     */
    private CurrencyCoinEnum token_out;

    /**
     * 出的金额
     */
    private BigDecimal token_out_amount;

    /**
     * 出的token
     */
    private CurrencyCoinEnum token_fee;

    /**
     * 出的金额
     */
    private BigDecimal token_fee_amount;

    /**
     * 成交价格
     */
    private BigDecimal price;

    /**
     * 操作方向buy, sell
     */
    private TradeDirectionEnum direction;

    /**
     * 创建时间
     */
    private LocalDateTime create_time;
    /**
     * 交易时间戳
     */
    private Long create_time_ms;

    public static TradeTokenLog getBuyTradeTokenLog(CurrencyTokenOrder currencyTokenOrder, ExchangeCoreResponseConvertDTO coreResponseConvertDTO, BigDecimal fee) {
        return TradeTokenLog.builder()
                .id(CommonFunction.generalId())
                .uid(currencyTokenOrder.getUid())
                .order_id(currencyTokenOrder.getId())
                .token_fiat(currencyTokenOrder.getToken_fiat())
                .token_stock(currencyTokenOrder.getToken_stock())
                .token_in(currencyTokenOrder.getToken_stock())
                .token_in_amount(coreResponseConvertDTO.getQuantity())
                .token_out(currencyTokenOrder.getToken_fiat())
                .token_out_amount(coreResponseConvertDTO.getQuantity().multiply(coreResponseConvertDTO.getPrice()))
                .token_fee(currencyTokenOrder.getToken_stock())
                .token_fee_amount(fee)
                .price(coreResponseConvertDTO.getPrice())
                .direction(TradeDirectionEnum.buy)
                .create_time(LocalDateTime.now())
                .create_time_ms(coreResponseConvertDTO.getTime())
                .build();
    }

    public static TradeTokenLog getSellTradeTokenLog(CurrencyTokenOrder currencyTokenOrder, ExchangeCoreResponseConvertDTO coreResponseConvertDTO, BigDecimal fee) {
        return TradeTokenLog.builder()
                .id(CommonFunction.generalId())
                .uid(currencyTokenOrder.getUid())
                .order_id(currencyTokenOrder.getId())
                .token_fiat(currencyTokenOrder.getToken_fiat())
                .token_stock(currencyTokenOrder.getToken_stock())
                .token_in(currencyTokenOrder.getToken_fiat())
                .token_in_amount(coreResponseConvertDTO.getQuantity().multiply(coreResponseConvertDTO.getPrice()))
                .token_out(currencyTokenOrder.getToken_stock())
                .token_out_amount(coreResponseConvertDTO.getQuantity())
                .token_fee(currencyTokenOrder.getToken_fiat())
                .token_fee_amount(fee)
                .price(coreResponseConvertDTO.getPrice())
                .direction(TradeDirectionEnum.sell)
                .create_time(LocalDateTime.now())
                .create_time_ms(coreResponseConvertDTO.getTime())
                .build();
    }
}
