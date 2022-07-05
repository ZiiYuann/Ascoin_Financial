package com.tianli.chain.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.tianli.address.AddressService;
import com.tianli.address.mapper.Address;
import com.tianli.chain.mapper.ChainLog;
import com.tianli.chain.mapper.ChainLogMapper;
import com.tianli.common.CommonFunction;
import com.tianli.currency.DigitalCurrency;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.mconfig.ConfigService;
import com.tianli.tool.MapBuilder;
import com.tianli.tool.http.HttpHandler;
import com.tianli.tool.http.HttpRequest;
import com.tianli.tool.judge.JsonObjectTool;
import com.tianli.user.UserService;
import com.tianli.user.mapper.User;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigInteger;

/**
 * @author wangqiyun
 * @since 2020/11/14 15:55
 */

@Service
public class ChainService {

    public void update(long uid, Address address, TokenCurrencyType type, boolean collect) {
        if (address == null) return;
        User user = userService._get(address.getUid());
        if (user == null) return;
        String wallet_url = configService.getOrDefault("wallet_url", "https://www.twallet.pro/api");
        switch (type) {
            case usdt_omni:
            case btc: {
                String stringResult = HttpHandler.execute(new HttpRequest().setUrl(wallet_url + "/address/balance").setQueryMap(MapBuilder.Map()
                        .put("address", address.getBtc()).build())).getStringResult();
                JsonObject jsonObject = new Gson().fromJson(stringResult, JsonObject.class);
                String btc = JsonObjectTool.getAsString(jsonObject, "data.btc");
                String usdt = JsonObjectTool.getAsString(jsonObject, "data.usdt");
                if (StringUtils.isEmpty(btc) || StringUtils.isEmpty(usdt)) return;
                if (new BigInteger(btc).compareTo(BigInteger.ZERO) > 0)
                    chainLogMapper.replace(ChainLog.builder().id(CommonFunction.generalId()).address(address.getBtc()).currency_type(TokenCurrencyType.btc)
                            .amount(new BigInteger(btc)).uid(uid).username(user.getUsername())
                            .u_create_time(user.getCreate_time()).build());
                else
                    chainLogMapper.delete(new LambdaQueryWrapper<ChainLog>().eq(ChainLog::getAddress, address.getBtc()).eq(ChainLog::getCurrency_type, TokenCurrencyType.btc));
                if (new BigInteger(usdt).compareTo(BigInteger.ZERO) > 0)
                    chainLogMapper.replace(ChainLog.builder().id(CommonFunction.generalId()).address(address.getBtc()).currency_type(TokenCurrencyType.usdt_omni)
                            .amount(new BigInteger(usdt)).uid(uid).username(user.getUsername())
                            .u_create_time(user.getCreate_time()).build());
                else
                    chainLogMapper.delete(new LambdaQueryWrapper<ChainLog>().eq(ChainLog::getAddress, address.getBtc()).eq(ChainLog::getCurrency_type, TokenCurrencyType.usdt_omni));

                if (!collect) {
                    //如果链金额大于1w自动归集
                    if (new DigitalCurrency(TokenCurrencyType.btc, new BigInteger(btc)).toOther(TokenCurrencyType.cny).getMoney() > 10000.0) {
                        chainTxService.collect(TokenCurrencyType.btc, new BigInteger(btc), address.getBtc(), uid, new BigInteger(usdt));
                    } else if (new DigitalCurrency(TokenCurrencyType.usdt_omni, new BigInteger(usdt)).toOther(TokenCurrencyType.cny).getMoney() > 10000.0) {
                        chainTxService.collect(TokenCurrencyType.usdt_omni, new BigInteger(usdt), address.getBtc(), uid, new BigInteger(btc));
                    }
                }
            }
            break;
            case eth:
            case usdt_erc20: {
                String stringResult = HttpHandler.execute(new HttpRequest().setUrl(wallet_url + "/eth/address/balance").setQueryMap(MapBuilder.Map()
                        .put("address", address.getEth()).build())).getStringResult();
                JsonObject jsonObject = new Gson().fromJson(stringResult, JsonObject.class);
                String eth = JsonObjectTool.getAsString(jsonObject, "data.eth");
                String usdt = JsonObjectTool.getAsString(jsonObject, "data.usdt");
                if (StringUtils.isEmpty(eth) || StringUtils.isEmpty(usdt)) return;
                if (new BigInteger(eth).compareTo(BigInteger.ZERO) > 0)
                    chainLogMapper.replace(ChainLog.builder().id(CommonFunction.generalId()).address(address.getEth()).currency_type(TokenCurrencyType.eth)
                            .amount(new BigInteger(eth)).uid(uid).username(user.getUsername())
                            .u_create_time(user.getCreate_time()).build());
                else
                    chainLogMapper.delete(new LambdaQueryWrapper<ChainLog>().eq(ChainLog::getAddress, address.getEth()).eq(ChainLog::getCurrency_type, TokenCurrencyType.eth));
                if (new BigInteger(usdt).compareTo(BigInteger.ZERO) > 0)
                    chainLogMapper.replace(ChainLog.builder().id(CommonFunction.generalId()).address(address.getEth()).currency_type(TokenCurrencyType.usdt_erc20)
                            .amount(new BigInteger(usdt)).uid(uid).username(user.getUsername())
                            .u_create_time(user.getCreate_time()).build());
                else
                    chainLogMapper.delete(new LambdaQueryWrapper<ChainLog>().eq(ChainLog::getAddress, address.getEth()).eq(ChainLog::getCurrency_type, TokenCurrencyType.usdt_erc20));
                if (!collect) {
                    //如果链金额大于1w自动归集
                    if (new DigitalCurrency(TokenCurrencyType.eth, new BigInteger(eth)).toOther(TokenCurrencyType.cny).getMoney() > 10000.0) {
                        chainTxService.collect(TokenCurrencyType.eth, new BigInteger(eth), address.getEth(), uid, new BigInteger(usdt));
                    } else if (new DigitalCurrency(TokenCurrencyType.usdt_erc20, new BigInteger(usdt)).toOther(TokenCurrencyType.cny).getMoney() > 10000.0) {
                        chainTxService.collect(TokenCurrencyType.usdt_erc20, new BigInteger(usdt), address.getEth(), uid, new BigInteger(eth));
                    }
                }
            }
            break;
            case tron:
            case usdt_trc20: {
                String stringResult = HttpHandler.execute(new HttpRequest().setUrl(wallet_url + "/org/tron/address/balance").setQueryMap(MapBuilder.Map()
                        .put("address", address.getTron()).build())).getStringResult();
                System.out.println("ChainService:update ==> " + stringResult);
                JsonObject jsonObject = new Gson().fromJson(stringResult, JsonObject.class);
                String tron = JsonObjectTool.getAsString(jsonObject, "data.trx");
                String usdt = JsonObjectTool.getAsString(jsonObject, "data.usdt");
                if (StringUtils.isEmpty(tron) || StringUtils.isEmpty(usdt)) return;
                if (new BigInteger(tron).compareTo(BigInteger.ZERO) > 0)
                    chainLogMapper.replace(ChainLog.builder().id(CommonFunction.generalId()).address(address.getTron()).currency_type(TokenCurrencyType.usdt_trc20)
                            .amount(new BigInteger(tron)).uid(uid).username(user.getUsername())
                            .u_create_time(user.getCreate_time()).build());
                else
                    chainLogMapper.delete(new LambdaQueryWrapper<ChainLog>().eq(ChainLog::getAddress, address.getTron()).eq(ChainLog::getCurrency_type, TokenCurrencyType.usdt_trc20));
                if (new BigInteger(usdt).compareTo(BigInteger.ZERO) > 0)
                    chainLogMapper.replace(ChainLog.builder().id(CommonFunction.generalId()).address(address.getTron()).currency_type(TokenCurrencyType.usdt_trc20)
                            .amount(new BigInteger(usdt)).uid(uid).username(user.getUsername())
                            .u_create_time(user.getCreate_time()).build());
                else
                    chainLogMapper.delete(new LambdaQueryWrapper<ChainLog>().eq(ChainLog::getAddress, address.getTron()).eq(ChainLog::getCurrency_type, TokenCurrencyType.usdt_trc20));
                if (!collect) {
                    //如果链金额大于1w自动归集
                    if (new DigitalCurrency(TokenCurrencyType.usdt_trc20, new BigInteger(usdt)).toOther(TokenCurrencyType.cny).getMoney() > 10000.0) {
                        chainTxService.collect(TokenCurrencyType.usdt_trc20, new BigInteger(usdt), address.getTron(), uid, new BigInteger(tron));
                    }
                }
            }
            break;
            default:
                return;
        }
    }


    @Resource
    private ChainTxService chainTxService;
    @Resource
    private UserService userService;
    @Resource
    private ConfigService configService;
    @Resource
    private AddressService addressService;
    @Resource
    private ChainLogMapper chainLogMapper;

    public void updateTxid(long id, String toString) {

    }

}
