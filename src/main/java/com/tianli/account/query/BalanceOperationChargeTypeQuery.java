package com.tianli.account.query;

import com.tianli.account.enums.AccountOperationType;
import com.tianli.charge.enums.ChargeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BalanceOperationChargeTypeQuery {

    private ChargeType chargeType;

    private AccountOperationType accountOperationType;
}
