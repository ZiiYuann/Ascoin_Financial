package com.tianli.dividends.controller;

import com.tianli.currency.TokenCurrencyType;
import com.tianli.dividends.mapper.Dividends;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * <p>
 * 分红表
 * </p>
 *
 * @author hd
 * @since 2020-12-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class DividendsVO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 创建时间
     */
    private LocalDateTime create_time;
    private Long create_time_ms;

    /**
     * 总净盈亏
     */
    private double all_profit;

    /**
     * 上级净盈亏
     */
    private double senior_profit;

    /**
     * 本级净盈亏
     */
    private double my_profit;

    /**
     * 下级净盈亏
     */
    private double low_profit;

    public static DividendsVO trans(Dividends dividends) {
        LocalDateTime create_time = dividends.getCreate_time();
        Instant create_instant = create_time.atZone(ZoneId.systemDefault()).toInstant();
        return DividendsVO.builder()
                .id(dividends.getId())
                .create_time(create_time)
                .create_time_ms(create_instant.toEpochMilli())
                .all_profit(TokenCurrencyType.usdt_omni.money(dividends.getAll_profit()))
                .senior_profit(TokenCurrencyType.usdt_omni.money(dividends.getSenior_profit()))
                .my_profit(TokenCurrencyType.usdt_omni.money(dividends.getMy_profit()))
                .low_profit(TokenCurrencyType.usdt_omni.money(dividends.getLow_profit()))
                .build();

    }
}
