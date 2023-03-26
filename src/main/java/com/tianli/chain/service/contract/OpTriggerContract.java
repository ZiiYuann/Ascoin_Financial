package com.tianli.chain.service.contract;

import com.tianli.chain.enums.ChainType;
import com.tianli.chain.web3j.OpEthGetTransactionReceipt;
import com.tianli.chain.web3j.OpTransactionReceipt;
import com.tianli.chain.web3j.OpWeb3j;
import com.tianli.common.ConfigConstants;
import com.tianli.common.blockchain.NetworkType;
import com.tianli.mconfig.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.web3j.protocol.core.JsonRpc2_0Web3j;
import org.web3j.protocol.core.methods.response.Transaction;
import org.web3j.protocol.http.HttpService;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @Author cs
 * @Date 2023-01-06 11:06
 */
@Component
public class OpTriggerContract extends Web3jContractOperation {

    private final ConfigService configService;
    private final JsonRpc2_0Web3j web3j;

    @Autowired
    public OpTriggerContract(ConfigService configService, @Value("${rpc.op.url}") String url) {
        this.configService = configService;
        this.web3j = new OpWeb3j(new HttpService(url));
    }

    @Override
    public String computeAddress(long addressId) throws IOException {
        return computeAddress(new BigInteger("" + addressId));
    }

    @Override
    public String computeAddress(BigInteger addressId) throws IOException {
        String address = configService.get(ConfigConstants.OP_MAIN_WALLET_ADDRESS);
        return computeAddress(address, addressId);
    }

    @Override
    public String computeAddress(String walletAddress, BigInteger addressId) throws IOException {
        String contractAddress = configService.get(ConfigConstants.OP_TRIGGER_ADDRESS);
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
            gas = configService.getOrDefault(ConfigConstants.OP_GAS_PRICE, "0.001");
        }
        return gas;
    }

    @Override
    protected String getMainWalletAddress() {
        return configService.get(ConfigConstants.OP_MAIN_WALLET_ADDRESS);
    }

    @Override
    protected String getMainWalletPassword() {
        return configService.get(ConfigConstants.MAIN_WALLET_PASSWORD);
    }

    @Override
    protected Long getChainId() {
        return Long.parseLong(configService.getOrDefault(ConfigConstants.OP_CHAIN_ID, "10"));
    }

    @Override
    protected String getRecycleGasLimit() {
        return configService.getOrDefault(ConfigConstants.OP_GAS_LIMIT_PLUS, "10000000");
    }

    @Override
    protected String getTransferGasLimit() {
        return configService.getOrDefault(ConfigConstants.OP_GAS_LIMIT, "800000");
    }

    @Override
    protected String getRecycleTriggerAddress() {
        return configService.get(ConfigConstants.OP_TRIGGER_ADDRESS);
    }

    @Override
    protected ChainType getChainType() {
        return ChainType.OPTIMISTIC;
    }

    @Override
    public boolean matchByChain(NetworkType chain) {
        return NetworkType.erc20_optimistic.equals(chain);
    }

    @Override
    public BigDecimal getConsumeFee(String hash) throws IOException {
        OpWeb3j opWeb3j = (OpWeb3j) this.web3j;
        OpEthGetTransactionReceipt response = opWeb3j.opEthGetTransactionReceipt(hash).send();
        OpTransactionReceipt transactionReceipt = response.getResult();
        Transaction transaction = opWeb3j.ethGetTransactionByHash(hash).send().getResult();
        BigInteger gasUsed = transactionReceipt.getGasUsed();
        BigInteger gasPrice = transaction.getGasPrice();
        BigInteger fee = gasPrice.multiply(gasUsed).add(transactionReceipt.getL1Fee());
        return new BigDecimal(fee).movePointLeft(18);
    }
}
