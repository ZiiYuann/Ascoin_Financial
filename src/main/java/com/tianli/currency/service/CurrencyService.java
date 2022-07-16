package com.tianli.currency.service;

import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.currency.dto.DollarAmountDTO;
import com.tianli.currency.enums.CurrencyAdaptType;

import java.math.BigDecimal;
import java.util.EnumMap;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-11
 **/
public interface CurrencyService {

    /**
     * 将金额转化成美元
     * @param currencyAdaptType 币别包装类型
     * @param amount 需要转化的金额
     * @return 美元金额DTO
     */
    DollarAmountDTO convertDollarAmount(CurrencyAdaptType currencyAdaptType, BigDecimal amount);

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
}
