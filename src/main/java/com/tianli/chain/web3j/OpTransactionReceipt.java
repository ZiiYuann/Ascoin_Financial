package com.tianli.chain.web3j;

import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.utils.Numeric;

import java.math.BigInteger;

/**
 * @Author cs
 * @Date 2022-07-11 14:43
 */
public class OpTransactionReceipt extends TransactionReceipt {
    private String l1Fee;
    private String l1FeeScalar;
    private String l1GasPrice;
    private String l1GasUsed;


    public OpTransactionReceipt() {
    }

    public BigInteger getL1Fee() {
        return Numeric.decodeQuantity(this.l1Fee);
    }

    public void setL1Fee(String l1Fee) {
        this.l1Fee = l1Fee;
    }

    public BigInteger getL1GasUsed() {
        return Numeric.decodeQuantity(this.l1GasUsed);
    }

    public void setL1GasUsed(String l1GasUsed) {
        this.l1GasUsed = l1GasUsed;
    }

    public Double getL1FeeScalar() {
        return Double.parseDouble(this.l1FeeScalar);
    }

    public void setL1FeeScalar(String l1FeeScalar) {
        this.l1FeeScalar = l1FeeScalar;
    }
    public BigInteger getL1GasPrice() {
        return Numeric.decodeQuantity(this.l1GasPrice);
    }

    public void setL1GasPrice(String l1GasPrice) {
        this.l1GasPrice = l1GasPrice;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof OpTransactionReceipt)) {
            return false;
        } else if (super.equals(o)) {
            OpTransactionReceipt that;
            that = (OpTransactionReceipt)o;

            if (this.l1Fee != null) {
                if (!this.l1Fee.equals(that.l1Fee)) {
                    return false;
                }
            } else if (that.l1Fee != null) {
                return false;
            }

            if (this.l1FeeScalar != null) {
                if (!this.l1FeeScalar.equals(that.l1FeeScalar)) {
                    return false;
                }
            } else if (that.l1FeeScalar != null) {
                return false;
            }

            if (this.l1GasPrice != null) {
                if (!this.l1GasPrice.equals(that.l1GasPrice)) {
                    return false;
                }
            } else if (that.l1GasPrice != null) {
                return false;
            }

            if (this.l1GasUsed != null) {
                return this.l1GasUsed.equals(that.l1GasUsed);
            } else return that.l1GasUsed == null;
        } else {
            return false;
        }
    }

    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (this.getL1Fee() != null ? this.getL1Fee().hashCode() : 0);
        result = 31 * result + (this.getL1FeeScalar() != null ? this.getL1FeeScalar().hashCode() : 0);
        result = 31 * result + (this.getL1GasPrice() != null ? this.getL1GasPrice().hashCode() : 0);
        result = 31 * result + (this.getL1GasUsed() != null ? this.getL1GasUsed().hashCode() : 0);
        return result;
    }

    public String toString() {
        return "OpTransactionReceipt{transactionHash='" + getTransactionHash() + '\'' + ", transactionIndex='" + getTransactionIndex() + '\'' + ", blockHash='" + getBlockHash() + '\'' + ", blockNumber='" + getBlockNumber() + '\'' + ", cumulativeGasUsed='" + getCumulativeGasUsed() + '\'' + ", gasUsed='" + getGasUsed() + '\'' + ", l1Fee='" + this.l1Fee + '\'' + ", l1FeeScalar='" + this.l1FeeScalar + '\'' + ", l1GasPrice='" + this.l1GasPrice + '\'' + ", l1GasUsed='" + this.l1GasUsed + '\'' + ", contractAddress='" + getContractAddress() + '\'' + ", root='" + getRoot() + '\'' + ", status='" + getStatus() + '\'' + ", from='" + getFrom() + '\'' + ", to='" + getTo() + '\'' + ", logs=" + getLogs() + ", logsBloom='" + getLogsBloom() + '\'' + '}';
    }
}
