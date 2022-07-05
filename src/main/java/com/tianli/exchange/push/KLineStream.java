package com.tianli.exchange.push;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * @author lzy
 * @date 2022/6/15 15:54
 */
@Builder
public class KLineStream {

    private Long t;

    private Long T;

    private String s;

    private String i;

    private Long f;

    private Long L;

    private BigDecimal o;

    private BigDecimal c;

    private BigDecimal h;

    private BigDecimal l;

    private BigDecimal v;

    private Long n;

    private Boolean x;

    private BigDecimal q;

    private BigDecimal V;

    private BigDecimal Q;
}
