package com.tianli.chain.controller;

import com.tianli.charge.mapper.ChargeStatus;
import com.tianli.currency.TokenCurrencyType;
import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * @author wangqiyun
 * @since 2020/11/17 16:16
 */
@Data
public class ChainTxDTO {
    private Long id;
    private LocalDateTime create_time;
    private LocalDateTime complete_time;
    private ChargeStatus status;
    private Long uid;
    private String sn;
    private TokenCurrencyType currency_type;
    private BigInteger amount;
    private double money;
    private double cny;
    private BigInteger other_amount;
    private String main_address;
    private String collect_address;
    private String txid;
}
