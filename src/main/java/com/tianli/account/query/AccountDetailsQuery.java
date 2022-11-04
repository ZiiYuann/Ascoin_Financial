package com.tianli.account.query;

import com.tianli.charge.enums.ChargeGroup;
import com.tianli.charge.enums.ChargeType;
import com.tianli.common.blockchain.CurrencyCoin;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-11-04
 **/
@Data
@NoArgsConstructor
public class AccountDetailsQuery {

    private ChargeGroup chargeGroup;

    private CurrencyCoin coin;

    private ChargeType chargeType;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

}
