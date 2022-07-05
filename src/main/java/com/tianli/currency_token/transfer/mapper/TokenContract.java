package com.tianli.currency_token.transfer.mapper;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.StrUtil;
import com.tianli.currency_token.mapper.ChainType;
import com.tianli.currency_token.mapper.CurrencyCoinEnum;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

/**
 * <p>
 * 链合约地址绑定表
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class TokenContract {
    private Long id;

    private CurrencyCoinEnum token;

    private ChainType chain;

    private String contract_address;

    private Integer decimals;

    private BigDecimal withdraw_rate;

    private BigDecimal withdraw_min_amount;

    private BigDecimal withdraw_fixed_amount;

    private Boolean platform_token;


    public BigDecimal money(BigInteger money) {
        String format = StrUtil.fillAfter("1", Convert.toChar(0), decimals + 1);
        return Convert.toBigDecimal(money).divide(Convert.toBigDecimal(format));
    }

    public BigInteger money(BigDecimal money) {
        String format = StrUtil.fillAfter("1", Convert.toChar(0), decimals + 1);
        return Convert.toBigInteger(money.subtract(Convert.toBigDecimal(format)));
    }

    public Boolean isMainCurrency() {
        List<CurrencyCoinEnum> mainCurrency = ListUtil.of(CurrencyCoinEnum.bnb, CurrencyCoinEnum.eth, CurrencyCoinEnum.trx);
        return mainCurrency.contains(token);
    }
}
