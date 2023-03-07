package com.tianli.chain.service.contract;

import com.tianli.chain.dto.TransactionReceiptLogDTO;
import com.tianli.chain.entity.Coin;
import com.tianli.common.blockchain.NetworkType;
import com.tianli.currency.enums.TokenAdapter;
import com.tianli.exception.ErrorCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.tron.tronj.abi.datatypes.Address;
import org.web3j.protocol.core.JsonRpc2_0Web3j;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Numeric;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author chenb
 * @apiNote
 * @since 2022-10-26
 **/
@Slf4j
@Component
public class TronWeb3jContract extends Web3jContractOperation {

    @Resource
    private JsonRpc2_0Web3j tronWeb3j;

    @Override
    public List<TransactionReceiptLogDTO> transactionReceiptLogDTOS(Coin coin, String hash) {
        try {
            TransactionReceipt transactionReceipt = this.getTransactionReceipt(hash);
            List<Log> logs = transactionReceipt.getLogs();
            return logs.stream().map(log -> {
                TransactionReceiptLogDTO transactionReceiptLogDTO = new TransactionReceiptLogDTO();
                transactionReceiptLogDTO.setAmount(TokenAdapter.alignment(coin, Numeric.toBigInt(log.getData())));
                List<String> topics = log.getTopics();
                String fromAddress = new Address(topics.get(1)).getValue();
                transactionReceiptLogDTO.setFromAddress(fromAddress);
                return transactionReceiptLogDTO;
            }).collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
            throw ErrorCodeEnum.SYSTEM_ERROR.generalException();
        }
    }

    @Override
    public boolean matchByChain(NetworkType chain) {
        return NetworkType.trc20.equals(chain);
    }

    @Override
    protected JsonRpc2_0Web3j getWeb3j() {
        return tronWeb3j;
    }

    @Override
    protected String getGas() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected String getMainWalletAddress() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected String getMainWalletPassword() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Long getChainId() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected String getRecycleGasLimit() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected String getTransferGasLimit() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected String getRecycleTriggerAddress() {
        throw new UnsupportedOperationException();
    }
}
