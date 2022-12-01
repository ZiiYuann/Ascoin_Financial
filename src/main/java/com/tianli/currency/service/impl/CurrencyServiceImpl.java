package com.tianli.currency.service.impl;

import com.tianli.currency.service.CurrencyService;
import com.tianli.currency.service.DigitalCurrencyExchange;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.management.dto.AmountDto;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
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
    public BigDecimal getDollarRate(String coinName) {
       return huobiUsdtRate(coinName);
    }

    @Override
    public BigDecimal calDollarAmount(List<AmountDto> amountDtos) {
        Optional.ofNullable(amountDtos).ifPresent(a -> a.remove(null));
        if (CollectionUtils.isEmpty(amountDtos)) {
            return BigDecimal.ZERO;
        }
        return amountDtos.stream()
                .filter(index -> Objects.nonNull(index.getCoin()))
                .map(amountDto -> {
                    BigDecimal amount = Optional.ofNullable(amountDto.getAmount()).orElse(BigDecimal.ZERO);
                    return this.getDollarRate(amountDto.getCoin()).multiply(amount);
                }).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public BigDecimal huobiUsdtRate(String coinName) {
        Optional.ofNullable(coinName).orElseThrow(NullPointerException::new);

        coinName = coinName.toLowerCase(Locale.ROOT);
        try {
            if ("usdt".equalsIgnoreCase(coinName)) {
                return BigDecimal.ONE;
            }
            if ("usdc".equalsIgnoreCase(coinName)) {
                return BigDecimal.ONE;
            }
            return BigDecimal.valueOf(digitalCurrencyExchange.coinUsdtPrice(coinName));
        } catch (Exception e) {
            throw ErrorCodeEnum.COIN_RATE_ERROR.generalException();
        }
    }
}
