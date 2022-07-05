package com.tianli.currency_token.dto;

import com.tianli.currency_token.mapper.CurrencyCoinEnum;
import com.tianli.currency_token.token.mapper.TokenList;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class CurrencyTokenPage {
    /**
     * 币种
     */
    private CurrencyCoinEnum token;
    /**
     * 余额
     */
    private String balance;
    /**
     * 冻结
     */
    private String freeze;
    /**
     * 可用
     */
    private String remain;
    /**
     * u的价值
     */
    private String value_u;
    private String value_freeze_u;
    private String value_remain_u;
    private BigDecimal value_balance;
    private BigDecimal value_freeze;
    private BigDecimal value_remain;
    private TokenList tokenInfo;

    private Boolean is_platform;
}
