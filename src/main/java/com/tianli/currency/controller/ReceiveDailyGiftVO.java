package com.tianli.currency.controller;

import com.tianli.currency.enums.TokenAdapter;
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
    private TokenAdapter token;

    /**
     * 数额
     */
    private double amount;

}
