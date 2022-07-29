package com.tianli.chain.service;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tianli.address.mapper.Address;
import com.tianli.chain.dto.CallbackPathDTO;
import com.tianli.chain.dto.PushConditionReq;
import com.tianli.chain.dto.TxConditionReq;
import com.tianli.chain.enums.ChainType;
import com.tianli.common.ConfigConstants;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.common.blockchain.NetworkType;
import com.tianli.currency.enums.CurrencyAdaptType;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.mconfig.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author wangqiyun
 * @since 2020/11/14 15:55
 */

@Slf4j
@Service
public class ChainService {

    public void pushCondition(Address address,CallbackPathDTO urlPath){
        String tron = address.getTron();
        String bsc = address.getBsc();
        String eth = address.getEth();
        pushCondition(tron,bsc,eth,urlPath);
    }

    public void pushCondition(NetworkType networkType, CurrencyCoin coin, CallbackPathDTO callBackPath, String to){
        CurrencyAdaptType currencyAdaptType = CurrencyAdaptType.get(coin, networkType);
        TxConditionReq txConditionReq = TxConditionReq.builder().contractAddress(currencyAdaptType.getContractAddress())
                .to(to)
                .chain(networkType.getChainType()).build();
        String urlPrefix = configService.get(ConfigConstants.SYSTEM_URL_PATH_PREFIX);
        httpPush(List.of(txConditionReq),urlPrefix + callBackPath.getPath());
    }

    public void pushCondition(String tron,String bsc,String eth,CallbackPathDTO urlPath) {

        List<TxConditionReq> txConditionReqs = new ArrayList<>();
        String urlPrefix = configService.get(ConfigConstants.SYSTEM_URL_PATH_PREFIX);
        String url = urlPrefix + urlPath.getPath();

        if(StringUtils.isNotBlank(bsc)){
            TxConditionReq bscTxConditionReqUsdt = TxConditionReq.builder().contractAddress(CurrencyAdaptType.usdt_bep20.getContractAddress()).to(bsc)
                    .chain(ChainType.BSC).build();
            TxConditionReq bscTxConditionReqUsdc = TxConditionReq.builder().contractAddress(CurrencyAdaptType.usdc_bep20.getContractAddress()).to(bsc)
                    .chain(ChainType.BSC).build();
            txConditionReqs.add(bscTxConditionReqUsdt);
            txConditionReqs.add(bscTxConditionReqUsdc);
        }

        if(StringUtils.isNotBlank(eth)){
            TxConditionReq ethTxConditionReqUsdt = TxConditionReq.builder().contractAddress(CurrencyAdaptType.usdt_erc20.getContractAddress()).to(eth)
                    .chain(ChainType.ETH).build();
            TxConditionReq ethTxConditionReqUsdc = TxConditionReq.builder().contractAddress(CurrencyAdaptType.usdc_erc20.getContractAddress()).to(eth)
                    .chain(ChainType.ETH).build();
            txConditionReqs.add(ethTxConditionReqUsdt);
            txConditionReqs.add(ethTxConditionReqUsdc);
        }

        if(StringUtils.isNotBlank(tron)){
            TxConditionReq tronTxConditionReqUsdt = TxConditionReq.builder().contractAddress(CurrencyAdaptType.usdt_trc20.getContractAddress()).to(tron)
                    .chain(ChainType.TRON).build();
            TxConditionReq tronTxConditionReqUsdc = TxConditionReq.builder().contractAddress(CurrencyAdaptType.usdc_trc20.getContractAddress()).to(tron)
                    .chain(ChainType.TRON).build();
            txConditionReqs.add(tronTxConditionReqUsdt);
            txConditionReqs.add(tronTxConditionReqUsdc);
        }

        httpPush(txConditionReqs, url);
    }

    private void httpPush(List<TxConditionReq> txConditionReqs, String url) {
        HttpClient client = HttpClientBuilder.create().build();
        String dataCenterUrlPath = configService.get(ConfigConstants.DATA_CENTER_URL_PUSH_PATH);
        String dataCenterCallBackRegisterPath = configService.get(ConfigConstants.DATA_CENTER_URL_REGISTER_PATH);

        // 注册域名
        try {
            var uriBuilder = new URIBuilder(dataCenterCallBackRegisterPath);
            uriBuilder.setParameter("callbackAddress", url);
            HttpPost httpRegisterPost = new HttpPost(uriBuilder.build());
            httpRegisterPost.setHeader("Content-Type", "application/x-www-form-urlencoded");
            log.info("推送钱包注册地址信息为: 【{}】", url);
            HttpResponse registerRep = client.execute(httpRegisterPost);
            this.handlerRep(registerRep.getEntity());

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
            HttpResponse response = client.execute(httpPost);
            this.handlerRep(response.getEntity());
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            throw ErrorCodeEnum.UPLOAD_DATACENTER_ERROR.generalException();
        }
    }

    public void handlerRep(HttpEntity httpEntity) throws IOException {
        String s = EntityUtils.toString(httpEntity);
        log.info("返回消息为：" +s);
        JSONObject json = JSONUtil.parseObj(s);
        String code = json.getStr("code");
        if("-1".equals(code)){
            throw ErrorCodeEnum.UPLOAD_DATACENTER_ERROR.generalException();
        }
    }


    @Resource
    private ConfigService configService;


}
