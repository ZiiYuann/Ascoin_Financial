package com.tianli.management.query;

import com.tianli.charge.enums.ChargeType;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.common.blockchain.CurrencyNetworkType;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-19
 **/
@Data
public class FinancialChargeQuery {

    private String txid;

    private CurrencyCoin coin;

    private Long uid;

    private CurrencyNetworkType networkType;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private ChargeType chargeType;

}
