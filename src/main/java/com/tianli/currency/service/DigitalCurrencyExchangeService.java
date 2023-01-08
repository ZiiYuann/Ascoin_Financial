package com.tianli.currency.service;

import com.tianli.currency.enums.TokenAdapter;
import com.tianli.currency.service.DigitalCurrencyExchange;
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

    @PostConstruct
    public void init() {

    }

    @Resource
    private DigitalCurrencyExchange digitalCurrencyExchangeComponent;
    private static final Map<TokenAdapter, Map<TokenAdapter, DoubleSupplier>> EXCHANGE_RATE_FUNCTION = new HashMap<>();
}
