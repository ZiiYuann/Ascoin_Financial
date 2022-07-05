package com.tianli.kline.task;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class KlineStatDTO {
    private String ch;
    private String status;
    private Long ts;
    private List<Stat> data;

    /**
     * 当前价格
     */
    private Double currentEth;

    /**
     * 当前人民币价格
     */
    private Double currentCny;

    /**
     * 同比增长
     */
    private Double rate;
}
