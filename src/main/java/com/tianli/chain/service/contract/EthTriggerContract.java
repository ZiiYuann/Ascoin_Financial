package com.tianli.chain.service.contract;

import com.tianli.common.ConfigConstants;
import com.tianli.common.blockchain.EthBlockChainActuator;
import com.tianli.exception.Result;
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
public class EthTriggerContract extends ContractService {

    @Resource
    private ConfigService configService;

    @Resource
    private JsonRpc2_0Web3j ethWeb3j;

    @Resource
    private EthBlockChainActuator ethBlockChainActuator;

    @Override
    public String computeAddress(long uid) throws IOException {
        return computeAddress(new BigInteger("" + uid));
    }

    public String computeAddress(BigInteger uid) throws IOException {
        String address = configService.get(ConfigConstants.ETH_MAIN_WALLET_ADDRESS);
        return computeAddress(address, uid);
    }

    public String computeAddress(String walletAddress, BigInteger uid) throws IOException {
        String contractAddress = configService.get(ConfigConstants.ETH_TRIGGER_ADDRESS);
        EthCall send = ethWeb3j.ethCall(Transaction.createEthCallTransaction(null, contractAddress,
                new DefaultFunctionEncoder().encodeFunction(
                        new Function("computeAddress", List.of(new Address(walletAddress), new Uint(uid)),
                                List.of())
                )), DefaultBlockParameterName.LATEST).send();
        Address address = new Address(send.getValue());
        return address.getValue();
    }

    public String recycle(String toAddress, List<Long> uids, List<String> erc20AddressList) {
        String contractAddress = configService.get(ConfigConstants.ETH_TRIGGER_ADDRESS);
        String address = configService.get(ConfigConstants.BSC_MAIN_WALLET_ADDRESS);
        long nonce = ethBlockChainActuator.getNonce(address);
        String password = configService.get(ConfigConstants.MAIN_WALLET_PASSWORD);
        if(toAddress == null || toAddress.isEmpty()) toAddress = address;
        Result result = null;
        try {
            result = ethBlockChainActuator.tokenSendRawTransaction(nonce,
                    contractAddress,
                    FunctionEncoder.encode(
                            new Function("recycle", List.of(new Address(toAddress),
                                    new DynamicArray(Uint256.class, uids.stream().map(e -> new Uint256(new BigInteger(e + ""))).collect(Collectors.toList())),
                                    new DynamicArray(Address.class, erc20AddressList.stream().map(Address::new).collect(Collectors.toList())))
                                    , new ArrayList<>())
                    ),
                    ethBlockChainActuator.getPrice(),
                    configService.getOrDefault(ConfigConstants.ETH_GAS_LIMIT_PLUS,"800000"),
                    password, "归集: ");
            return (String) result.getData();
        } catch (Exception ignored) {
            return null;
        }
    }

    public BigInteger erc20Balance(String address, String contract) {
        String balanceOf = null;
        try {
            balanceOf = ethWeb3j.ethCall(Transaction.createEthCallTransaction(null, contract, new DefaultFunctionEncoder().encodeFunction(
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

    public BigInteger ethBalance(String address) {
        BigInteger balanceOf = null;
        try {
            balanceOf = ethWeb3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send().getBalance();

        } catch (IOException e) {
            e.printStackTrace();
            return BigInteger.ZERO;
        }
        return balanceOf;
    }
}