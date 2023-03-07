package com.tianli.chain.web3j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.web3j.protocol.core.JsonRpc2_0Web3j;
import org.web3j.protocol.http.HttpService;

@Configuration
public class TronWeb3jConfig {

    private static String ETH_CHAIN_MAIN_NET = "https://api.trongrid.io/jsonrpc";

    @Bean
    public JsonRpc2_0Web3j tronWeb3j() {
        return new JsonRpc2_0Web3j(new HttpService(ETH_CHAIN_MAIN_NET));
    }

}
