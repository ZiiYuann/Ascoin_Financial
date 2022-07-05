package com.tianli.address.controller;

import com.tianli.currency.TokenCurrencyType;
import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * @Author wangqiyun
 * @Date 2020/3/30 18:03
 */

@Data
public class AddressWebhooksDTO {
    private String txid;
    private String from_address;
    private String to_address;
    private String block;
    private LocalDateTime create_time;
    private String sn;
    private TokenCurrencyType type;
    private BigInteger value;
}
