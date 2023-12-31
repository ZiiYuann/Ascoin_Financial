package com.tianli.chain.service;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tianli.address.PushHttpClient;
import com.tianli.address.mapper.Address;
import com.tianli.address.mapper.AddressMapper;
import com.tianli.chain.dto.CallbackPathDTO;
import com.tianli.chain.dto.PushConditionReq;
import com.tianli.chain.dto.TxConditionReq;
import com.tianli.chain.entity.Coin;
import com.tianli.chain.enums.ChainType;
import com.tianli.common.ConfigConstants;
import com.tianli.common.blockchain.NetworkType;
import com.tianli.currency.enums.TokenAdapter;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.mconfig.ConfigService;
import com.tianli.mconfig.mapper.Config;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.util.EntityUtils;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author wangqiyun
 * @since 2020/11/14 15:55
 */

@Slf4j
@Service
@DependsOn("flywayInitializer")
public class ChainService {

    private static final String RECHARGE_ADDRESS = "/api/charge/recharge";

    @PostConstruct
    public void pushConditionInit() {
        String dataCenterCallBackRegisterPath = configService.get(ConfigConstants.DATA_CENTER_URL_REGISTER_PATH);
        String pre = configService.get(ConfigConstants.SYSTEM_URL_PATH_PREFIX);

        boolean pushRegister = false;


        List<Address> addresses = null;
        for (TokenAdapter token : TokenAdapter.values()) {
            String flag = configService._get(token.name() + ConfigConstants.PUSH_RECHARGE_CONDITION);
            if (flag == null) {
                continue;
            }

            if (!pushRegister) {
                try {
                    var rechargeUriBuilder = new URIBuilder(dataCenterCallBackRegisterPath);
                    rechargeUriBuilder.setParameter("callbackAddress", pre + RECHARGE_ADDRESS);
                    HttpPost rechargeRegisterPost = new HttpPost(rechargeUriBuilder.build());
                    rechargeRegisterPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
                    log.info("推送钱包【充值回调】注册地址信息为: 【{}】", pre + RECHARGE_ADDRESS);
                    HttpResponse rechargeRegisterRep = PushHttpClient.getClient().execute(rechargeRegisterPost);
                    this.handlerRep(rechargeRegisterRep.getEntity());

//            var withdrawUriBuilder = new URIBuilder(dataCenterCallBackRegisterPath);
//            withdrawUriBuilder.setParameter("callbackAddress", pre + WITHDRAW_ADDRESS);
//            HttpPost withdrawRegisterPost = new HttpPost(withdrawUriBuilder.build());
//            withdrawRegisterPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
//            log.info("推送钱包注册地址信息为: 【{}】", pre + WITHDRAW_ADDRESS);
//            HttpResponse withdrawRegisterRep = client.execute(withdrawRegisterPost);
//            this.handlerRep(withdrawRegisterRep.getEntity());
                    pushRegister = true;
                } catch (Exception ignored) {
                    ErrorCodeEnum.throwException("推送回调地址失败");
                }
            }

            // todo address表数据量大考虑流式
            addresses = Optional.ofNullable(addresses).orElse(addressMapper.selectList(new LambdaQueryWrapper<>()));
            addresses.forEach(address -> pushCondition(address, token, pre + "/api/charge/recharge"));
            configService.remove(new LambdaQueryWrapper<Config>().eq(Config::getName, token.name() + ConfigConstants.PUSH_RECHARGE_CONDITION));
        }
    }

    public void pushCondition(Address address, TokenAdapter tokenAdapter, String url) {
        String needPushAddress = null;
        ChainType chainType = null;
        switch (tokenAdapter.getNetwork()) {
            case trc20:
                needPushAddress = address.getTron();
                chainType = ChainType.TRON;
                break;
            case bep20:
                needPushAddress = address.getBsc();
                chainType = ChainType.BSC;
                break;
            case erc20:
                needPushAddress = address.getEth();
                chainType = ChainType.ETH;
                break;
            default:
                ErrorCodeEnum.throwException("参数错误");
        }

        TxConditionReq bscTxConditionReqUsdt = TxConditionReq.builder().contractAddress(tokenAdapter.getContractAddress())
                .to(needPushAddress)
                .chain(chainType).build();
        httpPush(List.of(bscTxConditionReqUsdt), url);
    }

    public void pushCondition(Address address, CallbackPathDTO urlPath) {
        String tron = address.getTron();
        String bsc = address.getBsc();
        String eth = address.getEth();
        pushCondition(tron, bsc, eth, urlPath);
    }

    public void pushWithdrawCondition(NetworkType networkType, String coinName, CallbackPathDTO callBackPath, String to) {
        Coin coin = coinService.getByNameAndNetwork(coinName, networkType);
        TxConditionReq txConditionReq = TxConditionReq.builder().contractAddress(coin.isMainToken() ? "0x000000" : coin.getContract())
                .to(to)
                .chain(networkType.getChainType()).build();

        switch (networkType) {
            case trc20:
                txConditionReq.setFrom(configService.get(ConfigConstants.TRON_MAIN_WALLET_ADDRESS));
                break;
            case bep20:
                txConditionReq.setFrom(configService.get(ConfigConstants.BSC_MAIN_WALLET_ADDRESS));
                break;
            case erc20:
                txConditionReq.setFrom(configService.get(ConfigConstants.ETH_MAIN_WALLET_ADDRESS));
                break;
            default:
                break;
        }

        String urlPrefix = configService.get(ConfigConstants.SYSTEM_URL_PATH_PREFIX);
        httpPush(List.of(txConditionReq), urlPrefix + callBackPath.getPath());
    }

    public void pushConditionRecharge(List<String> addresses, Coin coin) {
        List<TxConditionReq> txConditionReqs = new ArrayList<>();
        addresses.forEach(address -> {
            TxConditionReq req = TxConditionReq.builder()
                    .contractAddress(coin.isMainToken() ? "0x000000" : coin.getContract())
                    .to(address)
                    .chain(coin.getChain()).build();
            txConditionReqs.add(req);
        });
        String url = configService.get(ConfigConstants.SYSTEM_URL_PATH_PREFIX) + "/api/charge/recharge";
        httpPush(txConditionReqs, url);
    }

    public void pushCondition(String tron, String bsc, String eth, CallbackPathDTO urlPath) {

        List<TxConditionReq> txConditionReqs = new ArrayList<>();
        String urlPrefix = configService.get(ConfigConstants.SYSTEM_URL_PATH_PREFIX);
        String url = urlPrefix + urlPath.getPath();
        HashMap<ChainType, String> addressMap = new HashMap<>();
        addressMap.put(ChainType.TRON, tron);
        addressMap.put(ChainType.BSC, bsc);
        addressMap.put(ChainType.ETH, eth);
        Set<ChainType> chainTypes = addressMap.keySet();

        List<Coin> coins = coinService.pushCoinsWithCache();
        coins.forEach(coin -> {
            if (!chainTypes.contains(coin.getChain())) {
                return;
            }
            TxConditionReq req = TxConditionReq.builder().contractAddress(coin.isMainToken() ? "0x000000" : coin.getContract())
                    .to(addressMap.get(coin.getChain()))
                    .chain(coin.getChain()).build();
            txConditionReqs.add(req);
        });

        httpPush(txConditionReqs, url);
    }

    private void httpPush(List<TxConditionReq> txConditionReqs, String url) {

        String dataCenterUrlPath = configService.get(ConfigConstants.DATA_CENTER_URL_PUSH_PATH);
        // 注册域名
        try {

            /*
             * {@link com.tianli.charge.controller.ChargeController#rechargeCallback}
             */
            PushConditionReq pushConditionReq = PushConditionReq.builder()
                    .callbackAddress(url).txConditionReqs(txConditionReqs).build();
            HttpPost httpPost = new HttpPost(dataCenterUrlPath);
            httpPost.setHeader("Content-Type", "application/json");
            var jsonStr = JSONUtil.parse(pushConditionReq);
            byte[] bytes = jsonStr.toString().getBytes(StandardCharsets.UTF_8);
            httpPost.setEntity(new InputStreamEntity(new ByteArrayInputStream(bytes), bytes.length));

            log.info("推送钱包地址监控信息为: 【{}】", jsonStr);
            HttpResponse response = PushHttpClient.getClient().execute(httpPost);
            this.handlerRep(response.getEntity());
        } catch (IOException e) {
            e.printStackTrace();
            throw ErrorCodeEnum.UPLOAD_DATACENTER_ERROR.generalException();
        }
    }

    public void handlerRep(HttpEntity httpEntity) throws IOException {
        String s = EntityUtils.toString(httpEntity);
        log.info("返回消息为：" + s);
        JSONObject json = JSONUtil.parseObj(s);
        String code = json.getStr("code");
        if (!"0".equals(code)) {
            throw ErrorCodeEnum.UPLOAD_DATACENTER_ERROR.generalException();
        }
    }


    @Resource
    private ConfigService configService;
    @Resource
    private AddressMapper addressMapper;
    @Resource
    private CoinService coinService;

}
