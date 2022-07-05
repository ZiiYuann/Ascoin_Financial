package com.tianli.exchange.dto;

import cn.hutool.core.util.ObjectUtil;
import com.tianli.common.CommonFunction;
import com.tianli.currency_token.mapper.CurrencyCoinEnum;
import com.tianli.currency_token.mapper.TokenOrderType;
import com.tianli.currency_token.mapper.TradeDirectionEnum;
import com.tianli.currency_token.order.mapper.CurrencyTokenOrder;
import com.tianli.currency_token.order.mapper.CurrencyTokenOrderStatus;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exchange.enums.PlatformTypeEnum;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author lzy
 * @date 2022/5/24 11:46
 */
@Data
public class PlaceOrderDTO {
    /**
     * 法币
     */
    @NotBlank(message = "参数错误")
    private String fiat;
    /**
     * 货币
     */
    @NotBlank(message = "参数错误")
    private String stock;
    /**
     * 使用 quantity 的市价单 MARKET 明确的是用户想用市价单买入或卖出的数量。
     * 比如在BTCUSDT上下一个市价单, quantity用户指明能够买进或者卖出多少BTC。
     */
    @DecimalMin(value = "0.001", message = "购买数量太小")
    @Digits(integer = 16, fraction = 3, message = "参数错误")
    private BigDecimal quantity;
    /**
     * 使用 quoteOrderQty 的市价单MARKET 明确的是通过买入(或卖出)想要花费(或获取)的报价资产数量; 此时的正确报单数量将会以市场流动性和quoteOrderQty被计算出来。
     * 以BTCUSDT为例, quoteOrderQty=100:
     * 下买单的时候, 订单会尽可能的买进价值100USDT的BTC.
     * 下卖单的时候, 订单会尽可能的卖出价值100USDT的BTC.
     */
    @DecimalMin(value = "0.001", message = "购买数量太小")
    @Digits(integer = 16, fraction = 3, message = "参数错误")
    private BigDecimal quoteOrderQty;
    /**
     * 订单方向
     */
    @NotNull(message = "参数错误")
    private TradeDirectionEnum direction;
    /**
     * 订单类型
     */
    @NotNull(message = "参数错误")
    private TokenOrderType type;
    /**
     * 价格
     */
    @DecimalMin(value = "0.001", message = "购买数量太小")
    @Digits(integer = 16, fraction = 3, message = "参数错误")
    private BigDecimal price;

    /**
     * 获取限价单
     *
     * @return
     */
    public CurrencyTokenOrder getLimitOrder(Long uid) {
        if ((ObjectUtil.isNull(price) || price.compareTo(BigDecimal.ZERO) <= 0)
                || (ObjectUtil.isNull(quantity) || quantity.compareTo(BigDecimal.ZERO) <= 0)) {
            throw ErrorCodeEnum.ARGUEMENT_ERROR.generalException();
        }
        return CurrencyTokenOrder.builder()
                .id(CommonFunction.generalId())
                .type(TokenOrderType.limit)
                .uid(uid)
                .token_fiat(CurrencyCoinEnum.getCurrencyCoinEnum(fiat))
                .token_stock(CurrencyCoinEnum.getCurrencyCoinEnum(stock))
                .price(price)
                .direction(direction)
                .amount(quantity)
                .amount_unit(CurrencyCoinEnum.getCurrencyCoinEnum(stock))
                .deal_amount(BigDecimal.ZERO)
                .status(CurrencyTokenOrderStatus.created)
                .tr_amount(price.multiply(quantity))
                .deal_tr_amount(BigDecimal.ZERO)
                .create_time(LocalDateTime.now())
                .create_time_ms(System.currentTimeMillis())
                .platform_type(PlatformTypeEnum.own)
                .build();
    }

    /**
     * 获取市价单
     *
     * @param uid
     * @return
     */
    public CurrencyTokenOrder getMarketOrder(Long uid) {
        if (ObjectUtil.isNull(quantity) && ObjectUtil.isNull(quoteOrderQty)) {
            throw ErrorCodeEnum.ARGUEMENT_ERROR.generalException();
        }
        BigDecimal amount = BigDecimal.ZERO;
        BigDecimal tr_amount = BigDecimal.ZERO;
        int market_price_type;
        if (ObjectUtil.isNotNull(quantity)) {
            amount = quantity;
            market_price_type = 0;
        } else {
            tr_amount = quoteOrderQty;
            market_price_type = 1;
        }
        return CurrencyTokenOrder.builder()
                .id(CommonFunction.generalId())
                .type(TokenOrderType.market)
                .uid(uid)
                .token_fiat(CurrencyCoinEnum.getCurrencyCoinEnum(fiat))
                .token_stock(CurrencyCoinEnum.getCurrencyCoinEnum(stock))
                //市价单价格没有意义
                .price(price)
                .direction(direction)
                .amount(amount)
                .amount_unit(CurrencyCoinEnum.getCurrencyCoinEnum(stock))
                .deal_amount(BigDecimal.ZERO)
                .status(CurrencyTokenOrderStatus.created)
                .tr_amount(tr_amount)
                .deal_tr_amount(BigDecimal.ZERO)
                .create_time(LocalDateTime.now())
                .create_time_ms(System.currentTimeMillis())
                .platform_type(PlatformTypeEnum.own)
                .market_price_type(market_price_type)
                .build();

    }
}
