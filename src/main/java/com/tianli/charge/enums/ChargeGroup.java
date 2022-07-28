package com.tianli.charge.enums;

import lombok.Getter;

import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-28
 **/
public enum ChargeGroup {
    // 收入
    receive(List.of(ChargeType.recharge,ChargeType.income,ChargeType.redeem,ChargeType.settle)),
    // 支出
    pay(List.of(ChargeType.withdraw,ChargeType.purchase,ChargeType.repay));

    ChargeGroup(List<ChargeType> chargeTypes){
        this.name = name();
        this.chargeTypes = chargeTypes;
    }

    @Getter
    private final String name;

    @Getter
    private final List<ChargeType> chargeTypes;
}
