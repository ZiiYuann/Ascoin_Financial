package com.tianli.exchange.push;

import cn.hutool.core.util.ObjectUtil;
import com.tianli.exchange.entity.KLinesInfo;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;

import java.math.BigDecimal;

/**
 * @author lzy
 * @date 2022/6/17 14:17
 */
@Builder
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class MiniTickerStream {

    private String e;

    private Long t;

    private String s;

    private BigDecimal c;

    private BigDecimal o;

    private BigDecimal h;

    private BigDecimal l;

    private BigDecimal v;

    private BigDecimal q;

    @Tolerate
    public MiniTickerStream() {
    }

    public static MiniTickerStream getMiniTickerStream(KLinesInfo kLinesInfo) {
        if (ObjectUtil.isNull(kLinesInfo)) {
            return null;
        }
        BigDecimal closing_price = kLinesInfo.getClosing_price();
        return MiniTickerStream.builder()
                .e("24hrMiniTicker")
                .t(System.currentTimeMillis())
                .s(kLinesInfo.getSymbol())
                .c(kLinesInfo.getClosing_price())
                .o(kLinesInfo.getOpening_price().compareTo(BigDecimal.ZERO) > 0 ? kLinesInfo.getOpening_price() : closing_price)
                .h(kLinesInfo.getHighest_price().compareTo(BigDecimal.ZERO) > 0 ? kLinesInfo.getHighest_price() : closing_price)
                .l(kLinesInfo.getLowest_price().compareTo(BigDecimal.ZERO) > 0 ? kLinesInfo.getLowest_price() : closing_price)
                .v(kLinesInfo.getVolume())
                .q(kLinesInfo.getTurnover())
                .build();
    }
}
