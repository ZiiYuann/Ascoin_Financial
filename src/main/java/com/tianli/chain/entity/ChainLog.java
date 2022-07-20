package com.tianli.chain.entity;

import com.tianli.currency.enums.CurrencyAdaptType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * @author wangqiyun
 * @since 2020/11/14 17:58
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChainLog {
    private Long id;
    private String address;
    private CurrencyAdaptType currency_type;
    private BigInteger amount;
    private Long uid;
    private String username;
    private LocalDateTime u_create_time;

}
