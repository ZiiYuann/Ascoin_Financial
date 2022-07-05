package com.tianli.kline.task;

import lombok.Data;

@Data
public class Stat {
    /**
     * 调整为新加坡时间的时间戳，单位秒，并以此作为此K线柱的id
     */
    private Long id;
    /**
     * 本阶段开盘价
     */
    private Double open;
    /**
     * 本阶段收盘价
     */
    private Double close;
    /**
     * 本阶段最低价
     */
    private Double low;
    /**
     * 本阶段最高价
     */
    private Double high;
//    /**
//     * 以基础币种计量的交易量
//     */
//    private Double amount;
//    /**
//     * 以报价币种计量的交易量
//     */
//    private Double vol;
//    /**
//     * 交易次数
//     */
//    private Integer count;
    /**
     * MA 10, 30, 60
     */
    private double ma10;
    private double ma30;
    private double ma60;
    /**
     * 押注统计
     */
    private double betStat;
    /**
     * MACD 等信息
     */
    private double ema12;
    private double ema26;
    private double diff;
    private double dea;
    private double bar;

}