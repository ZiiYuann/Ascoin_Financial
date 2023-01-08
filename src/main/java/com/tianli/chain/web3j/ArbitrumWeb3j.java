package com.tianli.chain.web3j;

import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.JsonRpc2_0Web3j;
import org.web3j.protocol.core.Request;

import java.util.Arrays;

/**
 * @Author cs
 * @Date 2022-07-14 14:05
 */
public class ArbitrumWeb3j extends JsonRpc2_0Web3j {
    public ArbitrumWeb3j(Web3jService web3jService) {
        super(web3jService);
    }

    public Request<?, ArbitrumEthGetTransactionReceipt> arbitrumEthGetTransactionReceipt(String transactionHash) {
        return new Request("eth_getTransactionReceipt", Arrays.asList(transactionHash), super.web3jService, ArbitrumEthGetTransactionReceipt.class);
    }
}
