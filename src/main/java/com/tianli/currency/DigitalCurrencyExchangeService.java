package com.tianli.currency;

import com.tianli.currency.enums.CurrencyAdaptType;
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

    public double exchange(CurrencyAdaptType source, CurrencyAdaptType dest) {
        if(source.equals(CurrencyAdaptType.usdt_trc20)){
            source = CurrencyAdaptType.usdt_erc20;
        }
        if (source.equals(CurrencyAdaptType.usdt_omni)) source = CurrencyAdaptType.usdt_erc20;
        if (dest.equals(CurrencyAdaptType.usdt_omni)) dest = CurrencyAdaptType.usdt_erc20;
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
        Map<CurrencyAdaptType, DoubleSupplier> eth = new HashMap<>();
        eth.put(CurrencyAdaptType.btc, digitalCurrencyExchangeComponent::ethBtcPrice);
        eth.put(CurrencyAdaptType.cny, digitalCurrencyExchangeComponent::ethCnyPrice);
        eth.put(CurrencyAdaptType.usdt_erc20, digitalCurrencyExchangeComponent::ethUsdtPrice);
        EXCHANGE_RATE_FUNCTION.put(CurrencyAdaptType.eth, eth);
        Map<CurrencyAdaptType, DoubleSupplier> btc = new HashMap<>();
        btc.put(CurrencyAdaptType.cny, digitalCurrencyExchangeComponent::btcCnyPrice);
        btc.put(CurrencyAdaptType.usdt_erc20, digitalCurrencyExchangeComponent::btcUsdtPrice);
        EXCHANGE_RATE_FUNCTION.put(CurrencyAdaptType.btc, btc);
        Map<CurrencyAdaptType, DoubleSupplier> usdt = new HashMap<>();
        usdt.put(CurrencyAdaptType.cny, digitalCurrencyExchangeComponent::usdtCnyPrice);
        EXCHANGE_RATE_FUNCTION.put(CurrencyAdaptType.usdt_erc20, usdt);
        EXCHANGE_RATE_FUNCTION.put(CurrencyAdaptType.usdt_trc20, usdt);
    }

    @Resource
    private DigitalCurrencyExchangeComponent digitalCurrencyExchangeComponent;
    private static final Map<CurrencyAdaptType, Map<CurrencyAdaptType, DoubleSupplier>> EXCHANGE_RATE_FUNCTION = new HashMap<>();
}
