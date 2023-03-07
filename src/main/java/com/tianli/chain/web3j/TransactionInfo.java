package com.tianli.chain.web3j;

import lombok.Builder;
import lombok.Data;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthTransaction;

import java.math.BigInteger;

@Data
@Builder
public class TransactionInfo {
    //  0x1成功 / 0x0失败
    private String status;
    // 实际的手续费
    private BigInteger fee;
    // ethGetTransactionByHash 返回值
    private EthTransaction transaction;
    // ethGetTransactionReceipt 返回值
    private EthGetTransactionReceipt transactionReceipt;
}
