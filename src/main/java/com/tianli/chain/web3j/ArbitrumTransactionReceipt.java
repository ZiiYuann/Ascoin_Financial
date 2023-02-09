package com.tianli.chain.web3j;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

/**
 * @Author cs
 * @Date 2022-07-14 14:06
 */
@NoArgsConstructor
@AllArgsConstructor
public class ArbitrumTransactionReceipt extends TransactionReceipt {
    private String effectiveGasPrice;

    public BigInteger getEffectiveGasPrice() {
        return Numeric.decodeQuantity(this.effectiveGasPrice);
    }

    public void setEffectiveGasPrice(String effectiveGasPrice) {
        this.effectiveGasPrice = effectiveGasPrice;
    }
}
