package com.tianli.kline.task;

import lombok.Data;

import java.util.Map;

@Data
public class KlineMacd {
    private Map<String, Double> ema12;
    private Map<String, Double> ema26;
    private Map<String, Double> dea;
}
