package com.tianli.chain.service.contract;

import com.google.gson.Gson;
import com.tianli.currency.enums.CurrencyAdaptType;
import com.tianli.exception.Result;
import com.tianli.tool.time.TimeTool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.web3j.abi.DefaultFunctionEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Uint;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.JsonRpc2_0Web3j;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.utils.Numeric;
import party.loveit.bip44forjava.utils.Bip44Utils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author chenb
 * @apiNote
 * @since 2022-08-03
 **/
@Slf4j
@Component
public abstract class Web3jContractOperation extends AbstractContractOperation {

    @Override
    public boolean isValidAddress(String address) {
        return super.validAddress(address);
    }

    /**
     * 代币转账
     */
    protected Result tokenTransfer(String to, BigInteger val, CurrencyAdaptType currencyAdaptType, String gasLimit) {
        Result result = null;
        try {
            result = this.tokenSendRawTransaction(
                    FunctionEncoder.encode(
                            new Function("transfer", List.of(new Address(to), new Uint(val)), new ArrayList<>())
                    ),
                    currencyAdaptType.getContractAddress(),
                    gasLimit,
                    String.format("转账%s", currencyAdaptType.getCurrencyCoin().getName()));
        } catch (Exception ignored) {
        }

        return result;
    }

    /**
     * 归集
     */
    protected String recycle(String toAddress, CurrencyAdaptType currencyAdaptType,String gasLimit, List<Long> addressId, List<String> addresses) {
        Result result;
        try {
            result = this.tokenSendRawTransaction(
                    FunctionEncoder.encode(
                            new Function("recycle", List.of(new Address(toAddress),
                                    new DynamicArray<>(Uint256.class, addressId.stream().map(e -> new Uint256(new BigInteger(e + ""))).collect(Collectors.toList())),
                                    new DynamicArray<>(Address.class, addresses.stream().map(Address::new).collect(Collectors.toList())))
                                    , new ArrayList<>())),
                    currencyAdaptType.getContractAddress(),
                    gasLimit,
                    "归集: ");
            return (String) result.getData();
        } catch (Exception ignored) {
            return null;
        }
    }

    protected String computeAddress(String walletAddress, BigInteger uid,String contractAddress) throws IOException {
        EthCall send = this.getWeb3j().ethCall(Transaction.createEthCallTransaction(null, contractAddress,
                new DefaultFunctionEncoder().encodeFunction(
                        new Function("computeAddress", List.of(new Address(walletAddress), new Uint(uid)),
                                List.of())
                )), DefaultBlockParameterName.LATEST).send();
        Address address = new Address(send.getValue());
        return address.getValue();
    }

    /**
     * 获取地址链上的最新的nance值,防止双花
     *
     * @param address eth地址
     */
    public Long getNonce(String address) {
        Long nance = null;
        try {
            EthGetTransactionCount send = this.getWeb3j().ethGetTransactionCount(address, DefaultBlockParameterName.PENDING).send();
            nance = send.getTransactionCount().longValue();
        } catch (Exception ignored) {
        }
        return Objects.isNull(nance) ? null : nance;
    }

    /**
     * 代币转账
     */
    public Result tokenSendRawTransaction(String chainParams, String tokenContractAddress, String gasLimit, String operation) {
        String password = this.getMainWalletPassword();
        String address = this.getMainWalletAddress();
        String gas = this.getGas();
        BigInteger nonce = BigInteger.valueOf(getNonce(address));
        Long chainId = this.getChainId();
        return tokenSendRawTransaction(nonce, chainId, tokenContractAddress, chainParams, gas, gasLimit, password, operation);
    }


    /**
     * 代币转账
     *
     * @param nonce                随机数，防止双花攻击
     * @param chainId              链id，防止双花攻击
     * @param tokenContractAddress 代币合约地址
     * @param chainParams          RawTransaction 中的 data 链参数【事件、地址、参数】
     * @param gas                  单位：wei 1000000000wei = 1Gwei  矿工费 费用越大，区块链优先处理的速度越快 一次交易约消费 21000 Gwei ~= 0.000021eth
     * @param gasLimit             最高的gas限制，如果交易超过了gasLimit则重置，但是已经消费的不会返回
     * @param password             私钥
     * @param operation            操作信息
     * @return 结果
     */
    public Result tokenSendRawTransaction(BigInteger nonce, Long chainId, String tokenContractAddress, String chainParams, String gas
            , String gasLimit, String password, String operation) {
        log.info("gas:{}, limit: {}", gas, gasLimit);

        BigInteger gasPrice = new BigDecimal(gas).multiply(new BigDecimal("1000000000")).toBigInteger();
        BigInteger privateKey = Bip44Utils.getPathPrivateKey(Collections.singletonList(password), "m/44'/60'/0'/0/0");


        RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, gasPrice, new BigInteger(gasLimit)
                , tokenContractAddress, chainParams);

        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, chainId, Credentials.create(ECKeyPair.create(privateKey)));
        String signedTransactionData = Numeric.toHexString(signedMessage);
        EthSendTransaction send = null;
        try {
            send = this.getWeb3j().ethSendRawTransaction(signedTransactionData).send();
        } catch (IOException e) {
            log.error(String.format("上链操作[%s], 执行异常 !", operation), e);
        }
        if (Objects.isNull(send) || StringUtils.isBlank(send.getTransactionHash())) {
            log.error("时间: " + TimeTool.getDateTimeDisplayString(LocalDateTime.now()) + ", 上链失败!!  SEND => " + new Gson().toJson(send));
            return Result.fail(operation + "  上链失败");
        }

        log.info("时间: " + TimeTool.getDateTimeDisplayString(LocalDateTime.now()) + ", " + operation + " < HASH: " + send.getTransactionHash() + " >");
        return Result.success(send.getTransactionHash());
    }

    protected abstract JsonRpc2_0Web3j getWeb3j();

    /**
     * 获取gas
     */
    protected abstract String getGas();

    /**
     * 获取主钱包地址
     */
    protected abstract String getMainWalletAddress();

    /**
     * 获取主钱包密码
     */
    protected abstract String getMainWalletPassword();

    /**
     * 获取chainId
     */
    protected abstract Long getChainId();

}
