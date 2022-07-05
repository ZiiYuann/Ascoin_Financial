package com.tianli.exchange.vo;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import com.tianli.exchange.entity.KLinesInfo;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author lzy
 * @date 2022/6/17 14:17
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Mini24HrTickerVo {

    private String symbol;

    private String priceChangePercent;

    private String lastPrice;

    private String openPrice;

    private String highPrice;

    private String lowPrice;

    private String volume;

    private String quoteVolume;

    private String openTime;

    private String closeTime;

    private Long count;

    @Tolerate
    public Mini24HrTickerVo() {
    }

    public static Mini24HrTickerVo getMini24HrTickerVo(KLinesInfo kLinesInfo) {
        if (ObjectUtil.isNull(kLinesInfo)) {
            return null;
        }
        String closing_price = kLinesInfo.getClosing_price().toString();
        Mini24HrTickerVo mini24HrTickerVo = Mini24HrTickerVo.builder()
                .symbol(kLinesInfo.getSymbol())
                .lastPrice(closing_price)
                .openPrice(kLinesInfo.getOpening_price().compareTo(BigDecimal.ZERO) > 0 ? kLinesInfo.getOpening_price().toString() : closing_price)
                .highPrice(kLinesInfo.getHighest_price().compareTo(BigDecimal.ZERO) > 0 ? kLinesInfo.getHighest_price().toString() : closing_price)
                .lowPrice(kLinesInfo.getLowest_price().compareTo(BigDecimal.ZERO) > 0 ? kLinesInfo.getLowest_price().toString() : closing_price)
                .volume(kLinesInfo.getVolume().toString())
                .quoteVolume(kLinesInfo.getTurnover().toString())
                .openTime(kLinesInfo.getOpening_time().toString())
                .closeTime(kLinesInfo.getClosing_time().toString())
                .count(kLinesInfo.getTurnover_num())
                .build();
        mini24HrTickerVo.setPriceChangePercent((kLinesInfo.getClosing_price().subtract(Convert.toBigDecimal(mini24HrTickerVo.getOpenPrice()))).divide(Convert.toBigDecimal(mini24HrTickerVo.getOpenPrice()), 2, RoundingMode.FLOOR).multiply(new BigDecimal("100")).toString());
        return mini24HrTickerVo;
    }
}
