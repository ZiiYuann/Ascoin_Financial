package com.tianli.currency.service;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.tianli.common.HttpUtils;
import com.tianli.common.lock.RedisLock;
import com.tianli.currency.enums.NationalCurrencyEnum;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.tool.http.HttpHandler;
import com.tianli.tool.http.HttpRequest;
import com.tianli.tool.judge.JsonObjectTool;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Author wangqiyun
 * @Date 2020/3/12 17:11
 */
@Component
public class DigitalCurrencyExchange {

    public double ethBtcPrice() {
        BoundValueOperations<String, Object> ops = redisTemplate.boundValueOps("ethBtcPrice");
        Object o = ops.get();
        if (o != null) return Double.valueOf(o.toString());
        String stringResult = HttpHandler.execute(new HttpRequest().setUrl("https://api.huobi.pro/market/trade?symbol=ethbtc")).getStringResult();
        JsonObject jsonObject = new Gson().fromJson(stringResult, JsonObject.class);
        Double aDouble = JsonObjectTool.getAsDouble(jsonObject, "tick.data[0].price");
        if (aDouble == null) ErrorCodeEnum.NETWORK_ERROR.throwException();
        ops.set(aDouble, 1L, TimeUnit.MINUTES);
        return aDouble;
    }


    public double ethUsdtPrice() {
        BoundValueOperations<String, Object> ops = redisTemplate.boundValueOps("ethUsdtPrice");
        Object o = ops.get();
        if (o != null) return Double.valueOf(o.toString());
        String stringResult = HttpHandler.execute(new HttpRequest().setUrl("https://api.huobi.pro/market/trade?symbol=ethusdt")).getStringResult();
        JsonObject jsonObject = new Gson().fromJson(stringResult, JsonObject.class);
        Double aDouble = JsonObjectTool.getAsDouble(jsonObject, "tick.data[0].price");
        if (aDouble == null) ErrorCodeEnum.NETWORK_ERROR.throwException();
        ops.set(aDouble, 1L, TimeUnit.MINUTES);
        return aDouble;

    }

    public double trxUsdtPrice() {
        BoundValueOperations<String, Object> ops = redisTemplate.boundValueOps("trxUsdtPrice");
        Object o = ops.get();
        if (o != null) return Double.valueOf(o.toString());
        String stringResult = HttpHandler.execute(new HttpRequest().setUrl("https://api.huobi.pro/market/trade?symbol=trxusdt")).getStringResult();
        JsonObject jsonObject = new Gson().fromJson(stringResult, JsonObject.class);
        Double aDouble = JsonObjectTool.getAsDouble(jsonObject, "tick.data[0].price");
        if (aDouble == null) ErrorCodeEnum.NETWORK_ERROR.throwException();
        ops.set(aDouble, 1L, TimeUnit.MINUTES);
        return aDouble;

    }

    public double bnbUsdtPrice() {
        BoundValueOperations<String, Object> ops = redisTemplate.boundValueOps("bnbUsdtPrice");
        Object o = ops.get();
        if (o != null) return Double.valueOf(o.toString());
        String stringResult = HttpHandler.execute(new HttpRequest().setUrl("https://api.huobi.pro/market/trade?symbol=bnbusdt")).getStringResult();
        JsonObject jsonObject = new Gson().fromJson(stringResult, JsonObject.class);
        Double aDouble = JsonObjectTool.getAsDouble(jsonObject, "tick.data[0].price");
        if (aDouble == null) ErrorCodeEnum.NETWORK_ERROR.throwException();
        ops.set(aDouble, 1L, TimeUnit.MINUTES);
        return aDouble;
    }

    public double coinUsdtPriceHuobi(String coinName) {
        coinName = coinName.toLowerCase(Locale.ROOT);
        var key = coinName + "UsdtPrice";
        BoundValueOperations<String, Object> ops = redisTemplate.boundValueOps(key);
        Object o = ops.get();
        if (o != null) return Double.parseDouble(o.toString());
        String stringResult = HttpHandler.execute(new HttpRequest().setUrl("https://api.huobi.pro/market/trade?symbol=" + coinName + "usdt")).getStringResult();
        JsonObject jsonObject = new Gson().fromJson(stringResult, JsonObject.class);
        Double aDouble = JsonObjectTool.getAsDouble(jsonObject, "tick.data[0].price");
        if (aDouble == null) ErrorCodeEnum.NETWORK_ERROR.throwException();
        ops.set(aDouble, 1L, TimeUnit.MINUTES);
        return aDouble;
    }

    public double coinUsdtPriceBnb(String coinName) {
        coinName = coinName.toLowerCase(Locale.ROOT);
        var key = coinName + "UsdtPrice";
        BoundValueOperations<String, Object> ops = redisTemplate.boundValueOps(key);
        Object o = ops.get();
        if (o != null) return Double.parseDouble(o.toString());
        String stringResult = HttpHandler.execute(new HttpRequest().setUrl("https://api.binance.com/api/v3/ticker/price?symbol=" + coinName.toUpperCase(Locale.ROOT) + "USDT")).getStringResult();
        JsonObject jsonObject = new Gson().fromJson(stringResult, JsonObject.class);
        Double aDouble = JsonObjectTool.getAsDouble(jsonObject, "price");
        if (aDouble == null) ErrorCodeEnum.NETWORK_ERROR.throwException();
        ops.set(aDouble, 1L, TimeUnit.MINUTES);
        return aDouble;
    }

    public double coinUsdtPriceOkx(String coinName) {
        coinName = coinName.toLowerCase(Locale.ROOT);
        var key = coinName + "UsdtPrice";
        BoundValueOperations<String, Object> ops = redisTemplate.boundValueOps(key);
        Object o = ops.get();
        if (o != null) return Double.parseDouble(o.toString());
        String stringResult = HttpHandler.execute(new HttpRequest().setUrl("https://www.okx.com/api/v5/market/ticker?instId=" + coinName.toUpperCase(Locale.ROOT) + "-USDT")).getStringResult();
        JsonObject jsonObject = new Gson().fromJson(stringResult, JsonObject.class);
        Double aDouble = JsonObjectTool.getAsDouble(jsonObject, "data[0].last");
        if (aDouble == null) ErrorCodeEnum.NETWORK_ERROR.throwException();
        ops.set(aDouble, 1L, TimeUnit.MINUTES);
        return aDouble;
    }



    public double btcUsdtPrice() {
        BoundValueOperations<String, Object> ops = redisTemplate.boundValueOps("btcUsdtPrice");
        Object o = ops.get();
        if (o != null) return Double.valueOf(o.toString());
        String stringResult = HttpHandler.execute(new HttpRequest().setUrl("https://api.huobi.pro/market/trade?symbol=btcusdt")).getStringResult();
        JsonObject jsonObject = new Gson().fromJson(stringResult, JsonObject.class);
        Double aDouble = JsonObjectTool.getAsDouble(jsonObject, "tick.data[0].price");
        if (aDouble == null) ErrorCodeEnum.NETWORK_ERROR.throwException();
        ops.set(aDouble, 1L, TimeUnit.MINUTES);
        return aDouble;
    }

    public double usdtCnyPrice() {
        BoundValueOperations<String, Object> ops = redisTemplate.boundValueOps("usdtCnyPrice");
        Object o = ops.get();
        if (o != null && !"null".equals(o)) return Double.valueOf(o.toString());
        Double aDouble = null;
        try {
            String stringResult = HttpHandler.execute(new HttpRequest().setUrl("https://api.coingecko.com/api/v3/simple/token_price/binance-smart-chain?contract_addresses=0x55d398326f99059ff775485246999027b3197955&vs_currencies=cny")).getStringResult();
            JsonObject jsonObject = new Gson().fromJson(stringResult, JsonObject.class);
            aDouble = JsonObjectTool.getAsDouble(jsonObject, "0x55d398326f99059ff775485246999027b3197955.cny");
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }
        if (aDouble == null) return 0.0;
        ops.set(aDouble, 1L, TimeUnit.HOURS);
        return aDouble;
    }

    public double usd2Cny() {
        BoundValueOperations<String, Object> ops = redisTemplate.boundValueOps("usd2Cny");
        Double aDouble = (Double)(ops.get());
        if(aDouble != null) return aDouble;
        if(redisLock._lock("usd2Cny_mutex", 1L, TimeUnit.MINUTES)) {
            try {
                String stringResult = HttpHandler.execute(new HttpRequest().setUrl("https://data.fixer.io/api/latest?base=USD&symbols=CNY&access_key=5aaf9fbe4063759cd073dc3fb11f5d6d")).getStringResult();
                JsonObject jsonObject = new Gson().fromJson(stringResult, JsonObject.class);
                aDouble = JsonObjectTool.getAsDouble(jsonObject, "rates.CNY");
                if(aDouble == null) return 0.0;
                ops.set(aDouble, 1L, TimeUnit.MINUTES);
                return aDouble;
            } catch (Exception e) {
                e.printStackTrace();
                return 0.0;
            } finally {
                redisLock.unlock("usd2Cny_mutex");
            }
        } else {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return usd2Cny();
        }
    }


    public double ethCnyPrice() {
        BoundValueOperations<String, Object> ops = redisTemplate.boundValueOps("ethCnyPrice");
        Object o = ops.get();
        if (o != null) return Double.valueOf(o.toString());
        String stringResult = HttpHandler.execute(new HttpRequest().setUrl("https://otc-api-hk.eiijo.cn/v1/data/trade-market?coinId=3&currency=1&tradeType=buy&currPage=1&payMethod=0&country=37&blockType=general&online=1&range=0&amount=")).getStringResult();
        JsonObject jsonObject = new Gson().fromJson(stringResult, JsonObject.class);
        Double aDouble = JsonObjectTool.getAsDouble(jsonObject, "data[0].price");
        if (aDouble == null) ErrorCodeEnum.NETWORK_ERROR.throwException();
        ops.set(aDouble, 1L, TimeUnit.MINUTES);
        return aDouble;
    }

    public double btcCnyPrice() {
        BoundValueOperations<String, Object> ops = redisTemplate.boundValueOps("btcCnyPrice");
        Object o = ops.get();
        if (o != null) return Double.valueOf(o.toString());
        String stringResult = HttpHandler.execute(new HttpRequest().setUrl("https://otc-api-hk.eiijo.cn/v1/data/trade-market?coinId=1&currency=1&tradeType=buy&currPage=1&payMethod=0&country=37&blockType=general&online=1&range=0&amount=")).getStringResult();
        JsonObject jsonObject = new Gson().fromJson(stringResult, JsonObject.class);
        Double aDouble = JsonObjectTool.getAsDouble(jsonObject, "data[0].price");
        if (aDouble == null) ErrorCodeEnum.NETWORK_ERROR.throwException();
        ops.set(aDouble, 1L, TimeUnit.MINUTES);
        return aDouble;
    }

    /**
     * CNY 人民币 currency = 1
     * MYR 马来西亚 currency = 22
     * USD 美元 currency = 2
     * SGD 新加坡 currency = 3
     * THB 泰国 currency = 31
     * HKD 香港 currency = 13
     * TWD 台湾 currency = 10
     * @return
     */
    public double usdtPrice(NationalCurrencyEnum currency) {
        BoundValueOperations<String, Object> ops = redisTemplate.boundValueOps("usdt"+currency+"Price");
        Object o = ops.get();
        if (o != null) return Double.valueOf(o.toString());
        if(currency.getCurrency() > 0){
            String stringResult = HttpHandler.execute(new HttpRequest().setUrl("https://otc-api-hk.eiijo.cn/v1/data/trade-market?coinId=2&currency="+currency.getCurrency()+"&tradeType=sell&currPage=1&payMethod=0&country=37&blockType=general&online=1&range=0&amount=")).getStringResult();
            JsonObject jsonObject = new Gson().fromJson(stringResult, JsonObject.class);
            Double aDouble = JsonObjectTool.getAsDouble(jsonObject, "data[0].price");
            if (aDouble == null) ErrorCodeEnum.NETWORK_ERROR.throwException();
            ops.set(aDouble, 1L, TimeUnit.MINUTES);
            return aDouble;
        } else {
            Map<String, String> headers = Maps.newHashMap();
            headers.put("content-type", "application/json");
            String stringResult = null;
            try {
                HttpResponse post = HttpUtils.doPost("https://c2c.binance.com", "/gateway-api/v2/public/c2c/adv/quoted-price", "POST", headers, null, String.format("{\"assets\":[\"USDT\"],\"fiatCurrency\":\"%s\",\"tradeType\":\"BUY\",\"fromUserRole\":\"USER\"}", currency.name()));
                stringResult = EntityUtils.toString(post.getEntity());
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(StringUtils.isBlank(stringResult)){
                return 0.0;
            }
            JsonObject jsonObject = new Gson().fromJson(stringResult, JsonObject.class);
            Double aDouble = JsonObjectTool.getAsDouble(jsonObject, "data[0].referencePrice");
            if (aDouble == null) ErrorCodeEnum.NETWORK_ERROR.throwException();
            ops.set(aDouble, 1L, TimeUnit.MINUTES);
            return aDouble;
        }
    }

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private RedisLock redisLock;

}
