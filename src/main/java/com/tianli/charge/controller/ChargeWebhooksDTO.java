package com.tianli.charge.controller;

import com.tianli.currency.TokenCurrencyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * @Author wangqiyun
 * @Date 2020/3/31 15:34
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChargeWebhooksDTO {
    private Long id;
    private Long uid;
    private String sn;
    private LocalDateTime create_time;
    private LocalDateTime success_time;
    private String status;
    private BigInteger amount;
    private String from_address;
    private String to_address;
    private String fee_address;
    private TokenCurrencyType type;
    private Boolean collect;
    private String notify_url;
    private String txid;
    private TokenCurrencyType miner_fee_type;
    private BigInteger miner_fee;
}
