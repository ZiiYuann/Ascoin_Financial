package com.tianli.currency_token.dto;

import com.tianli.currency_token.mapper.CurrencyCoinEnum;
import com.tianli.currency_token.mapper.TradeDirectionEnum;
import com.tianli.currency_token.mapper.TradeTokenLog;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.RoundingMode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class TradeLogPage {
    private Long id;

    private Long uid;

    private CurrencyCoinEnum token_fiat;

    private CurrencyCoinEnum token_stock;

    private CurrencyCoinEnum token_in;

    private String token_in_amount;

    private CurrencyCoinEnum token_out;

    private String token_out_amount;

    private CurrencyCoinEnum token_fee;

    private String token_fee_amount;

    private String price;

    private TradeDirectionEnum direction;

    private LocalDateTime create_time;

    private Long create_time_ms;

    public static TradeLogPage trans(TradeTokenLog tradeTokenLog) {
        return TradeLogPage.builder()
                .create_time(tradeTokenLog.getCreate_time())
                .direction(tradeTokenLog.getDirection())
                .id(tradeTokenLog.getId())
                .uid(tradeTokenLog.getUid())
                .price(tradeTokenLog.getPrice().setScale(6, RoundingMode.FLOOR).toString())
                .token_fee(tradeTokenLog.getToken_fee())
                .token_fee_amount(tradeTokenLog.getToken_fee_amount().setScale(6, RoundingMode.FLOOR).toString())
                .token_fiat(tradeTokenLog.getToken_fiat())
                .token_stock(tradeTokenLog.getToken_stock())
                .token_in(tradeTokenLog.getToken_in())
                .token_out(tradeTokenLog.getToken_out())
                .create_time_ms(tradeTokenLog.getCreate_time_ms())
                .token_in_amount(tradeTokenLog.getToken_in_amount().setScale(6, RoundingMode.FLOOR).toString())
                .token_out_amount(tradeTokenLog.getToken_out_amount().setScale(6, RoundingMode.FLOOR).toString())
                .build();
    }
}
