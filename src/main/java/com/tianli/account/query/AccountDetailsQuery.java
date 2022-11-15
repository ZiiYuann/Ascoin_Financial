package com.tianli.account.query;

import com.tianli.charge.enums.ChargeGroup;
import com.tianli.charge.enums.ChargeType;
import com.tianli.common.blockchain.CurrencyCoin;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

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

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    private List<ChargeGroup> chargeGroups;

    private List<ChargeType> chargeTypes;

}
