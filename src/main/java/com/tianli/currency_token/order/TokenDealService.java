package com.tianli.currency_token.order;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tianli.common.CommonFunction;
import com.tianli.common.init.RequestInitService;
import com.tianli.currency.CurrencyTypeEnum;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.currency.log.CurrencyLogType;
import com.tianli.currency_token.CurrencyTokenLogService;
import com.tianli.currency_token.CurrencyTokenService;
import com.tianli.currency_token.mapper.*;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exchange.entity.KLinesInfo;
import com.tianli.exchange.processor.CoinProcessor;
import com.tianli.kline.KLineService;
import com.tianli.management.ruleconfig.ConfigConstants;
import com.tianli.mconfig.ConfigService;
import com.tianli.tool.BianPriceCache;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;

@Service
public class TokenDealService {

    @Transactional
    public void limitBuy(Long uid, CurrencyCoinEnum fiat, CurrencyCoinEnum stock, BigDecimal fiat_amount, BigDecimal stock_amount, BigDecimal price, Long orderId) {
        long id = CommonFunction.generalId();
        String sn = CommonFunction.generalSn(id);

        BigDecimal buyRate = new BigDecimal(configService.get(ConfigConstants.ACTUAL_BUY_RATE));
        BigDecimal fee = stock_amount.multiply(buyRate);
        BigDecimal stock_get_actual = stock_amount.subtract(fee);
        CurrencyTokenLog log = currencyTokenLogService.getOne(
                new LambdaQueryWrapper<CurrencyTokenLog>().eq(CurrencyTokenLog::getSn, orderId.toString())
                        .eq(CurrencyTokenLog::getLog_type, CurrencyLogType.freeze)
        );

        currencyTokenService.unfreeze(uid, CurrencyTypeEnum.actual, fiat, log.getAmount(), sn, CurrencyLogDes.现货交易);
        currencyTokenService.decrease(uid, CurrencyTypeEnum.actual, fiat, fiat_amount, sn, CurrencyLogDes.现货交易);
        currencyTokenService.increase(uid, CurrencyTypeEnum.actual, stock, stock_get_actual, sn, CurrencyLogDes.现货交易);

        TradeTokenLog tradeTokenLog = TradeTokenLog.builder().token_fee(stock).token_fee_amount(fee)
                .token_fiat(fiat).token_out(fiat).token_out_amount(fiat_amount)
                .token_stock(stock).token_in(stock).token_in_amount(stock_get_actual)
                .create_time_ms(requestInitService.now_ms())
                .create_time(requestInitService.now()).direction(TradeDirectionEnum.buy)
                .id(id).uid(uid).price(price)
                .order_id(orderId)
                .build();

        tradeTokenLogService.save(tradeTokenLog);
    }


    @Transactional
    public void limitSell(Long uid, CurrencyCoinEnum fiat, CurrencyCoinEnum stock, BigDecimal fiat_amount, BigDecimal stock_amount, BigDecimal price, Long orderId) {
        long id = CommonFunction.generalId();
        String sn = CommonFunction.generalSn(id);
        BigDecimal sellRate = new BigDecimal(configService.get(ConfigConstants.ACTUAL_SELL_RATE));

        BigDecimal fee = fiat_amount.multiply(sellRate);
        BigDecimal fiat_get_actual = fiat_amount.subtract(fee);

        CurrencyTokenLog log = currencyTokenLogService.getOne(
                new LambdaQueryWrapper<CurrencyTokenLog>().eq(CurrencyTokenLog::getSn, orderId.toString())
                        .eq(CurrencyTokenLog::getLog_type, CurrencyLogType.freeze)
        );

        currencyTokenService.unfreeze(uid, CurrencyTypeEnum.actual, stock, log.getAmount(), sn, CurrencyLogDes.现货交易);
        currencyTokenService.decrease(uid, CurrencyTypeEnum.actual, stock, stock_amount, sn, CurrencyLogDes.现货交易);
        currencyTokenService.increase(uid, CurrencyTypeEnum.actual, fiat, fiat_get_actual, sn, CurrencyLogDes.现货交易);

        TradeTokenLog tradeTokenLog = TradeTokenLog.builder().token_fee(fiat).token_fee_amount(fee)
                .token_fiat(fiat).token_in(fiat).token_in_amount(fiat_get_actual)
                .token_stock(stock).token_out(stock).token_out_amount(stock_amount)
                .create_time_ms(requestInitService.now_ms())
                .create_time(requestInitService.now()).direction(TradeDirectionEnum.sell)
                .id(id).uid(uid).price(price)
                .order_id(orderId)
                .build();

        tradeTokenLogService.save(tradeTokenLog);
    }

    @Transactional
    public void marketBuy(Long uid, CurrencyCoinEnum fiat, CurrencyCoinEnum stock, BigDecimal fiat_amount, BigDecimal stock_amount, BigDecimal price, Long orderId) {
        long id = CommonFunction.generalId();
        String sn = CommonFunction.generalSn(id);

        BigDecimal buyRate = new BigDecimal(configService.get(ConfigConstants.ACTUAL_BUY_RATE));
        BigDecimal fee = stock_amount.multiply(buyRate);
        BigDecimal stock_get_actual = stock_amount.subtract(fee);

        currencyTokenService.decrease(uid, CurrencyTypeEnum.actual, fiat, fiat_amount, sn, CurrencyLogDes.现货交易);
        currencyTokenService.increase(uid, CurrencyTypeEnum.actual, stock, stock_get_actual, sn, CurrencyLogDes.现货交易);

        TradeTokenLog tradeTokenLog = TradeTokenLog.builder().token_fee(stock).token_fee_amount(fee)
                .token_fiat(fiat).token_out(fiat).token_out_amount(fiat_amount)
                .token_stock(stock).token_in(stock).token_in_amount(stock_get_actual)
                .create_time_ms(requestInitService.now_ms())
                .create_time(requestInitService.now()).create_time_ms(requestInitService.now_ms())
                .direction(TradeDirectionEnum.buy)
                .id(id).uid(uid).price(price)
                .order_id(orderId)
                .build();

        tradeTokenLogService.save(tradeTokenLog);
    }


    @Transactional
    public void marketSell(Long uid, CurrencyCoinEnum fiat, CurrencyCoinEnum stock, BigDecimal fiat_amount, BigDecimal stock_amount, BigDecimal price, Long orderId) {
        long id = CommonFunction.generalId();
        String sn = CommonFunction.generalSn(id);
        BigDecimal sellRate = new BigDecimal(configService.get(ConfigConstants.ACTUAL_SELL_RATE));

        BigDecimal fee = fiat_amount.multiply(sellRate);
        BigDecimal fiat_get_actual = fiat_amount.subtract(fee);

        currencyTokenService.decrease(uid, CurrencyTypeEnum.actual, stock, stock_amount, sn, CurrencyLogDes.现货交易);
        currencyTokenService.increase(uid, CurrencyTypeEnum.actual, fiat, fiat_get_actual, sn, CurrencyLogDes.现货交易);

        TradeTokenLog tradeTokenLog = TradeTokenLog.builder().token_fee(fiat).token_fee_amount(fee)
                .token_fiat(fiat).token_in(fiat).token_in_amount(fiat_get_actual)
                .token_stock(stock).token_out(stock).token_out_amount(stock_amount)
                .create_time_ms(requestInitService.now_ms())
                .create_time(requestInitService.now()).direction(TradeDirectionEnum.sell)
                .id(id).uid(uid).price(price)
                .order_id(orderId)
                .build();

        tradeTokenLogService.save(tradeTokenLog);
    }


//    public BigDecimal getBianPrice(CurrencyCoinEnum fiat, CurrencyCoinEnum stock) {
//        if(fiat.equals(stock)) return BigDecimal.ONE;
//        String symbol = stock.getName().toUpperCase() + fiat.getName().toUpperCase();
//        BoundValueOperations<String, String> priceOps = redisTemplate.boundValueOps(symbol + ".price");
//        String o = priceOps.get();
//        if(o == null) {
//            o = kLineService.getCurrentBianPrice(symbol).toString();
//            priceOps.set(o, 5, TimeUnit.SECONDS);
//        }
//        return new BigDecimal(o);
//    }

    public BigDecimal getBianPrice(CurrencyCoinEnum fiat, CurrencyCoinEnum stock) {
        if (fiat.equals(stock)) return BigDecimal.ONE;
        String symbol = stock.getName().toUpperCase() + fiat.getName().toUpperCase();
        //如果redis有价格代表是平台币
        KLinesInfo kLinesInfo = coinProcessor.getDayKLinesInfoBySymbol(symbol);
        if (ObjectUtil.isNotNull(kLinesInfo)) {
            return kLinesInfo.getClosing_price();
        }
        Double price = BianPriceCache.getLatestPrice(symbol);
        if (price == null) {
            price = kLineService.getCurrentBianPrice(symbol).doubleValue();
            BianPriceCache.setLatestBianPrice(symbol, price);
        }
        if (price == 0) {
            BianPriceCache.deleteLatestBianPrice(symbol);
            throw ErrorCodeEnum.SYSTEM_BUSY.generalException();
        }
        return new BigDecimal(price).setScale(10, RoundingMode.HALF_UP);
    }

    public void syncBianPrice() {
        JsonArray jsonArray = kLineService.getCurrentBianPriceList();
        for (JsonElement jsonElement : jsonArray) {
            JsonObject item = jsonElement.getAsJsonObject();
            String symbol = item.get("symbol").getAsString();
            String price = item.get("price").getAsString();
            BoundValueOperations<String, String> priceOps = redisTemplate.boundValueOps(symbol + ".price");
            priceOps.set(price, 1, TimeUnit.MINUTES);
        }
    }

    @Resource
    CoinProcessor coinProcessor;
    @Resource
    private KLineService kLineService;
    @Resource
    private RedisTemplate<String, String> redisTemplate;
    @Resource
    private ConfigService configService;
    @Resource
    private RequestInitService requestInitService;
    @Resource
    private CurrencyTokenService currencyTokenService;
    @Resource
    private CurrencyTokenLogService currencyTokenLogService;
    @Resource
    private TradeTokenLogService tradeTokenLogService;
    @Resource
    private CurrencyTokenOrderService currencyTokenOrderService;


}
