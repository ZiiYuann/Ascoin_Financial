package com.tianli.chain.service.contract;

import com.tianli.common.ConfigConstants;
import com.tianli.common.blockchain.BscBlockChainActuator;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.common.blockchain.NetworkType;
import com.tianli.currency.enums.CurrencyAdaptType;
import com.tianli.exception.Result;
import com.tianli.mconfig.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.web3j.abi.DefaultFunctionEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.JsonRpc2_0Web3j;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class BscTriggerContract extends ContractService{

    @Resource
    private ConfigService configService;

    @Resource
    private BscBlockChainActuator bscBlockChainActuator;

    @Resource
    private JsonRpc2_0Web3j bscWeb3j;

    @Override
    public String computeAddress(long uid) throws IOException {
        return computeAddress(new BigInteger("" + uid));
    }

    public String computeAddress(BigInteger uid) throws IOException {
        String address = configService.get(ConfigConstants.BSC_MAIN_WALLET_ADDRESS);
        return computeAddress(address, uid);
    }

    public String computeAddress(String walletAddress, BigInteger uid) throws IOException {
        String contractAddress = configService.get(ConfigConstants.BSC_TRIGGER_ADDRESS);
        EthCall send = bscWeb3j.ethCall(Transaction.createEthCallTransaction(null, contractAddress,
                new DefaultFunctionEncoder().encodeFunction(
                        new Function("computeAddress", List.of(new Address(walletAddress), new Uint(uid)),
                                List.of())
                )), DefaultBlockParameterName.LATEST).send();
        Address address = new Address(send.getValue());
        return address.getValue();
    }



    public String recycle(String toAddress, List<Long> addressId, List<String> bep20AddressList) {
        String contractAddress = configService.get(ConfigConstants.BSC_TRIGGER_ADDRESS);
        String address = configService.get(ConfigConstants.BSC_MAIN_WALLET_ADDRESS);
        long nonce = bscBlockChainActuator.getNonce(address);
        String password = configService.get(ConfigConstants.MAIN_WALLET_PASSWORD);
        if(toAddress == null || toAddress.isEmpty()) toAddress = address;
        Result result = null;
        try {
            result = bscBlockChainActuator.tokenSendRawTransaction(nonce,
                    contractAddress,
                    FunctionEncoder.encode(
                            new Function("recycle", List.of(new Address(toAddress),
                                    new DynamicArray(Uint256.class, addressId.stream().map(e -> new Uint256(new BigInteger(e + ""))).collect(Collectors.toList())),
                                    new DynamicArray(Address.class, bep20AddressList.stream().map(Address::new).collect(Collectors.toList())))
                                    , new ArrayList<>())
                    ),
                    configService.getOrDefault(ConfigConstants.BSC_GAS_PRICE,"5"),
                    configService.getOrDefault(ConfigConstants.BSC_GAS_LIMIT_PLUS,"10000000"),
                    password, "归集: ");
            return (String) result.getData();
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public Result transfer(String to, BigInteger val, CurrencyCoin coin) {
        String address = configService.get(ConfigConstants.BSC_MAIN_WALLET_ADDRESS);
        String password = configService.get(ConfigConstants.MAIN_WALLET_PASSWORD);
        long nonce = bscBlockChainActuator.getNonce(address);
        Result result = null;
        try {
            CurrencyAdaptType currencyAdaptType = CurrencyAdaptType.get(coin, NetworkType.bep20);
            result = bscBlockChainActuator.tokenSendRawTransaction(nonce,
                    currencyAdaptType.getContractAddress(),
                    FunctionEncoder.encode(
                            new Function("transfer", List.of(new Address(to), new Uint(val)), new ArrayList<>())
                    ),
                    configService.getOrDefault(ConfigConstants.BSC_GAS_PRICE,"5"),
                    configService.getOrDefault(ConfigConstants.BSC_GAS_LIMIT,"800000"),
                    password, String.format("转账%s",coin.getName()));
        } catch (Exception ignored) {
        }

        return result;
    }

    public BigInteger bep20Balance(String address, String contract) {
        String balanceOf = null;
        try {
             balanceOf = bscWeb3j.ethCall(Transaction.createEthCallTransaction(null, contract, new DefaultFunctionEncoder().encodeFunction(
                    new Function("balanceOf", List.of(new Address(address)),
                            List.of(TypeReference.create(Uint.class)))
            )), DefaultBlockParameterName.LATEST).send().getValue();
        } catch (IOException e) {
            e.printStackTrace();
            return BigInteger.ZERO;
        }
        List<Type> list = FunctionReturnDecoder.decode(balanceOf,
                List.of(TypeReference.create((Class) Uint256.class))
        );
        balanceOf = list.get(0).getValue().toString();
        return new BigInteger(balanceOf);
    }

    public BigInteger bnbBalance(String address) {
        BigInteger balanceOf = null;
        try {
            balanceOf = bscWeb3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send().getBalance();

        } catch (IOException e) {
            e.printStackTrace();
            return BigInteger.ZERO;
        }
        return balanceOf;
    }
}
