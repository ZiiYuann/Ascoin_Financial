package com.tianli.charge.query;

import com.tianli.charge.enums.ChargeType;
import com.tianli.common.blockchain.CurrencyCoin;

import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-29
 **/
public class ServiceAmountQuery {

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private ChargeType chargeType;

    private CurrencyCoin coin;
}