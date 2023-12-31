package com.tianli.chain.service.contract;

import com.google.gson.Gson;
import com.tianli.chain.dto.TransactionReceiptLogDTO;
import com.tianli.chain.entity.Coin;
import com.tianli.chain.enums.ChainType;
import com.tianli.chain.enums.TransactionStatus;
import com.tianli.common.webhook.WebHookService;
import com.tianli.common.webhook.WebHookToken;
import com.tianli.currency.enums.TokenAdapter;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.tool.time.TimeTool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.web3j.abi.*;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Uint;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.JsonRpc2_0Web3j;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;
import party.loveit.bip44forjava.utils.Bip44Utils;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author chenb
 * @apiNote
 * @since 2022-08-03
 **/
@Slf4j
@Component
public abstract class Web3jContractOperation extends AbstractContractOperation {

    @Resource
    private WebHookService webHookService;

    static final BigDecimal DEFAULT_LOAD_FACTOR = BigDecimal.valueOf(1.5f);

    @Override
    public boolean isValidAddress(String address) {
        return super.validAddress(address);
    }

    /**
     * 归集
     */
    @Override
    public String recycle(String toAddress, List<Long> addressIds, List<String> tokenContractAddresses) {
        toAddress = Optional.ofNullable(toAddress).orElse(this.getMainWalletAddress());
        String hash;
        String data = super.buildRecycleData(toAddress, addressIds, tokenContractAddresses);
        try {
            hash = this.sendRawTransaction(this.getRecycleTriggerAddress() // 归集
                    , data, BigInteger.ZERO
                    , BigInteger.valueOf(Long.parseLong(getRecycleGasLimit())).multiply(BigInteger.valueOf(addressIds.size())), "归集: ");
            return hash;
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public TransactionStatus successByHash(String hash) {
        EthGetTransactionReceipt ethGetTransactionReceipt = getTransactionByHash(hash);

        if (Objects.isNull(ethGetTransactionReceipt.getResult())) {
            return TransactionStatus.PENDING;
        }

        if ("0x1".equals(ethGetTransactionReceipt.getResult().getStatus())) {
            return TransactionStatus.SUCCESS;
        }


        if ("0x0".equals(ethGetTransactionReceipt.getResult().getStatus())) {
            return TransactionStatus.FAIL;
        }

        throw ErrorCodeEnum.SYSTEM_ERROR.generalException();
    }

    @Override
    public BigDecimal mainBalance(String address) {
        BigInteger balanceOf;
        try {
            balanceOf = getWeb3j().ethGetBalance(address, DefaultBlockParameterName.LATEST).send().getBalance();

        } catch (IOException e) {
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
        return new BigDecimal(balanceOf);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public BigDecimal tokenBalance(String address, Coin coin) {
        String balanceOf;
        try {
            var transaction = Transaction.createEthCallTransaction(
                    null
                    , coin.getContract()
                    , new DefaultFunctionEncoder().encodeFunction(
                            new Function("balanceOf", List.of(new Address(address)),
                                    List.of(TypeReference.create(Uint.class)))
                    ));
            balanceOf = getWeb3j().ethCall(transaction, DefaultBlockParameterName.LATEST).send().getValue();
        } catch (IOException e) {
            e.printStackTrace();
            return BigDecimal.ZERO;
        }
        List<Type> list = FunctionReturnDecoder.decode(balanceOf,
                List.of(TypeReference.create((Class) Uint256.class))
        );
        balanceOf = list.get(0).getValue().toString();
        return new BigDecimal(balanceOf);
    }

    /**
     * 代币转账
     */
    @Override
    public String tokenTransfer(String to, BigInteger val, Coin coin) {
        String result = null;
        try {
            result = this.sendRawTransaction( // 代币
                    coin.getContract(),
                    FunctionEncoder.encode(
                            new Function("transfer", List.of(new Address(to), new Uint(val)), new ArrayList<>())
                    ),
                    BigInteger.ZERO,
                    BigInteger.valueOf(Long.parseLong(getTransferGasLimit())),
                    String.format("转账%s", coin.getName()));
        } catch (Exception ignored) {
            log.error("代币转账失败");
        }
        return result;
    }

    @Override
    public String mainTokenTransfer(String to, BigInteger val, Coin coin) {
        return sendRawTransaction(to, "", val, // 主笔
                BigInteger.valueOf(Long.parseLong(getTransferGasLimit())), "主笔转账：" + coin.getName());
    }

    /**
     * 预估矿工费
     */
    public BigInteger estimateGas(String from, String to, String gas, BigInteger gasLimit, BigInteger value, String data) {
        BigInteger gasPrice = Convert.toWei(gas, Convert.Unit.GWEI).toBigInteger();
        Transaction functionCallTransaction =
                Transaction.createFunctionCallTransaction(from, null, gasPrice
                        , gasLimit, to, value, data);
        try {
            return getWeb3j().ethEstimateGas(functionCallTransaction).send().getAmountUsed();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 计算合约地址
     */
    protected String computeAddress(String walletAddress, BigInteger addressId, String contractAddress) throws IOException {
        EthCall send = this.getWeb3j().ethCall(Transaction.createEthCallTransaction(null, contractAddress,
                new DefaultFunctionEncoder().encodeFunction(
                        new Function("computeAddress", List.of(new Address(walletAddress), new Uint(addressId)),
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
        } catch (Exception e) {
            log.error("获取nance失败" + e.getMessage());
        }
        return Objects.isNull(nance) ? null : nance;
    }

    /**
     * 转账
     *
     * @param to        代币转帐：代币合约地址       主币转账：to地址         归集：归集合约
     * @param data      代币转帐：数据              主币转账：null           归集：归集数据
     * @param value     代币转账：BigInteger.ZERO  主币转账：转账金额           归集：BigInteger.ZERO
     * @param gasLimit  最高的gas限制，如果交易超过了gasLimit则重置，但是已经消费的不会返回
     * @param operation 操作信息
     * @return 结果
     */
    public String sendRawTransaction(String to, String data, BigInteger value, BigInteger gasLimit, String operation) {

        String mainWalletAddress = this.getMainWalletAddress();
        // gas 单位：wei 1000000000wei = 1G wei  矿工费 费用越大，区块链优先处理的速度越快 一次交易约消费 21000 G wei ~= 0.000021eth
        String gas = this.getGas();
        // nonce 随机数，防止双花攻击
        BigInteger nonce = BigInteger.valueOf(getNonce(mainWalletAddress));
        // chainId 链id，防止双花攻击
        Long chainId = this.getChainId();
        BigDecimal gasPrice = Convert.toWei(gas, Convert.Unit.GWEI);

        // 预估矿工费
        BigDecimal estimateGas = new BigDecimal(this.estimateGas(mainWalletAddress, to, "0", null, value, data).toString());
        BigDecimal preCalGas = estimateGas.multiply(DEFAULT_LOAD_FACTOR);

        BigDecimal mainBalance = mainBalance(this.getMainWalletAddress());
        BigDecimal preCalBalance = gasPrice.multiply(preCalGas);
        if (mainBalance.compareTo(preCalBalance) < 0) {
            ChainType chainType = this.getChainType();
            String msg = chainType.getDisplay() + "上链失败，主币余额不足，至少需要:" + preCalBalance.toPlainString();
            webHookService.dingTalkSend(msg, WebHookToken.FINANCIAL_PRODUCT);
            throw ErrorCodeEnum.generateException(msg);
        }

        if (gasPrice.toBigInteger().multiply(gasLimit).compareTo(preCalGas.toBigInteger()) < 0) {
            ChainType chainType = this.getChainType();
            String msg = String.format("矿工费设置不足  [%s] gas:%s gasLimit:%s 预计算gas:%s"
                    , chainType.getDisplay(), gas, gasLimit.toString(), preCalGas.toString());
            webHookService.dingTalkSend(msg, WebHookToken.FINANCIAL_PRODUCT);
            throw ErrorCodeEnum.generateException(msg);
        }

        log.info("gas:{}, limit: {}", gas, gasLimit);
        BigInteger privateKey = Bip44Utils.getPathPrivateKey(Collections.singletonList(this.getMainWalletPassword()
        ), "m/44'/60'/0'/0/0");

        RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, gasPrice.toBigInteger(), gasLimit
                , to, value, data);

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
            return null;
        }

        log.info("时间: " + TimeTool.getDateTimeDisplayString(LocalDateTime.now()) + ", " + operation + " < HASH: " + send.getTransactionHash() + " >");
        return send.getTransactionHash();
    }

    public EthGetTransactionReceipt getTransactionByHash(String hash) {
        try {
            return getWeb3j().ethGetTransactionReceipt(hash).send();
        } catch (IOException e) {
            e.printStackTrace();
            log.error("执行查询数据异常, 执行异常 !");
        }
        return null;
    }

    /**
     * 根据hash获取交易状态
     *
     * @param hash 交易hash
     * @return 交易状态
     */
    public TransactionReceipt getTransactionReceipt(String hash) throws IOException {
        var transactionReceipt =
                getWeb3j().ethGetTransactionReceipt(hash).send().getTransactionReceipt();
        return transactionReceipt.orElse(null);
    }

    public BigDecimal getConsumeFee(String hash) throws IOException {
        org.web3j.protocol.core.methods.response.Transaction transaction = this.getTransaction(hash);
        BigInteger gasUsed = getTransactionReceipt(hash).getGasUsed();
        BigDecimal gasPrice = Convert.fromWei(new BigDecimal(transaction.getGasPrice()), Convert.Unit.GWEI);
        return new BigDecimal(gasUsed).multiply(gasPrice).multiply(BigDecimal.valueOf(0.000000001f));
    }

    public Integer decimals(String contractAddress) {

        try {
            List<TypeReference<?>> outputParameters = new ArrayList<>();
            Function function = new Function("decimals", List.of(),
                    outputParameters);
            String result = getWeb3j().ethCall(Transaction.createEthCallTransaction(null, contractAddress,
                    new DefaultFunctionEncoder().encodeFunction(
                            function
                    )), DefaultBlockParameterName.LATEST).send().getValue();
            if (result == null) {
                ErrorCodeEnum.WEB3J_DECIMALS.throwException();
            }

            var list = FunctionReturnDecoder.decode(result, Utils.convert(List.of(TypeReference.create(Uint.class))));
            return Integer.valueOf(list.get(0).getValue().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw ErrorCodeEnum.WEB3J_DECIMALS.generalException();
    }

    /**
     * 根据hash获取交易状态
     *
     * @param hash 交易hash
     * @return 交易状态
     */
    public org.web3j.protocol.core.methods.response.Transaction getTransaction(String hash) throws IOException {
        var transaction =
                getWeb3j().ethGetTransactionByHash(hash).send().getTransaction();
        return transaction.orElse(null);
    }

    public List<TransactionReceiptLogDTO> transactionReceiptLogDTOS(Coin coin, String hash) {
        try {
            TransactionReceipt transactionReceipt = this.getTransactionReceipt(hash);
            List<Log> logs = transactionReceipt.getLogs();
            return logs.stream().map(log -> {
                TransactionReceiptLogDTO transactionReceiptLogDTO = new TransactionReceiptLogDTO();
                transactionReceiptLogDTO.setAmount(TokenAdapter.alignment(coin, Numeric.toBigInt(log.getData())));

                List<String> topics = log.getTopics();
                transactionReceiptLogDTO.setFromAddress("0x" + topics.get(1).substring(26));
                return transactionReceiptLogDTO;
            }).collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
            throw ErrorCodeEnum.SYSTEM_ERROR.generalException();
        }
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

    protected abstract String getRecycleGasLimit();

    protected abstract String getTransferGasLimit();

    protected abstract String getRecycleTriggerAddress();

    protected abstract ChainType getChainType();

}
