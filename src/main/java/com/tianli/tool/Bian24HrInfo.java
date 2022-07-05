package com.tianli.tool;

import lombok.Data;

/**
 * @author lzy
 * @date 2022/4/29 11:45
 */
@Data
public class Bian24HrInfo {

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
