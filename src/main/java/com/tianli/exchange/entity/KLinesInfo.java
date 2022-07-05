package com.tianli.exchange.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.tianli.common.CommonFunction;
import com.tianli.currency_token.mapper.CurrencyCoinEnum;
import com.tianli.exchange.enums.KLinesIntervalEnum;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 *
 * </p>
 *
 * @author lzy
 * @since 2022-06-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
@TableName("k_lines_info")
public class KLinesInfo extends Model<KLinesInfo> {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 开盘时间
     */
    private Long opening_time;

    /**
     * 开盘价格
     */
    @Builder.Default()
    private BigDecimal opening_price = BigDecimal.ZERO;

    /**
     * 第一笔成交id
     */
    @Builder.Default()
    private Long opening_trade_id = 0L;

    /**
     * 最高价
     */
    @Builder.Default()
    private BigDecimal highest_price = BigDecimal.ZERO;

    /**
     * 最低价
     */
    @Builder.Default()
    private BigDecimal lowest_price = BigDecimal.ZERO;
    ;

    /**
     * 收盘价(当前K线未结束的即为最新价)
     */
    @Builder.Default()
    private BigDecimal closing_price = BigDecimal.ZERO;

    /**
     * 最后一笔成交id
     */
    @Builder.Default()
    private Long closing_trade_id = 0L;

    /**
     * 成交量
     */
    @Builder.Default()
    private BigDecimal volume = BigDecimal.ZERO;

    /**
     * 收盘时间
     */
    private Long closing_time;

    /**
     * 成交额
     */
    @Builder.Default()
    private BigDecimal turnover = BigDecimal.ZERO;

    /**
     * 成交笔数
     */
    @Builder.Default()
    private Long turnover_num = 0L;

    /**
     * 主动买入成交量
     */
    @Builder.Default()
    private BigDecimal active_buy_volume = BigDecimal.ZERO;

    /**
     * 主动买入成交额
     */
    @Builder.Default()
    private BigDecimal active_buy_turnover = BigDecimal.ZERO;

    /**
     * 法币
     */
    private String token_fiat;

    /**
     * 货币
     */
    private String token_stock;
    /**
     * 币对 token_stock+token_fiat
     */
    private String symbol;

    private LocalDateTime create_time;

    private LocalDateTime update_time;

    @TableField("`interval`")
    private String interval;

    @Override
    protected Serializable pkVal() {
        return this.id;
    }


    public static KLinesInfo getDefault(String symbol, Long openTime, Long closingTime, KLinesIntervalEnum kLinesIntervalEnum) {
        return KLinesInfo.builder()
                .id(CommonFunction.generalId())
                .opening_time(openTime)
                .closing_time(closingTime)
                .symbol(symbol)
                .opening_trade_id(0L)
                .closing_trade_id(0L)
                .token_fiat(CurrencyCoinEnum.usdt.getName())
                .token_stock(CurrencyCoinEnum.getTokenStock(symbol).name())
                .interval(kLinesIntervalEnum.getInterval())
                .build();
    }
}
