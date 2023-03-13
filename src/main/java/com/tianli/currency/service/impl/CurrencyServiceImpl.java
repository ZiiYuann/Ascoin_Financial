package com.tianli.currency.service.impl;

import com.tianli.chain.service.CoinBaseService;
import com.tianli.currency.service.CurrencyService;
import com.tianli.currency.service.DigitalCurrencyExchange;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.management.dto.AmountDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-11
 **/
@Slf4j
@Service
public class CurrencyServiceImpl implements CurrencyService {

    @Resource
    private DigitalCurrencyExchange digitalCurrencyExchange;
    @Resource
    private CoinBaseService coinBaseService;


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
        coinName = Optional.ofNullable(coinName).orElseThrow(NullPointerException::new);
        coinName = coinName.toLowerCase(Locale.ROOT);
        try {
            return BigDecimal.valueOf(digitalCurrencyExchange.coinUsdtPriceBnb(coinName));
        } catch (Exception e) {
            log.error("币安没有对应货币价格：" + coinName);
        }

        try {
            return BigDecimal.valueOf(digitalCurrencyExchange.coinUsdtPriceOkx(coinName));
        } catch (Exception e) {
            log.error("欧易没有对应货币价格：" + coinName);
        }

        try {
            return BigDecimal.valueOf(digitalCurrencyExchange.coinUsdtPriceHuobi(coinName));
        } catch (Exception e) {
            log.error("huobi没有对应货币价格：" + coinName);
        }

        throw ErrorCodeEnum.COIN_RATE_ERROR.generalException();
    }

    @Override
    public HashMap<String, BigDecimal> rateMap() {
        Set<String> coins = coinBaseService.pushCoinNames();
        var coinRates = new HashMap<String, BigDecimal>();
        coins.forEach(coin -> coinRates.put(coin, this.getDollarRate(coin)));
        return coinRates;
    }

    @Override
    public HashMap<String, BigDecimal> rateMap(Collection<String> coins) {
        final var coinRates = new HashMap<String, BigDecimal>();
        coins.stream().distinct().forEach(coin -> coinRates.put(coin, this.getDollarRate(coin)));
        return coinRates;
    }
}
