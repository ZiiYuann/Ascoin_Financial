package com.tianli.charge.enums;

import lombok.Getter;

/**
 * @author :yangkang
 * @since : 2023-03-15 10:02
 * @apiNote : 交易类型组枚举
 */
public enum ChargeTypeGroupEnum {

    RECHARGE("Recharge", "充值", 1),

    WITHDRAW("Withdraw", "提币", 2),

    IN("Transfer in", "转入", 3),

    OUT("Transfer out", "转出", 4);

    @Getter
    private final String typeGroupEn;
    @Getter
    private final String typeGroup;
    @Getter
    private final Integer order;

    ChargeTypeGroupEnum(String typeGroupEn, String typeGroup, Integer order) {
        this.typeGroupEn = typeGroupEn;
        this.typeGroup = typeGroup;
        this.order = order;
    }

}
