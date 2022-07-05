package com.tianli.kline.controller;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class CurrencyPriceVO {
    /**
     * 币种名 ETH/USDT BTC/USDT...
     */
    private String name;

    /**
     * k线参数符号
     */
    private String symbol;

    /**
     * icon 图片地址
     */
    private String img;

    /**
     * 价格
     */
    private double price;

}
