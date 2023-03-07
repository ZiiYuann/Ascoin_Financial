package com.tianli.chain.service.contract;

import com.tianli.common.ConfigConstants;
import com.tianli.common.blockchain.NetworkType;
import com.tianli.mconfig.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.web3j.protocol.core.JsonRpc2_0Web3j;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigInteger;

@Slf4j
@Component
public class BscTriggerContract extends Web3jContractOperation {

    @Resource
    private ConfigService configService;
    @Resource
    private JsonRpc2_0Web3j bscWeb3j;

    @Override
    public String computeAddress(long addressId) throws IOException {
        return computeAddress(new BigInteger("" + addressId));
    }

    @Override
    public String computeAddress(BigInteger addressId) throws IOException {
        String address = configService.get(ConfigConstants.BSC_MAIN_WALLET_ADDRESS);
        return computeAddress(address, addressId);
    }

    @Override
    public String computeAddress(String walletAddress, BigInteger addressId) throws IOException {
        String contractAddress = configService.get(ConfigConstants.BSC_TRIGGER_ADDRESS);
        return super.computeAddress(walletAddress, addressId, contractAddress);
    }

    @Override
    protected JsonRpc2_0Web3j getWeb3j() {
        return bscWeb3j;
    }

    @Override
    protected String getGas() {
        return configService.getOrDefault(ConfigConstants.BSC_GAS_PRICE,"5");
    }

    @Override
    protected String getMainWalletAddress() {
        return configService.get(ConfigConstants.BSC_MAIN_WALLET_ADDRESS);
    }

    @Override
    protected String getMainWalletPassword() {
        return configService.get(ConfigConstants.MAIN_WALLET_PASSWORD);
    }

    @Override
    protected Long getChainId() {
        return Long.parseLong(configService.getOrDefault(ConfigConstants.BSC_CHAIN_ID, "56"));
    }

    @Override
    protected String getRecycleGasLimit() {
        return configService.getOrDefault(ConfigConstants.BSC_GAS_LIMIT_PLUS,"10000000");
    }

    @Override
    protected String getTransferGasLimit() {
        return configService.getOrDefault(ConfigConstants.BSC_GAS_LIMIT,"800000");
    }

    @Override
    protected String getRecycleTriggerAddress() {
        return configService.get(ConfigConstants.BSC_TRIGGER_ADDRESS);
    }

    @Override
    public boolean matchByChain(NetworkType chain) {
        return NetworkType.bep20.equals(chain);
    }
}
