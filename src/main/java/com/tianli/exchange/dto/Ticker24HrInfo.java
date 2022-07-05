package com.tianli.exchange.dto;

import lombok.Data;

/**
 * @author lzy
 * @date 2022/6/13 13:54
 */
@Data
public class Ticker24HrInfo {

    private String symbol;

    private String priceChange;

    private String priceChangePercent;

    private String weightedAvgPrice;

    private String prevClosePrice;

    private String lastPrice;

    private String lastQty;

    private String bidPrice;

    private String bidQty;

    private String askPrice;

    private String askQty;

    private  String openPrice;

    private String highPrice;

    private String lowPrice;
    private String volume;

    private String quoteVolume;

    private Long openTime;

    private Long closeTime;

    private Long firstId;

    private Long lastId;

    private Long count;
}
