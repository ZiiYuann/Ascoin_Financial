package com.tianli.charge.controller;

import com.tianli.currency.enums.CurrencyAdaptType;
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
    private LocalDateTime createTime;
    private LocalDateTime successTime;
    private String status;
    private BigInteger amount;
    private String fromAddress;
    private String toAddress;
    private String feeAddress;
    private CurrencyAdaptType type;
    private Boolean collect;
    private String notifyUrl;
    private String txid;
    private CurrencyAdaptType minerFeeType;
    private BigInteger minerFee;
}
