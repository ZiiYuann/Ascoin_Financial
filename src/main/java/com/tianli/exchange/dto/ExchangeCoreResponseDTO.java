package com.tianli.exchange.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigInteger;

/**
 * @author lzy
 * @date 2022/6/23 14:54
 */
@Builder
@Data
public class ExchangeCoreResponseDTO {


    private Long id;

    private Long time;

    private String symbol;

    private Integer operator;

    private Long cancel_id;
    /**
     * 价格
     */
    private BigInteger price;
    /**
     * 手数
     */
    private BigInteger quantity;

    private Long buy_id;

    private Long sell_id;
}
