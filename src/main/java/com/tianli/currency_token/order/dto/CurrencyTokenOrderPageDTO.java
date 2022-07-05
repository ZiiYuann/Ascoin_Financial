package com.tianli.currency_token.order.dto;

import com.tianli.currency_token.mapper.CurrencyCoinEnum;
import com.tianli.currency_token.mapper.TokenOrderType;
import com.tianli.currency_token.mapper.TradeDirectionEnum;
import com.tianli.currency_token.order.mapper.CurrencyTokenOrder;
import com.tianli.currency_token.order.mapper.CurrencyTokenOrderStatus;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class CurrencyTokenOrderPageDTO {
    private Long id;

    private Long uid;

    private TokenOrderType type;

    private CurrencyCoinEnum token_fiat;

    private CurrencyCoinEnum token_stock;

    private BigDecimal price;

    private BigDecimal deal_price;

    private TradeDirectionEnum direction;

    private BigDecimal amount;

    private CurrencyCoinEnum amount_unit;

    private BigDecimal deal_amount;

    private CurrencyTokenOrderStatus status;

    private BigDecimal deal_tr_amount;

    private LocalDateTime create_time;

    private LocalDateTime deal_time;

    private Long create_time_ms;

    private Long deal_time_ms;

    public static CurrencyTokenOrderPageDTO trans(CurrencyTokenOrder currencyTokenOrder) {
        return CurrencyTokenOrderPageDTO.builder()
                .amount(currencyTokenOrder.getAmount())
                .amount_unit(currencyTokenOrder.getAmount_unit())
                .create_time(currencyTokenOrder.getCreate_time())
                .create_time_ms(currencyTokenOrder.getCreate_time().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .deal_amount(currencyTokenOrder.getDeal_amount())
                .deal_price(currencyTokenOrder.getDeal_price())
                .deal_time(currencyTokenOrder.getDeal_time())
                .deal_time_ms(currencyTokenOrder.getDeal_time().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                .token_fiat(currencyTokenOrder.getToken_fiat())
                .token_stock(currencyTokenOrder.getToken_stock())
                .direction(currencyTokenOrder.getDirection())
                .price(currencyTokenOrder.getPrice())
                .id(currencyTokenOrder.getId())
                .uid(currencyTokenOrder.getUid())
                .status(currencyTokenOrder.getStatus())
                .deal_tr_amount(currencyTokenOrder.getDeal_tr_amount())
                .type(currencyTokenOrder.getType()).build();
    }


}
