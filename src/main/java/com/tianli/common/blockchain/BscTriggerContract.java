package com.tianli.common.blockchain;

import com.tianli.exception.Result;
import com.tianli.management.ruleconfig.ConfigConstants;
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
public class BscTriggerContract {

    @Resource
    private ConfigService configService;

    @Resource
    private BscBlockChainActuator bscBlockChainActuator;

    @Resource
    private JsonRpc2_0Web3j bscWeb3j;

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

    /**
     * 归集
     *
     * @param toAddress 归集的地址
     * @param uid 用户的id(address表的ID)
     * @param erc20Address 归集的代币地址
     */
    public String recycle(String toAddress, long uid, String erc20Address) {
        return recycle(toAddress, List.of(uid), List.of(erc20Address));
    }

    /**
     * 归集接口
     * @param toAddress 归集地址 如果为null，修改为主钱包地址
     * @param uids Address表中的id
     * @param bep20AddressList erc20的币种合约地址列表 可以传入多个一次性归集 地址数*用户数<400 最好
     * @return 返回交易hash
     */
    public String recycle(String toAddress, List<Long> uids, List<String> bep20AddressList) {
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
                                    new DynamicArray(Uint256.class, uids.stream().map(e -> new Uint256(new BigInteger(e + ""))).collect(Collectors.toList())),
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
