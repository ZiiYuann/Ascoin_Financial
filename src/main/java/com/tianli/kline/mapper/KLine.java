package com.tianli.kline.mapper;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * <p>
 * K线图数据
 * </p>
 *
 * @author hd
 * @since 2020-12-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class KLine {

    /**
     * 主键
     */
    private Long id;

    /**
     * 创建时间
     */
    private LocalDateTime create_time;

    /**
     * 精确到日的创建时间
     */
    private LocalDate create_time_day;

    /**
     * 交易对
     */
    private TradingPairEnum pair;

    /**
     * 数据来源渠道(火币之类)
     */
    private String channel;

    /**
     * 抓取价格
     */
    private Double value;

}
