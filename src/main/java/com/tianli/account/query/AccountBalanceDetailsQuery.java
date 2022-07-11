package com.tianli.account.query;

import com.tianli.charge.ChargeType;
import lombok.Data;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-11
 **/
@Data
public class AccountBalanceDetailsQuery {

    Long accountBalanceId;

    ChargeType type;
}
