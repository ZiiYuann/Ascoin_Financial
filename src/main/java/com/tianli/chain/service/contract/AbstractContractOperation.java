package com.tianli.chain.service.contract;

import com.tianli.chain.entity.Coin;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import org.apache.commons.lang3.StringUtils;
import org.tron.tronj.abi.FunctionEncoder;
import org.tron.tronj.abi.datatypes.Address;
import org.tron.tronj.abi.datatypes.DynamicArray;
import org.tron.tronj.abi.datatypes.Function;
import org.tron.tronj.abi.datatypes.generated.Uint256;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author chenb
 * @apiNote
 * @since 2022-08-03
 **/
public abstract class AbstractContractOperation implements ContractOperation {

    /**
     * 校验地址
     *
     * @param address 类似以太坊地址
     * @return true or false
     */
    protected boolean validAddress(String address) {
        if (StringUtils.isBlank(address) || !address.startsWith("0x")) {
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

    /**
     * @param toAddress              归集地址
     * @param ids                    Address表中的id
     * @param tokenContractAddresses 币种合约地址列表 可以传入多个一次性归集 地址数*用户数<400 最好
     * @return data
     */
    protected String buildRecycleData(String toAddress, List<Long> ids, List<String> tokenContractAddresses) {
        return FunctionEncoder.encode(
                new Function("recycle", List.of(new Address(toAddress),
                        new DynamicArray<>(Uint256.class, ids.stream().map(e -> new Uint256(new BigInteger(e + ""))).collect(Collectors.toList())),
                        new DynamicArray<>(Address.class, tokenContractAddresses.stream().map(Address::new).collect(Collectors.toList())))
                        , new ArrayList<>()));
    }

    @Override
    public boolean isValidAddress(String address) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String computeAddress(long addressId) throws IOException {
        throw ErrorCodeEnum.NOT_OPEN.generalException();
    }

    @Override
    public String computeAddress(BigInteger addressId) throws IOException {
        throw ErrorCodeEnum.NOT_OPEN.generalException();
    }

    @Override
    public String computeAddress(String walletAddress, BigInteger addressId) throws IOException {
        throw ErrorCodeEnum.NOT_OPEN.generalException();
    }

    @Override
    public String recycle(String toAddress, List<Long> addressIds, List<String> tokenContractAddresses) {
        throw ErrorCodeEnum.NOT_OPEN.generalException();
    }

    @Override
    public Result<String> transfer(String to, BigInteger val, Coin coin) {
        if (coin.isMainToken()) {
            return mainTokenTransfer(to, val, coin);
        } else {
            return tokenTransfer(to, val, coin);
        }
    }

    /**
     * 代币转账
     */
    abstract Result<String> tokenTransfer(String to, BigInteger val, Coin coin);

    /**
     * 主币转账
     */
    abstract Result<String> mainTokenTransfer(String to, BigInteger val, Coin coin);

}
