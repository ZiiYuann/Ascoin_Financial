package com.tianli.chain.service.contract;

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
 * @Date 2023-01-05 16:08
 */
@Component
public class PolygonTriggerContract extends Web3jContractOperation {

    private ConfigService configService;
    private JsonRpc2_0Web3j web3j;

    @Autowired
    public PolygonTriggerContract(ConfigService configService, @Value("${rpc.polygon.url}")String url) {
        this.configService = configService;
        this.web3j = new JsonRpc2_0Web3j(new HttpService(url));
    }

    @Override
    public String computeAddress(long addressId) throws IOException {
        return computeAddress(new BigInteger("" + addressId));
    }

    public String computeAddress(BigInteger addressId) throws IOException {
        String address = configService.get(ConfigConstants.POLYGON_MAIN_WALLET_ADDRESS);
        return computeAddress(address, addressId);
    }

    public String computeAddress(String walletAddress, BigInteger addressId) throws IOException {
        String contractAddress = configService.get(ConfigConstants.POLYGON_TRIGGER_ADDRESS);
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
            gas = configService.getOrDefault(ConfigConstants.POLYGON_GAS_PRICE,"50");
        }
        return gas;
    }

    @Override
    protected String getMainWalletAddress() {
        return configService.get(ConfigConstants.POLYGON_MAIN_WALLET_ADDRESS);
    }

    @Override
    protected String getMainWalletPassword() {
        return configService.get(ConfigConstants.MAIN_WALLET_PASSWORD);
    }

    @Override
    protected Long getChainId() {
        return Long.parseLong(configService.getOrDefault(ConfigConstants.POLYGON_CHAIN_ID, "137"));
    }

    @Override
    protected String getRecycleGasLimit() {
        return configService.getOrDefault(ConfigConstants.POLYGON_GAS_LIMIT_PLUS,"10000000");
    }

    @Override
    protected String getTransferGasLimit() {
        return configService.getOrDefault(ConfigConstants.POLYGON_GAS_LIMIT,"800000");
    }

    @Override
    protected String getRecycleTriggerAddress() {
        return configService.get(ConfigConstants.POLYGON_TRIGGER_ADDRESS);
    }

    @Override
    public boolean matchByChain(NetworkType chain) {
        return NetworkType.erc20_polygon.equals(chain);
    }
}
