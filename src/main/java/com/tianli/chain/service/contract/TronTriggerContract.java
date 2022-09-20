package com.tianli.chain.service.contract;

import com.google.protobuf.ByteString;
import com.tianli.common.ConfigConstants;
import com.tianli.common.blockchain.SignTransactionResult;
import com.tianli.currency.enums.TokenAdapter;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.mconfig.ConfigService;
import com.tianli.tool.time.TimeTool;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.jcajce.provider.digest.SHA256;
import org.bouncycastle.util.encoders.Hex;
import org.springframework.stereotype.Component;
import org.tron.api.GrpcAPI;
import org.tron.api.WalletGrpc;
import org.tron.common.utils.Base58Utils;
import org.tron.common.utils.ByteArray;
import org.tron.protos.Protocol;
import org.tron.protos.contract.BalanceContract;
import org.tron.protos.contract.SmartContractOuterClass;
import org.tron.tronj.abi.FunctionEncoder;
import org.tron.tronj.abi.datatypes.Address;
import org.tron.tronj.abi.datatypes.Function;
import org.tron.tronj.abi.datatypes.Uint;
import org.tron.tronj.abi.datatypes.generated.Uint256;
import org.tron.tronj.crypto.SECP256K1;
import org.tron.tronj.crypto.tuweniTypes.Bytes32;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author  cs
 * @since  2022-01-06 2:27 下午
 */
@Component
public class TronTriggerContract extends AbstractContractOperation {

    @Override
    public String computeAddress(long uid) {
        return computeAddress(BigInteger.valueOf(uid));
    }

    public String computeAddress(BigInteger uid) {
        String address = configService.get(ConfigConstants.TRON_MAIN_WALLET_ADDRESS);
        return computeAddress(address, uid);
    }

    public String computeAddress(String walletAddress, BigInteger uid) {
        String contractAddress = configService.get(ConfigConstants.TRON_TRIGGER_ADDRESS);

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

    public String transfer(String ownerAddress, String toAddress, String contractAddress, BigInteger amount) {
        String data = FunctionEncoder.encode(
                new Function("transfer",
                        List.of(new Address(toAddress), new Uint256(amount)),
                        List.of()));
        return triggerSmartContract(ownerAddress, contractAddress, data, 40000000L);
    }

    public String recycle(String toAddress, List<Long> addressIds, List<String> tokenContractAddresses) {
        String ownerAddress = configService.get(ConfigConstants.TRON_MAIN_WALLET_ADDRESS);
        String contractAddress = configService.getOrDefault(ConfigConstants.TRON_TRIGGER_ADDRESS, "TEuLfwtYM83r4TjkewRWFFFS1inHzdpsP2");
        if (toAddress == null || toAddress.isEmpty()) toAddress = ownerAddress;

        String data = super.buildRecycleData(toAddress, addressIds, tokenContractAddresses);
        return triggerSmartContract(ownerAddress, contractAddress, data, 1000000000L);
    }

    @Override
    public Result tokenTransfer(String to, BigInteger val, TokenAdapter tokenAdapter) {
        String ownerAddress = configService.get(ConfigConstants.TRON_MAIN_WALLET_ADDRESS);
        String contractAddress = tokenAdapter.getContractAddress();
        String data = org.tron.tronj.abi.FunctionEncoder.encode(
                new org.tron.tronj.abi.datatypes.Function("transfer",
                        List.of(new org.tron.tronj.abi.datatypes.Address(to), new org.tron.tronj.abi.datatypes.generated.Uint256(val)),
                        List.of()));
        return Result.success(triggerSmartContract(ownerAddress, contractAddress, data, 40000000L));
    }

    @Override
    Result mainTokenTransfer(String to, BigInteger val, TokenAdapter tokenAdapter) {
        throw ErrorCodeEnum.NOT_OPEN.generalException();
    }

    public String triggerSmartContract(String ownerAddress, String contractAddress, String data, long feeLimit) {
        SmartContractOuterClass.TriggerSmartContract trigger = SmartContractOuterClass.TriggerSmartContract.newBuilder()
                .setOwnerAddress(address2ByteString(ownerAddress))
                .setContractAddress(address2ByteString(contractAddress))
                .setData(parseHex(data)).build();
        GrpcAPI.TransactionExtention txnExt = blockingStub.triggerContract(trigger);

        Protocol.Transaction signedTxn;
        String txid;
        if (feeLimit > 0L) {
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
        if (!ret.getResult()) ErrorCodeEnum.throwException(ret.toString());
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
        byte[] txid = digest.digest();
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
        if (bytes == null) ErrorCodeEnum.ADDRESS_ERROR.throwException();
        return bytes;
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

    @Override
    public boolean isValidAddress(String address) {
        byte[] bytes = Base58Utils.decode58Check(address);
        return Base58Utils.addressValid(bytes);
    }

    @Override
    public boolean successByHash(String hash) {
        byte[] decode = Hex.decode(hash);
        Protocol.Transaction transaction =
                blockingStub.getTransactionById(GrpcAPI.BytesMessage.newBuilder().setValue(ByteString.copyFrom(decode)).build());

       if (CollectionUtils.isEmpty(transaction.getRetList())){
           return false;
       }
        int status = transaction.getRet(0).getContractRetValue();
        return status == Protocol.Transaction.Result.contractResult.SUCCESS_VALUE;
    }

    @Override
    public BigDecimal mainBalance(String address) {
        if (StringUtils.isBlank(address)) ErrorCodeEnum.throwException("请输入地址");
        Protocol.Account account;
        byte[] addressBytes = Base58Utils.decodeFromBase58Check(address);
        if (addressBytes == null) {
            ErrorCodeEnum.throwException("地址解析异常");
        }
        Protocol.Account request = Protocol.Account.newBuilder().setAddress(ByteString.copyFrom(addressBytes)).build();
        account = blockingStub.getAccount(request);
        if (account == null) {
            ErrorCodeEnum.throwException("暂无trx余额信息");
            return BigDecimal.ZERO;
        } else {
            return new BigDecimal(account.getBalance());
        }
    }

    @Override
    public BigDecimal tokenBalance(String address, TokenAdapter tokenAdapter) {
        byte[] ownerAddresses ;
        ownerAddresses = Base58Utils.decodeFromBase58Check(address);
        if (ownerAddresses == null) ErrorCodeEnum.ADDRESS_ERROR.throwException();

        String methodStr = "balanceOf(address)";
        String argsStr = "\"" + address + "\"";
        boolean isHex = false;
        byte[] input = Hex.decode(Base58Utils.parseMethod(methodStr, argsStr, isHex));
        byte[] contractAddress = Base58Utils.decodeFromBase58Check(tokenAdapter.getContractAddress());
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
            ErrorCodeEnum.throwException(String.format("[%s]获取%s代币余额失败", "tron", tokenAdapter.getCurrencyCoin().name()));
        }
        Protocol.Transaction transaction = transactionExtention.getTransaction();
        if (transaction.getRetCount() != 0) {
            byte[] result = transactionExtention.getConstantResult(0).toByteArray();
            String amount = org.spongycastle.util.encoders.Hex.toHexString(result);
            return new BigDecimal(new BigInteger(amount, 16));

        }
        return BigDecimal.ZERO;
    }


    @Resource
    private ConfigService configService;
    @Resource
    private WalletGrpc.WalletBlockingStub blockingStub;

}
