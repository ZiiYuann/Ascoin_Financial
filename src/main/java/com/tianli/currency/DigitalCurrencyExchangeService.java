package com.tianli.currency;

import com.tianli.exception.ErrorCodeEnum;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.function.DoubleSupplier;

/**
 * @Author wangqiyun
 * @Date 2020/3/12 17:25
 */

@Service
public class DigitalCurrencyExchangeService {

    public double exchange(TokenCurrencyType source, TokenCurrencyType dest) {
        if(source.equals(TokenCurrencyType.usdt_trc20)){
            source = TokenCurrencyType.usdt_erc20;
        }
        if (source.equals(TokenCurrencyType.usdt_omni)) source = TokenCurrencyType.usdt_erc20;
        if (dest.equals(TokenCurrencyType.usdt_omni)) dest = TokenCurrencyType.usdt_erc20;
        if (source.equals(dest)) return 1.0;
        if (EXCHANGE_RATE_FUNCTION.containsKey(source) && EXCHANGE_RATE_FUNCTION.get(source).containsKey(dest)) {
            return EXCHANGE_RATE_FUNCTION.get(source).get(dest).getAsDouble();
        } else if (EXCHANGE_RATE_FUNCTION.containsKey(dest) && EXCHANGE_RATE_FUNCTION.get(dest).containsKey(source)) {
            return 1.0 / EXCHANGE_RATE_FUNCTION.get(dest).get(source).getAsDouble();
        }
        ErrorCodeEnum.NOT_OPEN.throwException();
        return 0.0;
    }

    @PostConstruct
    public void init() {
        Map<TokenCurrencyType, DoubleSupplier> eth = new HashMap<>();
        eth.put(TokenCurrencyType.btc, digitalCurrencyExchangeComponent::ethBtcPrice);
        eth.put(TokenCurrencyType.cny, digitalCurrencyExchangeComponent::ethCnyPrice);
        eth.put(TokenCurrencyType.usdt_erc20, digitalCurrencyExchangeComponent::ethUsdtPrice);
        EXCHANGE_RATE_FUNCTION.put(TokenCurrencyType.eth, eth);
        Map<TokenCurrencyType, DoubleSupplier> btc = new HashMap<>();
        btc.put(TokenCurrencyType.cny, digitalCurrencyExchangeComponent::btcCnyPrice);
        btc.put(TokenCurrencyType.usdt_erc20, digitalCurrencyExchangeComponent::btcUsdtPrice);
        EXCHANGE_RATE_FUNCTION.put(TokenCurrencyType.btc, btc);
        Map<TokenCurrencyType, DoubleSupplier> usdt = new HashMap<>();
        usdt.put(TokenCurrencyType.cny, digitalCurrencyExchangeComponent::usdtCnyPrice);
        EXCHANGE_RATE_FUNCTION.put(TokenCurrencyType.usdt_erc20, usdt);
        EXCHANGE_RATE_FUNCTION.put(TokenCurrencyType.usdt_trc20, usdt);
    }

    @Resource
    private DigitalCurrencyExchangeComponent digitalCurrencyExchangeComponent;
    private static final Map<TokenCurrencyType, Map<TokenCurrencyType, DoubleSupplier>> EXCHANGE_RATE_FUNCTION = new HashMap<>();
}
