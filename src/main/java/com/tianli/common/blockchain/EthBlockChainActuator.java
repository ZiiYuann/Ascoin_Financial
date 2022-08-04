package com.tianli.common.blockchain;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.tianli.chain.dto.EthGasAPIResponse;
import com.tianli.common.ConfigConstants;
import com.tianli.common.HttpUtils;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.mconfig.ConfigService;
import com.tianli.tool.judge.JsonObjectTool;
import com.tianli.tool.time.TimeTool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.web3j.abi.DefaultFunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Uint;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.JsonRpc2_0Web3j;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.utils.Numeric;
import party.loveit.bip44forjava.utils.Bip44Utils;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class EthBlockChainActuator {

    @Resource
    private JsonRpc2_0Web3j ethWeb3j;

    @Resource
    private ConfigService configService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;


    /**
     * 查询合约
     *
     * @param contractName 合约名 -> config表中可以获取到其对应的合约地址
     * @param methodName 合约方法名
     * @param params 方法调用参数
     * @return 返回Result, 0标识成功 其他为失败, msg为错误详情   data实际为EthCall
     * @throws IOException 可能的异常
     */
    public Result ethCall(String contractName, String methodName, List<Type> params) throws IOException {
        return ethCall(contractName, methodName, params, Utf8String.class);
    }

    public <T extends Type> Result ethCall(String contractName, String methodName, List<Type> params, Class<T> result) throws IOException {
        String curContractAddress = configService._get(contractName);
        List.of(TypeReference.create(Uint.class));
        EthCall send = ethWeb3j.ethCall(Transaction.createEthCallTransaction(null, curContractAddress,
                new DefaultFunctionEncoder().encodeFunction(
                        new Function(methodName, params,
                                List.of(TypeReference.create(result)))
                )), DefaultBlockParameterName.LATEST).send();
        return Result.success(send);
    }

    public TransactionInfo getTransactionByHash(String transactionHash) throws IOException {
        Request<?, EthTransaction> ethTransactionRequest = ethWeb3j.ethGetTransactionByHash(transactionHash);
        EthTransaction send = ethTransactionRequest.send();
        org.web3j.protocol.core.methods.response.Transaction result;
        if(Objects.isNull(send) || Objects.isNull(result = send.getResult())){
            return null;
        }
        // gasPrice
        BigInteger gasPrice = result.getGasPrice();
        Request<?, EthGetTransactionReceipt> ethGetTransactionReceiptRequest = ethWeb3j.ethGetTransactionReceipt(transactionHash);
        EthGetTransactionReceipt send2 = ethGetTransactionReceiptRequest.send();
        TransactionReceipt result2;
        if(Objects.isNull(send2) || Objects.isNull(result2 = send2.getResult())){
            return null;
        }
        // gasUsed
        BigInteger gasUsed = result2.getGasUsed();
        // fee
        BigInteger fee = gasUsed.multiply(gasPrice);
        // status  0x1成功 / 0x0失败
        String status = result2.getStatus();
        return TransactionInfo.builder()
                .status(status)
                .status(status)
                .fee(fee)
                .transaction(send)
                .transactionReceipt(send2)
                .build();
    }


    /**
     * @param transactionHash 交易hash
     * @return -1 pending状态, 0: 失败  1: 成功
     */
    public TransactionResult roughJudgmentTransactionByHash(String transactionHash) {
        TransactionInfo transactionByHash;
        try {
            transactionByHash = getTransactionByHash(transactionHash);
        } catch (IOException e) {
            return TransactionResult.builder().build();
        }
        if(Objects.isNull(transactionByHash)){
            return TransactionResult.builder().build();
        }
        String status = transactionByHash.getStatus();
        EthGetTransactionReceipt transactionReceipt = transactionByHash.getTransactionReceipt();
        TransactionReceipt result = transactionReceipt.getResult();
        if(Objects.equals(status, "0x1") && Objects.nonNull(result)){
            List<Log> logs = result.getLogs();
            return TransactionResult.builder()
                    .success(true)
                    .fee(transactionByHash.getFee())
                    .logs(logs)
                    .build();
        }
        return TransactionResult.builder()
                .success(false)
                .fee(transactionByHash.getFee())
                .build();
    }

    public static EthCall getEthCallWithParam(JsonRpc2_0Web3j web3j, String address, String symbol, List<Type> params) throws IOException {
        return web3j.ethCall(Transaction.createEthCallTransaction(null, address,
                new DefaultFunctionEncoder().encodeFunction(
                        new Function(symbol, params,
                                List.of())
                )), DefaultBlockParameterName.LATEST).send();
    }

    public static String verifyList(List<Type> list){
        if (list == null || CollectionUtils.isEmpty(list)) {
            ErrorCodeEnum.CONTRACT_ADDRESS_ERROR.throwException();
        }
        String str = list.get(0).getValue().toString();
        if (StringUtils.isBlank(str)){
            ErrorCodeEnum.CONTRACT_ADDRESS_ERROR.throwException();
        }
        return str;
    }

    public String getSymbol(String address) throws IOException {
        String value = getEthCallWithParam(ethWeb3j, address, "symbol", List.of()).getValue();
        List<Type> list = FunctionReturnDecoder.decode(value,
                List.of(TypeReference.create((Class) Utf8String.class))
        );
        String symbol = verifyList(list);
        return symbol;
    }

    public String getName(String address) throws IOException {
        String value = getEthCallWithParam(ethWeb3j, address, "name", List.of()).getValue();
        List<Type> list = FunctionReturnDecoder.decode(value,
                List.of(TypeReference.create((Class) Utf8String.class))
        );
        String name = verifyList(list);
        return name;
    }

    /**
     * 转发主币
     */
    public Result mainTokenSendRawTransaction(long nonce, String toAddress, BigInteger value, String gas, String gasLimit, String password, String operation) {
        log.info(String.format("gas: %s, limit: %s", gas, gasLimit));
        String main_wallet_chain_id = configService.getOrDefault(ConfigConstants.ETH_CHAIN_ID, "1");
        BigInteger privateKey = Bip44Utils.getPathPrivateKey(Collections.singletonList(password), "m/44'/60'/0'/0/0");
        BigInteger gasPrice = new BigDecimal(gas).multiply(new BigDecimal("1000000000")).toBigInteger();
        RawTransaction rawTransaction = RawTransaction.createEtherTransaction(new BigInteger("" + nonce), gasPrice, new BigInteger(gasLimit), toAddress, value);
        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, Long.parseLong(main_wallet_chain_id), Credentials.create(ECKeyPair.create(privateKey)));
        String signedTransactionData = Numeric.toHexString(signedMessage);
        EthSendTransaction send = null;
        try {
            send = ethWeb3j.ethSendRawTransaction(signedTransactionData).send();
        } catch (IOException e) {
            log.error(String.format("上链操作[%s], 执行异常 !", operation), e);
        }
        if (Objects.isNull(send) || StringUtils.isBlank(send.getTransactionHash())) {
            System.out.println("时间: " + TimeTool.getDateTimeDisplayString(LocalDateTime.now()) + ", 上链失败!!  SEND => " + new Gson().toJson(send));
            return Result.fail(operation + "  上链失败");
        }
        System.out.println("时间: " + TimeTool.getDateTimeDisplayString(LocalDateTime.now()) + ", " + operation + " < HASH: " + send.getTransactionHash() + " >");
        return Result.success(send.getTransactionHash());
    }

}
