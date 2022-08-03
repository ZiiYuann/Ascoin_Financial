package com.tianli.chain.service.contract;

import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.currency.enums.CurrencyAdaptType;
import com.tianli.exception.Result;
import org.apache.commons.lang3.StringUtils;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2022-08-03
 **/
public abstract class AbstractContractOperation implements ContractOperation {

    /**
     * 校验地址
     * @param address 类似以太坊地址
     * @return true or false
     */
    protected boolean validAddress(String address) {
        if(StringUtils.isBlank(address) || !address.startsWith("0x")){
            return false;
        }

        String cleanAddress = Numeric.cleanHexPrefix(address);
        try {
            Numeric.toBigIntNoPrefix(cleanAddress);
        } catch (NumberFormatException e) {
            return false;
        }
        return cleanAddress.length() == 40;
    }

    @Override
    public boolean isValidAddress(String address) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String computeAddress(long uid) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String computeAddress(BigInteger uid) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String computeAddress(String walletAddress, BigInteger uid) throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String recycle(String toAddress, CurrencyAdaptType currencyAdaptType, List<Long> addressId, List<String> addresses) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Result transfer(String to, BigInteger val, CurrencyCoin coin) {
        throw new UnsupportedOperationException();
    }
}
