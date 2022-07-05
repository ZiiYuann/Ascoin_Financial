package com.tianli.chain.mapper;

import com.tianli.charge.mapper.ChargeStatus;
import com.tianli.currency.TokenCurrencyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * @author wangqiyun
 * @since 2020/11/16 21:00
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChainTx {
    private Long id;
    private LocalDateTime create_time;
    private LocalDateTime complete_time;
    private ChargeStatus status;
    private Long uid;
    private String sn;
    private TokenCurrencyType currency_type;
    private BigInteger amount;
    private BigInteger fee;
    private TokenCurrencyType fee_currency_type;
    private BigInteger other_amount;
    private String main_address;
    private String collect_address;
    private String txid;
    private String block;
}
