package com.tianli.chain.service;

import com.tianli.address.mapper.Address;
import com.tianli.chain.dto.PushConditionReq;
import com.tianli.chain.dto.TxConditionReq;
import com.tianli.chain.enums.ChainTypeEnum;
import com.tianli.common.ConfigConstants;
import com.tianli.common.HttpUtils;
import com.tianli.mconfig.ConfigService;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author wangqiyun
 * @since 2020/11/14 15:55
 */

@Service
public class ChainService {



    public void pushCondition(Address address){

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
                .chain(ChainTypeEnum.BSC).build();
        TxConditionReq ethTxConditionReq = TxConditionReq.builder().contractAddress(ethContractAddress).to(ethWalletAddress).from(eth)
                .chain(ChainTypeEnum.BSC).build();
        TxConditionReq tronTxConditionReq = TxConditionReq.builder().contractAddress(tronContractAddress).to(tronWalletAddress).from(tron)
                .chain(ChainTypeEnum.BSC).build();

        List<TxConditionReq> txConditionReqs = List.of(bscTxConditionReq, ethTxConditionReq, tronTxConditionReq);

        String urlPrefix = configService.get(ConfigConstants.SYSTEM_URL_PATH_PREFIX);
        String dataCenterPathUrlPath = configService.get(ConfigConstants.DATA_CENTER_URL_PUSH_PATH);

        /**
         * {@link com.tianli.charge.controller.ChargeController#rechargeCallback}
         */
        String url = urlPrefix + "/api/recharge";
        PushConditionReq pushConditionReq = PushConditionReq.builder()
                .callbackAddress(url).txConditionReqs(txConditionReqs).build();

    }



    @Resource
    private ConfigService configService;

}
