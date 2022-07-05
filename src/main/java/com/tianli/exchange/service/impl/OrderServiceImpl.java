package com.tianli.exchange.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tianli.common.CommonFunction;
import com.tianli.common.init.RequestInitService;
import com.tianli.currency.CurrencyTypeEnum;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.currency_token.CurrencyTokenService;
import com.tianli.currency_token.mapper.CurrencyCoinEnum;
import com.tianli.currency_token.mapper.TradeTokenLog;
import com.tianli.currency_token.mapper.TradeTokenLogService;
import com.tianli.currency_token.order.CurrencyTokenOrderService;
import com.tianli.currency_token.order.mapper.CurrencyTokenOrder;
import com.tianli.currency_token.order.mapper.CurrencyTokenOrderStatus;
import com.tianli.currency_token.token.TokenListService;
import com.tianli.currency_token.token.mapper.TokenList;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exchange.dto.*;
import com.tianli.exchange.entity.ExchangeMarketData;
import com.tianli.exchange.enums.PlatformTypeEnum;
import com.tianli.exchange.fatcory.OrderFactory;
import com.tianli.exchange.processor.CoinProcessor;
import com.tianli.exchange.production.ExchangeTradeProduction;
import com.tianli.exchange.push.DepthStream;
import com.tianli.exchange.push.QuotesPush;
import com.tianli.exchange.service.IExchangeMarketDataService;
import com.tianli.exchange.service.IOrderService;
import com.tianli.exchange.vo.DepthVo;
import com.tianli.management.ruleconfig.ConfigConstants;
import com.tianli.mconfig.ConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author lzy
 * @date 2022/5/24 11:42
 */
@Service
public class OrderServiceImpl implements IOrderService {

    @Resource
    OrderFactory orderFactory;

    @Resource
    CurrencyTokenOrderService currencyTokenOrderService;

    @Resource
    RequestInitService requestInitService;

    @Resource
    CurrencyTokenService currencyTokenService;

    @Resource
    ExchangeTradeProduction exchangeTradeProduction;
    @Resource
    IExchangeMarketDataService exchangeMarketDataService;

    @Resource
    CoinProcessor coinProcessor;

    @Resource
    QuotesPush quotesPush;

    @Resource
    ConfigService configService;

    @Resource
    TradeTokenLogService tradeTokenLogService;

    @Resource
    TokenListService tokenListService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void placeOrder(PlaceOrderDTO placeOrderDTO, Long uid) {
        check(placeOrderDTO.getStock());
        orderFactory.placeOrder(placeOrderDTO);
    }

    private void check(String stock) {
        CurrencyCoinEnum currencyCoinEnum = CurrencyCoinEnum.getCurrencyCoinEnum(stock);
        if (ObjectUtil.isNull(currencyCoinEnum)) {
            throw ErrorCodeEnum.ARGUEMENT_ERROR.generalException();
        }
        TokenList tokenList = tokenListService.getByToken(currencyCoinEnum);
        if (ObjectUtil.isNull(tokenList) || !tokenList.getPlatform_token()) {
            throw ErrorCodeEnum.ARGUEMENT_ERROR.generalException();
        }
    }

    @Override
    public void revokeOrder(RevokeOrderDTO revokeOrderDTO) {
        if (ObjectUtil.isNull(revokeOrderDTO)) {
            return;
        }
        if (StrUtil.isNotBlank(revokeOrderDTO.getCoin())) {
            check(revokeOrderDTO.getCoin());
        }
        Long uid = requestInitService.uid();
        List<CurrencyTokenOrder> list = currencyTokenOrderService.list(Wrappers.lambdaQuery(CurrencyTokenOrder.class)
                .eq(CurrencyTokenOrder::getUid, uid)
                .eq(CurrencyTokenOrder::getPlatform_type, PlatformTypeEnum.own)
                .eq(ObjectUtil.isNotNull(revokeOrderDTO.getCoin()), CurrencyTokenOrder::getToken_stock, revokeOrderDTO.getCoin())
                .eq(ObjectUtil.isNotNull(revokeOrderDTO.getOrderId()), CurrencyTokenOrder::getId, revokeOrderDTO.getOrderId())
                .in(CurrencyTokenOrder::getStatus, ListUtil.of(CurrencyTokenOrderStatus.created, CurrencyTokenOrderStatus.partial_deal)));
        if (CollUtil.isNotEmpty(list)) {
            for (CurrencyTokenOrder currencyTokenOrder : list) {
                exchangeTradeProduction.submit(ExchangeCoreRequestDTO.initRevokeOrderTrade(currencyTokenOrder));
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void matchOrder(ExchangeCoreResponseDTO coreResponseDTO) {
        ExchangeCoreResponseConvertDTO coreResponseConvertDTO = ExchangeCoreResponseConvertDTO.get(coreResponseDTO);
        if (ObjectUtil.isNotNull(coreResponseConvertDTO.getCancel_id()) && coreResponseConvertDTO.getCancel_id() > 0) {
            //取消的订单
            revokeOrder(coreResponseConvertDTO);
            return;
        }
        //成交的订单
        makeOrder(coreResponseConvertDTO);
    }

    @Override
    public void depth(ExchangeDepthDTO exchangeDepthDTO) {
        DepthStream depthStream = DepthStream.getDepthStream(exchangeDepthDTO);
        coinProcessor.setDepth(depthStream, exchangeDepthDTO.getSymbol());
        quotesPush.pushDepth(depthStream, exchangeDepthDTO.getSymbol());
    }

    @Override
    public DepthVo queryDepth(String symbol) {
        return coinProcessor.getDepth(symbol);
    }

    private void makeOrder(ExchangeCoreResponseConvertDTO coreResponseDTO) {
        CurrencyTokenOrder buyCurrencyTokenOrder = currencyTokenOrderService.getById(coreResponseDTO.getBuy_id());
        CurrencyTokenOrder sellCurrencyTokenOrder = currencyTokenOrderService.getById(coreResponseDTO.getSell_id());
        if (ObjectUtil.isNull(buyCurrencyTokenOrder) || ObjectUtil.isNull(sellCurrencyTokenOrder)) {
            return;
        }
        BigDecimal quantity = coreResponseDTO.getQuantity();
        BigDecimal price = coreResponseDTO.getPrice();
        BigDecimal tr_amount = quantity.multiply(price);

        buyMakeOrder(buyCurrencyTokenOrder, quantity, tr_amount, coreResponseDTO);
        sellMakeOrder(sellCurrencyTokenOrder, quantity, tr_amount, coreResponseDTO);

        orderFactory.makeOrder(buyCurrencyTokenOrder, coreResponseDTO);
        orderFactory.makeOrder(sellCurrencyTokenOrder, coreResponseDTO);
        //成交记录
        ExchangeMarketData marketData = exchangeMarketDataService.add(coreResponseDTO);
        //k线
        coinProcessor.processTrade(coreResponseDTO);
        //推送逐笔交易
        quotesPush.pushTrade(marketData);
        //推送24Hrk线
        quotesPush.push24HrInfos(coinProcessor.getDayKLinesInfoBySymbol(coreResponseDTO.getSymbol()));
    }

    private void sellMakeOrder(CurrencyTokenOrder sellCurrencyTokenOrder, BigDecimal quantity, BigDecimal tr_amount, ExchangeCoreResponseConvertDTO exchangeCoreResponseDTO) {
        sellCurrencyTokenOrder.setDeal_amount(sellCurrencyTokenOrder.getDeal_amount().add(quantity));
        sellCurrencyTokenOrder.setDeal_tr_amount(sellCurrencyTokenOrder.getDeal_tr_amount().add(tr_amount));
        BigDecimal actualSellRate = Convert.toBigDecimal(configService.getOrDefaultNoCache(ConfigConstants.ACTUAL_SELL_RATE, "0"));
        //扣掉卖出手续费
        BigDecimal sellHandlingFee = tr_amount.multiply(actualSellRate);
        TradeTokenLog sellTradeTokenLog = TradeTokenLog.getSellTradeTokenLog(sellCurrencyTokenOrder, exchangeCoreResponseDTO, sellHandlingFee);
        String generalSn = CommonFunction.generalSn(sellTradeTokenLog.getId());
        //扣除货币
        currencyTokenService.reduce(sellCurrencyTokenOrder.getUid(), CurrencyTypeEnum.actual, sellCurrencyTokenOrder.getToken_stock(), quantity, generalSn, CurrencyLogDes.现货交易);
        //增加U
        currencyTokenService.increase(sellCurrencyTokenOrder.getUid(), CurrencyTypeEnum.actual, sellCurrencyTokenOrder.getToken_fiat(), tr_amount.subtract(sellHandlingFee), generalSn, CurrencyLogDes.现货交易);
        //增加用户成交记录
        tradeTokenLogService.save(sellTradeTokenLog);
    }

    private void buyMakeOrder(CurrencyTokenOrder buyCurrencyTokenOrder, BigDecimal quantity, BigDecimal tr_amount, ExchangeCoreResponseConvertDTO exchangeCoreResponseDTO) {
        buyCurrencyTokenOrder.setDeal_amount(buyCurrencyTokenOrder.getDeal_amount().add(quantity));
        buyCurrencyTokenOrder.setDeal_tr_amount(buyCurrencyTokenOrder.getDeal_tr_amount().add(tr_amount));
        BigDecimal actualBuyRate = Convert.toBigDecimal(configService.getOrDefaultNoCache(ConfigConstants.ACTUAL_BUY_RATE, "0"));
        //扣掉买入手续费
        BigDecimal buyHandlingFee = quantity.multiply(actualBuyRate);
        TradeTokenLog buyTradeTokenLog = TradeTokenLog.getBuyTradeTokenLog(buyCurrencyTokenOrder, exchangeCoreResponseDTO, buyHandlingFee);
        String generalSn = CommonFunction.generalSn(buyTradeTokenLog.getId());
        //扣除U
        currencyTokenService.reduce(buyCurrencyTokenOrder.getUid(), CurrencyTypeEnum.actual, buyCurrencyTokenOrder.getToken_fiat(), tr_amount, generalSn, CurrencyLogDes.现货交易);
        //增加货币
        currencyTokenService.increase(buyCurrencyTokenOrder.getUid(), CurrencyTypeEnum.actual, buyCurrencyTokenOrder.getToken_stock(), quantity.subtract(buyHandlingFee), generalSn, CurrencyLogDes.现货交易);
        //增加用户成交记录
        tradeTokenLogService.save(buyTradeTokenLog);
    }


    private void revokeOrder(ExchangeCoreResponseConvertDTO coreResponseDTO) {
        Long cancelId = coreResponseDTO.getCancel_id();
        CurrencyTokenOrder currencyTokenOrder = currencyTokenOrderService.getById(cancelId);
        if (ObjectUtil.isNull(currencyTokenOrder)) {
            return;
        }
        orderFactory.revokeOrder(currencyTokenOrder, coreResponseDTO);
    }
}
