package com.tianli.management.spot.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author lzy
 * @date 2022/4/16 11:12 上午
 */
@Data
public class SGTransactionRecordListVo {

    private Long id;

    private String username;

    private String user_nick;

    private Integer user_type;

    private String token_fiat;

    private String token_stock;

    private String token_in;

    private String token_out;
    /**
     * '操作方向sell, buy
     */
    private String direction;

    private BigDecimal token_in_amount;

    private BigDecimal token_out_amount;

    private BigDecimal token_fee_amount;

    private String token_fee;

    private BigDecimal price;

    private LocalDateTime create_time;

    /**
     * 转账网络
     */
    private String currency_type;
}
