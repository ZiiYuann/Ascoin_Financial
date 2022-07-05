package com.tianli.exchange.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.tianli.common.init.RequestInitService;
import com.tianli.currency.CurrencyTypeEnum;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.currency_token.CurrencyTokenService;
import com.tianli.currency_token.mapper.TradeDirectionEnum;
import com.tianli.currency_token.order.CurrencyTokenOrderService;
import com.tianli.currency_token.order.mapper.CurrencyTokenOrder;
import com.tianli.currency_token.order.mapper.CurrencyTokenOrderStatus;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exchange.dto.ExchangeCoreRequestDTO;
import com.tianli.exchange.dto.ExchangeCoreResponseConvertDTO;
import com.tianli.exchange.dto.PlaceOrderDTO;
import com.tianli.exchange.production.ExchangeTradeProduction;
import com.tianli.exchange.service.OrderStrategy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author lzy
 * @date 2022/5/24 14:01
 * 限价单类型
 */
@Service("limitOrderService")
public class LimitOrderService implements OrderStrategy {

    @Resource
    CurrencyTokenService currencyTokenService;

    @Resource
    CurrencyTokenOrderService currencyTokenOrderService;

    @Resource
    RequestInitService requestInitService;


    @Resource
    ExchangeTradeProduction exchangeTradeProduction;


    @Override
    public void placeOrder(PlaceOrderDTO placeOrderDTO) {
        CurrencyTokenOrder currencyTokenOrder = placeOrderDTO.getLimitOrder(requestInitService.uid());
        //冻结资金
        if (StrUtil.equals(currencyTokenOrder.getDirection().name(), TradeDirectionEnum.buy.name())) {
            currencyTokenService.freeze(currencyTokenOrder.getUid(), CurrencyTypeEnum.actual, currencyTokenOrder.getToken_fiat(), currencyTokenOrder.getTr_amount(), currencyTokenOrder.getId().toString(), CurrencyLogDes.现货交易);
        } else {
            currencyTokenService.freeze(currencyTokenOrder.getUid(), CurrencyTypeEnum.actual, currencyTokenOrder.getToken_stock(), currencyTokenOrder.getAmount(), currencyTokenOrder.getId().toString(), CurrencyLogDes.现货交易);
        }
        boolean save = currencyTokenOrderService.save(currencyTokenOrder);
        if (!save) {
            throw ErrorCodeEnum.SYSTEM_BUSY.generalException();
        }
        exchangeTradeProduction.submit(ExchangeCoreRequestDTO.initTrade(currencyTokenOrder));
    }

    @Override
    public void makeOrder(CurrencyTokenOrder currencyTokenOrder, ExchangeCoreResponseConvertDTO coreResponseDTO) {
        if (currencyTokenOrder.getDeal_amount().compareTo(currencyTokenOrder.getAmount()) >= 0) {
            currencyTokenOrder.setStatus(CurrencyTokenOrderStatus.success);
            currencyTokenOrder.setDeal_time(LocalDateTime.now());
            currencyTokenOrder.setDeal_time_ms(System.currentTimeMillis());
            //有剩余未花完的钱 返回账户
            if (StrUtil.equals(currencyTokenOrder.getDirection().name(), TradeDirectionEnum.buy.name())) {
                BigDecimal subtract = currencyTokenOrder.getTr_amount().subtract(currencyTokenOrder.getDeal_tr_amount());
                if (subtract.compareTo(BigDecimal.ZERO) > 0) {
                    currencyTokenService.unfreeze(currencyTokenOrder.getUid(), CurrencyTypeEnum.actual, currencyTokenOrder.getToken_fiat(), subtract, currencyTokenOrder.getId().toString(), CurrencyLogDes.现货交易);
                }
            }
        } else {
            currencyTokenOrder.setStatus(CurrencyTokenOrderStatus.partial_deal);
        }
        currencyTokenOrder.setUpdate_time(LocalDateTime.now());
        currencyTokenOrderService.updateById(currencyTokenOrder);
    }

    @Override
    public void revokeOrder(CurrencyTokenOrder currencyTokenOrder, ExchangeCoreResponseConvertDTO coreResponseDTO) {
        if (ObjectUtil.equal(currencyTokenOrder.getDirection(), TradeDirectionEnum.buy)) {
            BigDecimal unAmount = currencyTokenOrder.getTr_amount().subtract(currencyTokenOrder.getDeal_tr_amount());
            currencyTokenService.unfreeze(currencyTokenOrder.getUid(), CurrencyTypeEnum.actual, currencyTokenOrder.getToken_fiat(), unAmount, currencyTokenOrder.getId().toString(), CurrencyLogDes.现货交易);
            currencyTokenOrder.setStatus(currencyTokenOrder.getDeal_tr_amount().compareTo(BigDecimal.ZERO) == 0 ? CurrencyTokenOrderStatus.canceled : CurrencyTokenOrderStatus.canceled_partial_deal);
        } else {
            BigDecimal quantity = coreResponseDTO.getQuantity();
            currencyTokenService.unfreeze(currencyTokenOrder.getUid(), CurrencyTypeEnum.actual, currencyTokenOrder.getToken_stock(), quantity, currencyTokenOrder.getId().toString(), CurrencyLogDes.现货交易);
            currencyTokenOrder.setStatus(currencyTokenOrder.getAmount().compareTo(quantity) == 0 ? CurrencyTokenOrderStatus.canceled : CurrencyTokenOrderStatus.canceled_partial_deal);
        }
        currencyTokenOrder.setUpdate_time(LocalDateTime.now());
        currencyTokenOrderService.updateById(currencyTokenOrder);
    }
}
