package com.tianli.exchange.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.tianli.common.init.RequestInitService;
import com.tianli.currency.CurrencyTypeEnum;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.currency.log.CurrencyLogType;
import com.tianli.currency_token.CurrencyTokenLogService;
import com.tianli.currency_token.CurrencyTokenService;
import com.tianli.currency_token.mapper.CurrencyCoinEnum;
import com.tianli.currency_token.mapper.CurrencyToken;
import com.tianli.currency_token.mapper.CurrencyTokenLog;
import com.tianli.currency_token.mapper.TradeDirectionEnum;
import com.tianli.currency_token.order.CurrencyTokenOrderService;
import com.tianli.currency_token.order.mapper.CurrencyTokenOrder;
import com.tianli.currency_token.order.mapper.CurrencyTokenOrderStatus;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exchange.dto.ExchangeCoreRequestDTO;
import com.tianli.exchange.dto.ExchangeCoreResponseConvertDTO;
import com.tianli.exchange.dto.PlaceOrderDTO;
import com.tianli.exchange.processor.CoinProcessor;
import com.tianli.exchange.production.ExchangeTradeProduction;
import com.tianli.exchange.service.OrderStrategy;
import com.tianli.exchange.vo.DepthVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author lzy
 * @date 2022/5/24 14:02
 */
@Service("marketOrderService")
public class MarketOrderService implements OrderStrategy {

    @Resource
    CoinProcessor coinProcessor;

    @Resource
    CurrencyTokenService currencyTokenService;

    @Resource
    RequestInitService requestInitService;

    @Resource
    CurrencyTokenOrderService currencyTokenOrderService;

    @Resource
    ExchangeTradeProduction exchangeTradeProduction;

    @Resource
    CurrencyTokenLogService currencyTokenLogService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void placeOrder(PlaceOrderDTO placeOrderDTO) {
        DepthVo depth = coinProcessor.getDepth(placeOrderDTO.getStock().toUpperCase() + placeOrderDTO.getFiat().toUpperCase());
        if (ObjectUtil.isNull(depth)) {
            throw ErrorCodeEnum.NO_OPPONENT_PLATE.generalException();
        }
        CurrencyTokenOrder marketOrder = placeOrderDTO.getMarketOrder(requestInitService.uid());
        ExchangeCoreRequestDTO exchangeCoreRequestDTO;
        if (marketOrder.getDirection().equals(TradeDirectionEnum.buy)) {
            exchangeCoreRequestDTO = getBuyCor(marketOrder);
        } else {
            exchangeCoreRequestDTO = getSellCor(marketOrder);
        }
        boolean save = currencyTokenOrderService.save(marketOrder);
        if (!save) {
            throw ErrorCodeEnum.SYSTEM_BUSY.generalException();
        }
        exchangeTradeProduction.submit(exchangeCoreRequestDTO);
    }

    @Override
    public void makeOrder(CurrencyTokenOrder currencyTokenOrder, ExchangeCoreResponseConvertDTO coreResponseDTO) {
        CurrencyTokenLog currencyTokenLog = currencyTokenLogService.findBySn(currencyTokenOrder.getId().toString(), currencyTokenOrder.getUid(), CurrencyLogType.freeze, CurrencyTypeEnum.actual);
        //下单时冻结的金额
        BigDecimal amount = currencyTokenLog.getAmount();
        if (currencyTokenOrder.getDirection().equals(TradeDirectionEnum.buy)) {
            if (currencyTokenOrder.getMarket_price_type().equals(0)) {
                if (currencyTokenOrder.getDeal_amount().compareTo(currencyTokenOrder.getAmount()) >= 0 || currencyTokenOrder.getDeal_tr_amount().compareTo(amount) >= 0) {
                    trSuccess(currencyTokenOrder, amount, currencyTokenOrder.getDeal_tr_amount(), currencyTokenOrder.getToken_fiat());
                }
            } else {
                if (currencyTokenOrder.getDeal_tr_amount().compareTo(currencyTokenOrder.getTr_amount()) >= 0) {
                    trSuccess(currencyTokenOrder, amount, currencyTokenOrder.getDeal_tr_amount(), currencyTokenOrder.getToken_fiat());
                }
            }
        } else {
            if (currencyTokenOrder.getMarket_price_type().equals(0)) {
                if (currencyTokenOrder.getDeal_amount().compareTo(currencyTokenOrder.getAmount()) >= 0) {
                    trSuccess(currencyTokenOrder, amount, currencyTokenOrder.getDeal_amount(), currencyTokenOrder.getToken_stock());
                }
            } else {
                if (currencyTokenOrder.getDeal_amount().compareTo(amount) >= 0 || currencyTokenOrder.getDeal_tr_amount().compareTo(currencyTokenOrder.getTr_amount()) >= 0) {
                    trSuccess(currencyTokenOrder, amount, currencyTokenOrder.getDeal_amount(), currencyTokenOrder.getToken_stock());
                }
            }
        }
        currencyTokenOrderService.updateById(currencyTokenOrder);
    }

    private void trSuccess(CurrencyTokenOrder currencyTokenOrder, BigDecimal amount, BigDecimal deal_amount, CurrencyCoinEnum token_stock) {
        currencyTokenOrder.setStatus(CurrencyTokenOrderStatus.success);
        currencyTokenOrder.setDeal_time(LocalDateTime.now());
        currencyTokenOrder.setDeal_time_ms(System.currentTimeMillis());
        currencyTokenOrder.setUpdate_time(LocalDateTime.now());
        BigDecimal thawAmount = amount.subtract(deal_amount);
        if (thawAmount.compareTo(BigDecimal.ZERO) > 0) {
            currencyTokenService.unfreeze(currencyTokenOrder.getUid(), CurrencyTypeEnum.actual, token_stock, thawAmount, currencyTokenOrder.getId().toString(), CurrencyLogDes.现货交易);
        }
    }

    private ExchangeCoreRequestDTO getSellCor(CurrencyTokenOrder marketOrder) {
        ExchangeCoreRequestDTO exchangeCoreRequestDTO;
        CurrencyToken currencyToken = currencyTokenService._get(requestInitService.uid(), CurrencyTypeEnum.actual, marketOrder.getToken_stock());
        BigDecimal remain = currencyToken.getRemain();
        if (remain.compareTo(BigDecimal.ZERO) <= 0) {
            throw ErrorCodeEnum.INSUFFICIENT_BALANCE.generalException();
        }
        if (marketOrder.getMarket_price_type().equals(0) && remain.compareTo(marketOrder.getAmount()) < 0) {
            throw ErrorCodeEnum.INSUFFICIENT_BALANCE.generalException();
        }
        //冻结全部币
        currencyTokenService.freeze(marketOrder.getUid(), CurrencyTypeEnum.actual, marketOrder.getToken_stock(), remain, marketOrder.getId().toString(), CurrencyLogDes.现货交易);
        exchangeCoreRequestDTO = ExchangeCoreRequestDTO.initMarketSellOrder(marketOrder, remain);
        return exchangeCoreRequestDTO;
    }

    private ExchangeCoreRequestDTO getBuyCor(CurrencyTokenOrder marketOrder) {
        ExchangeCoreRequestDTO exchangeCoreRequestDTO;
        CurrencyToken currencyToken = currencyTokenService._get(requestInitService.uid(), CurrencyTypeEnum.actual, marketOrder.getToken_fiat());
        BigDecimal remain = currencyToken.getRemain();
        if (remain.compareTo(BigDecimal.ZERO) <= 0) {
            throw ErrorCodeEnum.INSUFFICIENT_BALANCE.generalException();
        }
        if (marketOrder.getMarket_price_type().equals(1) && remain.compareTo(marketOrder.getTr_amount()) < 0) {
            throw ErrorCodeEnum.INSUFFICIENT_BALANCE.generalException();
        }
        //冻结全部u
        currencyTokenService.freeze(marketOrder.getUid(), CurrencyTypeEnum.actual, marketOrder.getToken_fiat(), remain, marketOrder.getId().toString(), CurrencyLogDes.现货交易);
        //发送撮合系统
        exchangeCoreRequestDTO = ExchangeCoreRequestDTO.initMarketBuyOrder(marketOrder, remain);
        return exchangeCoreRequestDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void revokeOrder(CurrencyTokenOrder currencyTokenOrder, ExchangeCoreResponseConvertDTO coreResponseDTO) {
        CurrencyTokenLog currencyTokenLog = currencyTokenLogService.findBySn(currencyTokenOrder.getId().toString(), currencyTokenOrder.getUid(), CurrencyLogType.freeze, CurrencyTypeEnum.actual);
        BigDecimal amount = currencyTokenLog.getAmount();
        BigDecimal deal_tr_amount = currencyTokenOrder.getDeal_tr_amount();
        BigDecimal deal_amount = currencyTokenOrder.getDeal_amount();
        CurrencyTokenOrderStatus currencyTokenOrderStatus;
        if (currencyTokenOrder.getDirection().equals(TradeDirectionEnum.buy)) {
            currencyTokenOrderStatus = deal_tr_amount.compareTo(BigDecimal.ZERO) > 0 ? CurrencyTokenOrderStatus.canceled_partial_deal : CurrencyTokenOrderStatus.canceled;
            BigDecimal thawAmount = amount.subtract(deal_tr_amount);
            currencyTokenService.unfreeze(currencyTokenOrder.getUid(), CurrencyTypeEnum.actual, currencyTokenOrder.getToken_fiat(), thawAmount, currencyTokenOrder.getId().toString(), CurrencyLogDes.现货交易);
        } else {
            currencyTokenOrderStatus = deal_amount.compareTo(BigDecimal.ZERO) > 0 ? CurrencyTokenOrderStatus.canceled_partial_deal : CurrencyTokenOrderStatus.canceled;
            BigDecimal thawAmount = amount.subtract(deal_amount);
            currencyTokenService.unfreeze(currencyTokenOrder.getUid(), CurrencyTypeEnum.actual, currencyTokenOrder.getToken_stock(), thawAmount, currencyTokenOrder.getId().toString(), CurrencyLogDes.现货交易);
        }
        currencyTokenOrder.setUpdate_time(LocalDateTime.now());
        currencyTokenOrder.setStatus(currencyTokenOrderStatus);
        currencyTokenOrderService.updateById(currencyTokenOrder);
    }
}
