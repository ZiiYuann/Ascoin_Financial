package com.tianli.chain.service.contract;

import com.tianli.chain.web3j.ArbitrumEthGetTransactionReceipt;
import com.tianli.chain.web3j.ArbitrumTransactionReceipt;
import com.tianli.chain.web3j.ArbitrumWeb3j;
import com.tianli.common.ConfigConstants;
import com.tianli.common.blockchain.NetworkType;
import com.tianli.mconfig.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.web3j.protocol.core.JsonRpc2_0Web3j;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @Author cs
 * @Date 2023-01-06 11:23
 */
@Component
public class ArbitrumTriggerContract extends Web3jContractOperation {

    private ConfigService configService;
    private JsonRpc2_0Web3j web3j;

    @Autowired
    public ArbitrumTriggerContract(ConfigService configService, @Value("${rpc.arbitrum.url}")String url) {
        this.configService = configService;
        this.web3j = new ArbitrumWeb3j(new HttpService(url));
    }

    @Override
    public String computeAddress(long addressId) throws IOException {
        return computeAddress(new BigInteger("" + addressId));
    }

    public String computeAddress(BigInteger addressId) throws IOException {
        String address = configService.get(ConfigConstants.ARBITRUM_MAIN_WALLET_ADDRESS);
        return computeAddress(address, addressId);
    }

    public String computeAddress(String walletAddress, BigInteger addressId) throws IOException {
        String contractAddress = configService.get(ConfigConstants.ARBITRUM_TRIGGER_ADDRESS);
        return super.computeAddress(walletAddress, addressId, contractAddress);
    }

    @Override
    protected JsonRpc2_0Web3j getWeb3j() {
        return web3j;
    }

    @Override
    protected String getGas() {
        String gas;
        try {
            BigInteger gasPrice = web3j.ethGasPrice().send().getGasPrice();
            gas = new BigDecimal(gasPrice).movePointLeft(9).toString();
        } catch (IOException e) {
            gas = configService.getOrDefault(ConfigConstants.ARBITRUM_GAS_PRICE,"0.1");
        }
        return gas;
    }

    @Override
    protected String getMainWalletAddress() {
        return configService.get(ConfigConstants.ARBITRUM_MAIN_WALLET_ADDRESS);
    }

    @Override
    protected String getMainWalletPassword() {
        return configService.get(ConfigConstants.MAIN_WALLET_PASSWORD);
    }

    @Override
    protected Long getChainId() {
        return Long.parseLong(configService.getOrDefault(ConfigConstants.ARBITRUM_CHAIN_ID, "42161"));
    }

    @Override
    protected String getRecycleGasLimit() {
        return configService.getOrDefault(ConfigConstants.ARBITRUM_GAS_LIMIT_PLUS,"10000000");
    }

    @Override
    protected String getTransferGasLimit() {
        return configService.getOrDefault(ConfigConstants.ARBITRUM_GAS_LIMIT,"800000");
    }

    @Override
    protected String getRecycleTriggerAddress() {
        return configService.get(ConfigConstants.ARBITRUM_TRIGGER_ADDRESS);
    }

    @Override
    public boolean matchByChain(NetworkType chain) {
        return NetworkType.erc20_arbitrum.equals(chain);
    }

    @Override
    public BigDecimal getConsumeFee(String hash) throws IOException {
        ArbitrumWeb3j arbitrumWeb3j = (ArbitrumWeb3j) this.web3j;
        ArbitrumEthGetTransactionReceipt response = arbitrumWeb3j.arbitrumEthGetTransactionReceipt(hash).send();
        ArbitrumTransactionReceipt arbitrumReceipt = response.getResult();
        BigInteger transactionFee = arbitrumReceipt.getEffectiveGasPrice().multiply(arbitrumReceipt.getGasUsed());
        return new BigDecimal(transactionFee).movePointLeft(18);
    }
}
