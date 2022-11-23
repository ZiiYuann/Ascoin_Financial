package com.tianli.currency.service;

import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.currency.dto.DollarAmountDTO;
import com.tianli.currency.enums.TokenAdapter;
import com.tianli.management.dto.AmountDto;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-11
 **/
public interface CurrencyService {

    /**
     * 获取汇率
     * @param currencyCoin 币别包装类型
     * @return 美元汇率
     */
    BigDecimal getDollarRate(CurrencyCoin currencyCoin);

    /**
     * 获取常见的汇率
     */
    EnumMap<CurrencyCoin,BigDecimal> getDollarRateMap();

    /**
     * 计算不同币别总金额
     * @param amountDtos 集合
     * @return 总金额
     */
    BigDecimal calDollarAmount(List<AmountDto> amountDtos);

    /**
     * 获取huobi网转usdt的汇率
     *
     * @param coinName 币别名称
     * @return 汇率信息
     */
    BigDecimal huobiUsdtRate(String coinName);
}
