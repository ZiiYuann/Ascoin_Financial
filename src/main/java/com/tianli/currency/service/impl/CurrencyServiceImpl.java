package com.tianli.currency.service.impl;

import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.currency.service.DigitalCurrencyExchange;
import com.tianli.currency.enums.NationalCurrencyEnum;
import com.tianli.currency.dto.DollarAmountDTO;
import com.tianli.currency.enums.TokenAdapter;
import com.tianli.currency.service.CurrencyService;
import com.tianli.exception.ErrorCodeEnum;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.Optional;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-11
 **/
@Service
public class CurrencyServiceImpl implements CurrencyService {

    @Resource
    private DigitalCurrencyExchange digitalCurrencyExchange;

    @Override
    public BigDecimal getDollarRate(CurrencyCoin currencyCoin) {
        switch (currencyCoin) {
            case usdc:
            case usdt:
                return BigDecimal.ONE;
            case bnb:
                return BigDecimal.valueOf(digitalCurrencyExchange.bnbUsdtPrice());
            case eth:
                return BigDecimal.valueOf(digitalCurrencyExchange.ethUsdtPrice());
            case trx:
                return BigDecimal.valueOf(digitalCurrencyExchange.trxUsdtPrice());
            default:
                break;
        }
        throw ErrorCodeEnum.CURRENCY_NOT_SUPPORT.generalException();
    }

    @Override
    public EnumMap<CurrencyCoin, BigDecimal> getDollarRateMap() {
        EnumMap<CurrencyCoin, BigDecimal> rateMap = new EnumMap<>(CurrencyCoin.class);
        rateMap.put(CurrencyCoin.usdt, this.getDollarRate(CurrencyCoin.usdt));
        rateMap.put(CurrencyCoin.usdc, this.getDollarRate(CurrencyCoin.usdc));
        rateMap.put(CurrencyCoin.bnb, this.getDollarRate(CurrencyCoin.bnb));
        rateMap.put(CurrencyCoin.eth, this.getDollarRate(CurrencyCoin.eth));
        rateMap.put(CurrencyCoin.trx, this.getDollarRate(CurrencyCoin.trx));
        // 设置其他类型的汇率
        return rateMap;
    }
}
