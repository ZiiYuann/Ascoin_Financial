package com.tianli.common.blockchain;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.protobuf.ByteString;
import com.tianli.currency_token.mapper.ChainType;
import com.tianli.currency_token.mapper.CurrencyCoinEnum;
import com.tianli.currency_token.transfer.mapper.TokenContract;
import com.tianli.currency_token.transfer.service.TokenContractService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.management.ruleconfig.ConfigConstants;
import com.tianli.mconfig.ConfigService;
import com.tianli.tool.time.TimeTool;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jcajce.provider.digest.SHA256;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.stereotype.Component;
import org.tron.api.GrpcAPI;
import org.tron.api.WalletGrpc;
import org.tron.common.crypto.Sha256Sm3Hash;
import org.tron.common.utils.Base58Utils;
import org.tron.common.utils.ByteArray;
import org.tron.protos.Protocol;
import org.tron.protos.contract.BalanceContract;
import org.tron.protos.contract.SmartContractOuterClass;
import org.tron.tronj.abi.FunctionEncoder;
import org.tron.tronj.abi.datatypes.Address;
import org.tron.tronj.abi.datatypes.DynamicArray;
import org.tron.tronj.abi.datatypes.Function;
import org.tron.tronj.abi.datatypes.Uint;
import org.tron.tronj.abi.datatypes.generated.Uint256;
import org.tron.tronj.crypto.SECP256K1;
import org.tron.tronj.crypto.tuweniTypes.Bytes32;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author cs
 * @Date 2022-01-06 2:27 下午
 */
@Component
public class TronTriggerContract {

    @Resource
    private ConfigService configService;
    @Resource
    private WalletGrpc.WalletBlockingStub blockingStub;
    @Resource
    private TokenContractService tokenContractService;

    public String computeAddress(long uid) {
        return computeAddress(BigInteger.valueOf(uid));
    }

    public String computeAddress(BigInteger uid) {
        String address = configService.get(ConfigConstants.TRON_MAIN_WALLET_ADDRESS);
        return computeAddress(address, uid);
    }

    public String computeAddress(String walletAddress, BigInteger uid) {
        String contractAddress = configService.get(ConfigConstants.TRON_CONTRACT_ADDRESS);

        String data = FunctionEncoder.encode(
                new Function("computeAddress",
                        List.of(new Address(walletAddress), new Uint(uid)),
                        List.of()));

        GrpcAPI.TransactionExtention transactionExtention = blockingStub.triggerConstantContract(
                SmartContractOuterClass.TriggerSmartContract.newBuilder()
                        .setContractAddress(address2ByteString(contractAddress))
                        .setData(parseHex(data)).build());
        ByteString constantResult = transactionExtention.getConstantResult(0);

        return Base58Utils.encode58Check(ByteArray.fromHexString("41" + ByteArray.toHexString(constantResult.toByteArray()).substring(24)));
    }

    public String transferUsdt(String toAddress, BigInteger amount) {
        String ownerAddress = configService.get(ConfigConstants.TRON_MAIN_WALLET_ADDRESS);
        return transfer(ownerAddress, toAddress, "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t", amount);
    }

    public String transferUsdc(String toAddress, BigInteger amount) {
        String ownerAddress = configService.get(ConfigConstants.TRON_MAIN_WALLET_ADDRESS);
        return transfer(ownerAddress, toAddress, "TEkxiTehnzSmSe2XqrBj4w32RUN966rdz8", amount);
    }

    public String transferToken(String toAddress, BigInteger amount, CurrencyCoinEnum token) {
        String ownerAddress = configService.get(ConfigConstants.TRON_MAIN_WALLET_ADDRESS);
        TokenContract tokenContract = tokenContractService.getOne(
                new LambdaQueryWrapper<TokenContract>()
                        .eq(TokenContract::getChain, ChainType.trc20)
                        .eq(TokenContract::getToken, token)
        );
        return transfer(ownerAddress, toAddress, tokenContract.getContract_address(), amount);
    }

    /**
     * trc20代币转账的方法
     * @param ownerAddress 发送地址
     * @param toAddress 接收地址
     * @param contractAddress 合约地址 后续如果需要其他的币种转账，修改这个合约地址即可
     * @param amount 转账数额
     * @return
     */
    public String transfer(String ownerAddress, String toAddress, String contractAddress, BigInteger amount) {
        String data = FunctionEncoder.encode(
                new Function("transfer",
                        List.of(new Address(toAddress), new Uint256(amount)),
                        List.of()));
        return triggerSmartContract(ownerAddress, contractAddress, data, 40000000L);
    }

    /**
     * 归集接口
     * @param toAddress 归集地址 如果为null，修改为主钱包地址
     * @param uids Address表中的id
     * @param trc20AddressList trc20的币种合约地址列表 可以传入多个一次性归集 地址数*用户数<400 最好
     * @return 返回交易hash
     */
    public String recycle(String toAddress, List<Long> uids, List<String> trc20AddressList) {
        String ownerAddress = configService.get(ConfigConstants.TRON_MAIN_WALLET_ADDRESS);
        String contractAddress = configService.getOrDefault(ConfigConstants.TRON_CONTRACT_ADDRESS, "TEuLfwtYM83r4TjkewRWFFFS1inHzdpsP2");
        if(toAddress == null || toAddress.isEmpty()) toAddress = ownerAddress;
        String data = FunctionEncoder.encode(
                new Function("recycle", List.of(new Address(toAddress),
                        new DynamicArray(Uint256.class, uids.stream().map(e -> new Uint256(new BigInteger(e + ""))).collect(Collectors.toList())),
                        new DynamicArray(Address.class, trc20AddressList.stream().map(Address::new).collect(Collectors.toList())))
                        , new ArrayList<>()));
        return triggerSmartContract(ownerAddress, contractAddress, data, 1000000000L);
    }

    public String triggerSmartContract(String ownerAddress, String contractAddress, String data, long feeLimit) {
        SmartContractOuterClass.TriggerSmartContract trigger = SmartContractOuterClass.TriggerSmartContract.newBuilder()
                .setOwnerAddress(address2ByteString(ownerAddress))
                .setContractAddress(address2ByteString(contractAddress))
                .setData(parseHex(data)).build();
        GrpcAPI.TransactionExtention txnExt = blockingStub.triggerContract(trigger);

        Protocol.Transaction signedTxn;
        String txid;
        if(feeLimit > 0L) {
            Protocol.Transaction transaction = txnExt.getTransaction();
            Protocol.Transaction txn = transaction.toBuilder()
                    .setRawData(transaction.getRawData().toBuilder().setFeeLimit(feeLimit).build()).build();
            SignTransactionResult result = signTransaction(txn, getKeyPair());
            txid = result.getTxid();
            signedTxn = result.getTxn();
        } else {
            signedTxn = signTransaction(txnExt, getKeyPair());
            txid = Hex.toHexString(txnExt.getTxid().toByteArray());
        }
        System.out.println("时间: " + TimeTool.getDateTimeDisplayString(LocalDateTime.now()) + ", " + " < HASH: " + txid + " >");
        GrpcAPI.Return ret = blockingStub.broadcastTransaction(signedTxn);
        System.out.println("======== Result ========\n" + ret.toString());
        if(!ret.getResult()) ErrorCodeEnum.throwException(ret.toString());
        return txid;
    }

    public SECP256K1.KeyPair getKeyPair() {
        String hexPrivateKey = configService.get(ConfigConstants.TRON_PRIVATE_KEY);
        return SECP256K1.KeyPair.create(SECP256K1.PrivateKey.create(Bytes32.fromHexString(hexPrivateKey)));
    }

    public Protocol.Transaction signTransaction(GrpcAPI.TransactionExtention txnExt, SECP256K1.KeyPair kp) {
        SECP256K1.Signature sig = SECP256K1.sign(Bytes32.wrap(txnExt.getTxid().toByteArray()), kp);
        return txnExt.getTransaction().toBuilder().addSignature(ByteString.copyFrom(sig.encodedBytes().toArray())).build();
    }

    public SignTransactionResult signTransaction(Protocol.Transaction txn, SECP256K1.KeyPair kp) {
        SHA256.Digest digest = new SHA256.Digest();
        digest.update(txn.getRawData().toByteArray());
        byte[] txid= digest.digest();
        SECP256K1.Signature sig = SECP256K1.sign(Bytes32.wrap(txid), kp);
        Protocol.Transaction transaction = txn.toBuilder().addSignature(ByteString.copyFrom(sig.encodedBytes().toArray())).build();
        return SignTransactionResult.builder().txn(transaction).txid(Hex.toHexString(txid)).build();
    }

    public static ByteString parseHex(String data) {
        return ByteString.copyFrom(Hex.decode(data));
    }

    public static ByteString address2ByteString(String address) {
        return ByteString.copyFrom(address2Bytes(address));
    }

    public static byte[] address2Bytes(String address) {
        byte[] bytes = Base58Utils.decodeFromBase58Check(address);
        if(bytes == null) ErrorCodeEnum.ADDRESS_ERROR.throwException();
        return bytes;
    }

    public BigDecimal trc20Balance(String ownerAddress, String contractAddrStr) {
        byte[] ownerAddresses = null;
        ownerAddresses = Base58Utils.decodeFromBase58Check(ownerAddress);
        if (ownerAddresses == null) ErrorCodeEnum.ADDRESS_ERROR.throwException();

        String methodStr = "balanceOf(address)";
        String argsStr = "\"" + ownerAddress + "\"";
        boolean isHex = false;
        byte[] input = Hex.decode(Base58Utils.parseMethod(methodStr, argsStr, isHex));
        byte[] contractAddress = Base58Utils.decodeFromBase58Check(contractAddrStr);
        if (contractAddress == null) ErrorCodeEnum.throwException("合约地址错误");

        SmartContractOuterClass.TriggerSmartContract.Builder builder = SmartContractOuterClass.TriggerSmartContract.newBuilder();
        builder.setOwnerAddress(ByteString.copyFrom(ownerAddresses));
        builder.setContractAddress(ByteString.copyFrom(contractAddress));
        builder.setData(ByteString.copyFrom(input));
        builder.setCallValue(0);

        SmartContractOuterClass.TriggerSmartContract triggerContract = builder.build();
        GrpcAPI.TransactionExtention transactionExtention;
        transactionExtention = blockingStub.triggerConstantContract(triggerContract);

        if (transactionExtention == null || !transactionExtention.getResult().getResult()) {
            ErrorCodeEnum.throwException("获取trc20代币余额失败");
        }
        Protocol.Transaction transaction = transactionExtention.getTransaction();
        if (transaction.getRetCount() != 0
                && transactionExtention.getConstantResult(0) != null
                && transactionExtention.getResult() != null) {
            byte[] result = transactionExtention.getConstantResult(0).toByteArray();
            String amount = org.spongycastle.util.encoders.Hex.toHexString(result);
            return new BigDecimal(new BigInteger(amount, 16));

        }
        return BigDecimal.ZERO;
    }

    public BigDecimal trxBalance(String ownerAddress) {
        if (StringUtils.isBlank(ownerAddress)) ErrorCodeEnum.throwException("请输入地址");
        Protocol.Account account;
        byte[] addressBytes = Base58Utils.decodeFromBase58Check(ownerAddress);
        if (addressBytes == null) {
            ErrorCodeEnum.throwException("地址解析异常");
        }
        ByteString addressBS = ByteString.copyFrom(addressBytes);
        Protocol.Account request = Protocol.Account.newBuilder().setAddress(addressBS).build();
        account = blockingStub.getAccount(request);
        if (account == null) {
            ErrorCodeEnum.throwException("暂无trx余额信息");
            return BigDecimal.ZERO;
        } else {
            return new BigDecimal(account.getBalance());
        }
    }


    public String transferTrx(String to, long amount) {
        String ownerAddress = configService.get(ConfigConstants.TRON_MAIN_WALLET_ADDRESS);
        BalanceContract.TransferContract contract = createTransferContract(to, ownerAddress, amount);
        GrpcAPI.TransactionExtention transactionExtention = blockingStub.createTransaction2(contract);
        return processTransactionExtention(transactionExtention);
    }

    public BalanceContract.TransferContract createTransferContract(String to, String owner, long amount) {
        BalanceContract.TransferContract.Builder builder = BalanceContract.TransferContract.newBuilder();
        ByteString bsTo = address2ByteString(to);
        ByteString bsOwner = address2ByteString(owner);
        builder.setToAddress(bsTo);
        builder.setOwnerAddress(bsOwner);
        builder.setAmount(amount);
        return builder.build();
    }

    private String processTransactionExtention(GrpcAPI.TransactionExtention transactionExtention) {
        if (transactionExtention == null) {
            return null;
        }
        GrpcAPI.Return ret = transactionExtention.getResult();
        if (!ret.getResult()) {
            System.out.println("Code = " + ret.getCode());
            System.out.println("Message = " + ret.getMessage().toStringUtf8());
            return null;
        }
        Protocol.Transaction transaction = transactionExtention.getTransaction();
        if (transaction.getRawData().getContractCount() == 0) {
            System.out.println("Transaction is empty");
            return null;
        }

        if (transaction.getRawData().getContract(0).getType()
                == Protocol.Transaction.Contract.ContractType.ShieldedTransferContract) {
            return null;
        }
        System.out.println("before sign transaction hex string is " +
                ByteArray.toHexString(transaction.toByteArray()));
        transaction = signTransaction(transactionExtention, getKeyPair());
//        showTransactionAfterSign(transaction);
        GrpcAPI.Return ret2 = blockingStub.broadcastTransaction(transaction);
        if (!ret2.getResult()) return null;
        return ByteArray.toHexString(Sha256Sm3Hash.hash(transaction.getRawData().toByteArray()));
    }
}
