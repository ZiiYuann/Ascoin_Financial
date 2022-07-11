package com.tianli.currency.controller;

import com.tianli.currency.enums.CurrencyAdaptType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户每日奖励记录
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class ReceiveDailyGiftVO {

    /**
     * 代币类型
     */
    private CurrencyAdaptType token;

    /**
     * 数额
     */
    private double amount;

    public static ReceiveDailyGiftVO convert(DailyGiftRecord receive) {
        CurrencyAdaptType token = receive.getToken();
        return ReceiveDailyGiftVO.builder()
                .amount(token.money(receive.getAmount()))
                .token(token)
                .build();
    }
}
