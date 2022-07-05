package com.tianli.exchange.dto;

import com.tianli.common.CommonFunction;
import com.tianli.currency_token.mapper.TradeDirectionEnum;
import com.tianli.currency_token.order.mapper.CurrencyTokenOrder;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author lzy
 * @date 2022/6/22 16:30
 */
@Builder
@Data
public class ExchangeCoreRequestDTO {

    private Long id;

    private String symbol;

    private Integer operator;

    private BigInteger price;

    private BigInteger quantity;

    private Integer trade;

    private BigInteger amount;

    private Long cancel_id;

    public static ExchangeCoreRequestDTO initMarketBuyOrder(CurrencyTokenOrder currencyTokenOrder, BigDecimal remain) {
        BigInteger quantity;
        BigInteger amount;
        if (currencyTokenOrder.getMarket_price_type().equals(0)) {
            quantity = currencyTokenOrder.getAmount().multiply(new BigDecimal("1000")).toBigInteger();
            amount = remain.multiply(new BigDecimal("1000000")).toBigInteger();
        } else {
            quantity = BigInteger.valueOf(Integer.MAX_VALUE);
            amount = currencyTokenOrder.getTr_amount().multiply(new BigDecimal("1000000")).toBigInteger();
        }
        return ExchangeCoreRequestDTO.builder()
                .id(currencyTokenOrder.getId())
                .symbol(currencyTokenOrder.getToken_stock().name().toUpperCase() + currencyTokenOrder.getToken_fiat().name().toUpperCase())
                .operator(1)
                .price(BigInteger.valueOf(Integer.MAX_VALUE))
                .trade(0)
                .quantity(quantity)
                .amount(amount)
                .build();
    }

    public static ExchangeCoreRequestDTO initMarketSellOrder(CurrencyTokenOrder currencyTokenOrder, BigDecimal remain) {
        BigInteger quantity;
        BigInteger amount;
        if (currencyTokenOrder.getMarket_price_type().equals(0)) {
            quantity = currencyTokenOrder.getAmount().multiply(new BigDecimal("1000")).toBigInteger();
            amount = BigInteger.valueOf(Long.MAX_VALUE);
        } else {
            quantity = remain.multiply(new BigDecimal("1000")).toBigInteger();
            amount = currencyTokenOrder.getTr_amount().multiply(new BigDecimal("1000000")).toBigInteger();
        }
        return ExchangeCoreRequestDTO.builder()
                .id(currencyTokenOrder.getId())
                .symbol(currencyTokenOrder.getToken_stock().name().toUpperCase() + currencyTokenOrder.getToken_fiat().name().toUpperCase())
                .operator(2)
                .price(BigInteger.valueOf(0L))
                .trade(0)
                .quantity(quantity)
                .amount(amount)
                .build();
    }


    public static ExchangeCoreRequestDTO initTrade(CurrencyTokenOrder currencyTokenOrder) {
        BigDecimal price = currencyTokenOrder.getPrice().multiply(new BigDecimal("1000"));
        return ExchangeCoreRequestDTO.builder()
                .id(currencyTokenOrder.getId())
                .symbol(currencyTokenOrder.getToken_stock().name().toUpperCase() + currencyTokenOrder.getToken_fiat().name().toUpperCase())
                .operator(currencyTokenOrder.getDirection().equals(TradeDirectionEnum.buy) ? 1 : 2)
                .price(price.toBigInteger())
                .trade(1)
                .quantity(currencyTokenOrder.getAmount().multiply(new BigDecimal("1000")).toBigInteger())
                .amount(BigInteger.ZERO)
                .build();
    }

    public static ExchangeCoreRequestDTO initRevokeOrderTrade(CurrencyTokenOrder currencyTokenOrder) {
        return ExchangeCoreRequestDTO.builder()
                .id(CommonFunction.generalId())
                .symbol(currencyTokenOrder.getToken_stock().name().toUpperCase() + currencyTokenOrder.getToken_fiat().name().toUpperCase())
                .operator(3)
                .cancel_id(currencyTokenOrder.getId())
                .build();
    }
}
