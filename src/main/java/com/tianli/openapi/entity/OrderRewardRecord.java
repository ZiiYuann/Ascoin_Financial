package com.tianli.openapi.entity;

import com.tianli.charge.enums.ChargeType;
import com.tianli.common.blockchain.CurrencyCoin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2022-10-28
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRewardRecord {

    private String id;

    private Long uid;

    private BigDecimal amount;

    private ChargeType type;

    private CurrencyCoin coin;

}
