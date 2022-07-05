package com.tianli.bet;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class KLineDirectionResult {
    /**
     * 是否可计算出最终结果
     */
    private Boolean analyzable;

    /**
     * 最终结果 涨跌平
     */
    private KlineDirectionEnum result;

    /**
     * 开始费率
     */
    private Double startExchangeRate;

    /**
     * 结束费率
     */
    private Double endExchangeRate;


    static KLineDirectionResult fail() {
        return KLineDirectionResult.builder()
                .analyzable(false).build();
    }

    public static KLineDirectionResult success(KlineDirectionEnum direction, double start, double end) {
        return KLineDirectionResult.builder()
                .analyzable(true)
                .result(direction)
                .startExchangeRate(start)
                .endExchangeRate(end)
                .build();
    }
}