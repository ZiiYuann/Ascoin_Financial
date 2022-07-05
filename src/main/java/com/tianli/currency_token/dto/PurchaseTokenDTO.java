package com.tianli.currency_token.dto;

import com.tianli.currency_token.mapper.CurrencyCoinEnum;
import com.tianli.currency_token.mapper.TokenOrderType;
import com.tianli.currency_token.mapper.TradeDirectionEnum;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;

@Data
public class PurchaseTokenDTO {
    private CurrencyCoinEnum fiat;
    private CurrencyCoinEnum stock;
//    private BigDecimal fiat_amount;
//    private BigDecimal stock_amount;
    @DecimalMin(value = "0.000001", message = "购买金额不能为空")
    private BigDecimal amount;
    private CurrencyCoinEnum amount_unit;
    private TradeDirectionEnum direction;
    private TokenOrderType type;
    private BigDecimal limit_price;
}
