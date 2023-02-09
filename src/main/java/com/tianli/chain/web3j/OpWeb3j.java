package com.tianli.chain.web3j;

import org.web3j.protocol.Web3jService;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.JsonRpc2_0Web3j;
import org.web3j.protocol.core.Request;

import java.util.Arrays;

/**
 * @Author cs
 * @Date 2022-07-11 14:41
 */
public class OpWeb3j extends JsonRpc2_0Web3j {
    public OpWeb3j(Web3jService web3jService) {
        super(web3jService);
    }

    public Request<?, OpEthGetTransactionReceipt> opEthGetTransactionReceipt(String transactionHash) {
        return new Request("eth_getTransactionReceipt", Arrays.asList(transactionHash), super.web3jService, OpEthGetTransactionReceipt.class);
    }

}
