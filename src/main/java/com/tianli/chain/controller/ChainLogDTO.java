package com.tianli.chain.controller;

import com.tianli.currency.enums.CurrencyAdaptType;
import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * @author wangqiyun
 * @since 2020/11/17 11:24
 */

@Data
public class ChainLogDTO {
    private Long id;
    private String address;
    private CurrencyAdaptType currency_type;
    private BigInteger amount;
    private double money;
    private double cny;
    private Long uid;
    private String username;
    private LocalDateTime u_create_time;
}
