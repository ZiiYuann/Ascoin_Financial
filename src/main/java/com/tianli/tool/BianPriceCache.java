package com.tianli.tool;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lzy
 * @date 2022/4/29 10:57
 */
public class BianPriceCache {


    private static ConcurrentHashMap<String, Bian24HrInfo> BIAN_PRICES = new ConcurrentHashMap<>();

    private BianPriceCache() {
    }

    public static Bian24HrInfo getPrice(String symbol) {
        return BIAN_PRICES.get(symbol);
    }

    public static void setPrice(String symbol, Bian24HrInfo bian24HrInfo) {
        BIAN_PRICES.put(symbol, bian24HrInfo);
    }


    private static ConcurrentHashMap<String, Double> LATEST_BIAN_PRICES = new ConcurrentHashMap<>();

    public static Double getLatestPrice(String symbol) {
        return LATEST_BIAN_PRICES.get(symbol);
    }

    public static void setLatestBianPrice(String symbol, Double latestPrice) {
        LATEST_BIAN_PRICES.put(symbol, latestPrice);
    }

    public static void deleteLatestBianPrice(String symbol) {
        LATEST_BIAN_PRICES.remove(symbol);
    }
}
