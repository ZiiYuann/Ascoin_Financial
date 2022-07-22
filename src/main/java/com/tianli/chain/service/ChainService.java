package com.tianli.chain.service;

import cn.hutool.json.JSONUtil;
import com.tianli.address.mapper.Address;
import com.tianli.chain.dto.PushConditionReq;
import com.tianli.chain.dto.TxConditionReq;
import com.tianli.chain.enums.ChainType;
import com.tianli.common.ConfigConstants;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.mconfig.ConfigService;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * @author wangqiyun
 * @since 2020/11/14 15:55
 */

@Service
public class ChainService {

    public void pushCondition(Address address)  {
        String bscContractAddress = configService.get(ConfigConstants.BSC_TRIGGER_ADDRESS);
        String bscWalletAddress = configService.get(ConfigConstants.BSC_MAIN_WALLET_ADDRESS);
        String ethContractAddress = configService.get(ConfigConstants.ETH_TRIGGER_ADDRESS);
        String ethWalletAddress = configService.get(ConfigConstants.ETH_MAIN_WALLET_ADDRESS);
        String tronContractAddress = configService.get(ConfigConstants.TRON_TRIGGER_ADDRESS);
        String tronWalletAddress = configService.get(ConfigConstants.TRON_MAIN_WALLET_ADDRESS);
        String tron = address.getTron();
        String bsc = address.getBsc();
        String eth = address.getEth();

        TxConditionReq bscTxConditionReq = TxConditionReq.builder().contractAddress(bscContractAddress).to(bscWalletAddress).from(bsc)
                .chain(ChainType.BSC).build();
        TxConditionReq ethTxConditionReq = TxConditionReq.builder().contractAddress(ethContractAddress).to(ethWalletAddress).from(eth)
                .chain(ChainType.BSC).build();
        TxConditionReq tronTxConditionReq = TxConditionReq.builder().contractAddress(tronContractAddress).to(tronWalletAddress).from(tron)
                .chain(ChainType.BSC).build();

        List<TxConditionReq> txConditionReqs = List.of(bscTxConditionReq, ethTxConditionReq, tronTxConditionReq);

        String urlPrefix = configService.get(ConfigConstants.SYSTEM_URL_PATH_PREFIX);
        String dataCenterPathUrlPath = configService.get(ConfigConstants.DATA_CENTER_URL_PUSH_PATH);
        /**
         * {@link com.tianli.charge.controller.ChargeController#rechargeCallback}
         */
        String url = urlPrefix + "/api/recharge";
        PushConditionReq pushConditionReq = PushConditionReq.builder()
                .callbackAddress(url).txConditionReqs(txConditionReqs).build();

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost(dataCenterPathUrlPath);
        httpPost.setHeader("Content-Type", "application/json");
        byte[] bytes = JSONUtil.parse(pushConditionReq).toString().getBytes(StandardCharsets.UTF_8);
        httpPost.setEntity(new InputStreamEntity(new ByteArrayInputStream(bytes), bytes.length));
        try{
            client.execute(httpPost);
        }catch (IOException e){
            throw ErrorCodeEnum.UPLOAD_DATACENTER_ERROR.generalException();
        }
    }

    @Resource
    private ConfigService configService;

}
