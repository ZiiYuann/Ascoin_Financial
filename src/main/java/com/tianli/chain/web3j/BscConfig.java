package com.tianli.chain.web3j;

import com.tianli.common.ConfigConstants;
import com.tianli.mconfig.ConfigService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.protocol.core.JsonRpc2_0Web3j;
import org.web3j.protocol.http.HttpService;

import javax.annotation.Resource;

@Configuration
public class BscConfig {

    private static final String BINANCE_SMART_CHAIN_MAIN_NET = "http://18.183.222.219:8545/C676M3pn5kaSdSq";

    @Resource
    private ConfigService configService;

    @Bean
    public JsonRpc2_0Web3j bscWeb3j(){
        String bsc_url;
        try {
            bsc_url = configService.getOrDefault(ConfigConstants.BSC_CHAIN_URL, BINANCE_SMART_CHAIN_MAIN_NET);
        } catch (Exception e) {
            bsc_url = BINANCE_SMART_CHAIN_MAIN_NET;
        }
        return new JsonRpc2_0Web3j(new HttpService(bsc_url));
    }

}
