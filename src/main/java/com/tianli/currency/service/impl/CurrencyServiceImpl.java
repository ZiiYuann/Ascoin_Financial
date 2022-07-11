package com.tianli.currency.service.impl;

import com.tianli.currency.dto.DollarAmountDTO;
import com.tianli.currency.enums.CurrencyAdaptType;
import com.tianli.currency.service.CurrencyService;
import com.tianli.exception.ErrorCodeEnum;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-11
 **/
@Service
public class CurrencyServiceImpl implements CurrencyService {

    @Override
    public DollarAmountDTO convertDollarAmount(CurrencyAdaptType currencyAdaptType, BigDecimal amount) {
        amount = Optional.ofNullable(amount).orElse(BigDecimal.ZERO);

        DollarAmountDTO dollarAmountDTO = new DollarAmountDTO();
        // since 2022.07.10 本期功能只支持usdt和usdt，汇率都是1
        BigDecimal dollarRate = this.getDollarRate(currencyAdaptType);
        dollarAmountDTO.setOriginalAmount(amount);
        dollarAmountDTO.setDollarAmount(amount.multiply(dollarRate).setScale(8, RoundingMode.HALF_DOWN));
        return dollarAmountDTO;
    }

    @Override
    public BigDecimal getDollarRate(CurrencyAdaptType currencyAdaptType) {
        switch (currencyAdaptType.getCurrencyType()){
            case usdc:
            case usdt: return BigDecimal.ONE;
            default: break;
        }
        throw ErrorCodeEnum.CURRENCY_NOT_SUPPORT.generalException();
    }
}
