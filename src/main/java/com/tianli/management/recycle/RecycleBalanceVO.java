package com.tianli.management.recycle;

import com.tianli.currency_token.mapper.ChainType;
import com.tianli.currency_token.mapper.CurrencyCoinEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @Author cs
 * @Date 2022-03-29 2:44 下午
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RecycleBalanceVO {
    private long id;
    private String address;
    private BigDecimal balance;
    private CurrencyCoinEnum token;
    private ChainType chain;

}
