package com.tianli.tron.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.protobuf.InvalidProtocolBufferException;
import com.tianli.address.AddressService;
import com.tianli.address.controller.AddressWebhooksDTO;
import com.tianli.charge.ChargeService;
import com.tianli.charge.ChargeType;
import com.tianli.charge.controller.ChargeWebhooksDTO;
import com.tianli.charge.mapper.Charge;
import com.tianli.common.CommonFunction;
import com.tianli.common.Constants;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.dividends.settlement.ChargeSettlementService;
import com.tianli.dividends.settlement.mapper.ChargeSettlement;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.management.ruleconfig.ConfigConstants;
import com.tianli.mconfig.ConfigService;
import com.tianli.tron.mapper.Trc20Tx;
import com.tianli.tron.mapper.Trc20TxMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.tron.api.GrpcAPI;
import org.tron.api.WalletGrpc;
import org.tron.common.utils.Base58Utils;
import org.tron.common.utils.ByteArray;
import org.tron.protos.Protocol;
import org.tron.protos.contract.SmartContractOuterClass;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @Author cs
 * @Date 2022-01-07 2:44 下午
 */
@Service
public class TronService {

    @Resource
    private WalletGrpc.WalletBlockingStub blockingStub;
    @Resource
    private ConfigService configService;
    @Resource
    private Trc20TxMapper trc20TxMapper;
    @Resource
    private AddressService addressService;
    @Resource
    private ChargeService chargeService;
    @Resource
    private ChargeSettlementService chargeSettlementService;

    public static final String TRANSFER = "ddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef";
    public static final String USDT_CONTRACT = "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t";
    public static final String USDC_CONTRACT = "TEkxiTehnzSmSe2XqrBj4w32RUN966rdz8";
    public static final String TRX_BLOCK_COUNT = "trx_block_count";

    @Transactional
    public void auto(Long blockNum) {
        Long oldBlockNum = blockNum - 1;
        GrpcAPI.BlockExtention blockByNum = blockingStub.getBlockByNum2(GrpcAPI.NumberMessage.newBuilder().setNum(blockNum).build());
        Protocol.BlockHeader.raw blockHeader = blockByNum.getBlockHeader().getRawData();
        long timestamp = blockHeader.getTimestamp();

        GrpcAPI.TransactionInfoList transactionInfoByBlockNum = blockingStub.getTransactionInfoByBlockNum(GrpcAPI.NumberMessage.newBuilder().setNum(blockNum).build());
        List<Protocol.TransactionInfo> transactionInfoList = transactionInfoByBlockNum.getTransactionInfoList();
        Map<String, Protocol.TransactionInfo> transactionInfoMap = transactionInfoList.stream().collect(Collectors.toMap(e -> ByteArray.toHexString(e.getId().toByteArray()), e -> e));

        List<GrpcAPI.TransactionExtention> transactionsList = blockByNum.getTransactionsList();
        String myAddress = configService.get(ConfigConstants.TRON_MAIN_WALLET_ADDRESS);
        for (GrpcAPI.TransactionExtention tx : transactionsList) {
            //获取txid
            String txid = ByteArray.toHexString(tx.getTxid().toByteArray());
            Protocol.TransactionInfo transactionInfo = transactionInfoMap.get(txid);
            updateTx(txid, myAddress, tx, transactionInfo, timestamp, blockNum);
        }
        boolean cas = configService.cas(TRX_BLOCK_COUNT, oldBlockNum.toString(), blockNum.toString());
        if (!cas) ErrorCodeEnum.SYSTEM_ERROR.throwException();
    }

    private void updateTx(String txid, String myAddress, GrpcAPI.TransactionExtention tx, Protocol.TransactionInfo transactionInfo, long timestamp, long blockNum) {

        LocalDateTime blocktime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.of("UTC+8"));

        Protocol.Transaction transaction = tx.getTransaction();
        if (!CollectionUtils.isEmpty(transaction.getRetList())) {
            if (transaction.getRetCount() > 0) {
                int status = transaction.getRet(0).getContractRetValue();
                boolean success = status == Protocol.Transaction.Result.contractResult.SUCCESS_VALUE;
                Protocol.Transaction.raw rawData = transaction.getRawData();
                if (!CollectionUtils.isEmpty(rawData.getContractList())) {
                    Protocol.Transaction.Contract contract = rawData.getContract(0);
                    if (Protocol.Transaction.Contract.ContractType.TriggerSmartContract.getNumber() == contract.getType().getNumber()) {
                        if (success) {
                            for (Protocol.TransactionInfo.Log log : transactionInfo.getLogList()) {
                                if (log.getTopicsCount() == 3) {
                                    String contractAddress = Base58Utils.encode58Check(ByteArray.fromHexString("41" + ByteArray.toHexString(log.getAddress().toByteArray())));
                                    //判断币种类型， 当前只有usdt 如果后续需要加币 修改这里
                                    TokenCurrencyType tokenType = null;
                                    if(USDC_CONTRACT.equals(contractAddress)) tokenType = TokenCurrencyType.usdc_trc20;
                                    if(USDT_CONTRACT.equals(contractAddress)) tokenType = TokenCurrencyType.usdt_trc20;
                                    if(!ObjectUtils.isEmpty(tokenType)) {
                                        String topic0 = ByteArray.toHexString(log.getTopics(0).toByteArray());
                                        if (TRANSFER.equals(topic0)) {
                                            String topic1 = ByteArray.toHexString(log.getTopics(1).toByteArray()).substring(24);
                                            String ownerAddress = "41" + topic1;
                                            ownerAddress = Base58Utils.encode58Check(ByteArray.fromHexString(ownerAddress));

                                            String topic2 = ByteArray.toHexString(log.getTopics(2).toByteArray()).substring(24);
                                            String toAddress = "41" + topic2;
                                            toAddress = Base58Utils.encode58Check(ByteArray.fromHexString(toAddress));

                                            String data = ByteArray.toHexString(log.getData().toByteArray());
                                            data = new BigInteger(data, 16).toString(10);
                                            BigInteger amount = new BigInteger(data);

                                            boolean valueBool = ((amount.compareTo(BigInteger.ZERO) > 0) && (amount.toString().length() <= 38));
                                            //如果收款地址是Address表中的地址  说明有人充钱了
                                            boolean to = addressService.getByTron(toAddress) != null;
                                            //如果发送地址是钱包主地址    说明有人提现了（需要系统上链提现的版本，现版本是手动转账）
                                            boolean from = ownerAddress.equals(myAddress);
                                            if (valueBool && (to||from)) {
                                                long id = CommonFunction.generalId();
                                                Protocol.ResourceReceipt receipt = transactionInfo.getReceipt();
                                                BigInteger net_fee = new BigInteger(receipt.getNetFee() + "");
                                                BigInteger energy_fee = new BigInteger(receipt.getEnergyFee() + "");
                                                Trc20Tx trc20Tx = Trc20Tx.builder().id(id)
                                                        .txid(txid).block(blockNum).owner_address(ownerAddress)
                                                        .contract_address(contractAddress)
                                                        .to_address(toAddress).amount(amount)
                                                        .create_time(blocktime).status(status)
                                                        .net_fee(net_fee)
                                                        .energy_fee(energy_fee)
                                                        .net_usage(receipt.getNetUsage())
                                                        .energy_usage(receipt.getEnergyUsage())
                                                        .energy_usage_total(receipt.getEnergyUsageTotal())
                                                        .origin_energy_usage(receipt.getOriginEnergyUsage())
                                                        .build();
                                                trc20TxMapper.insert(trc20Tx);
                                                String finalOwnerAddress = ownerAddress;
                                                String finalToAddress = toAddress;
                                                if(to && from) {
                                                    boolean i = true;
                                                }
                                                if(to) {
                                                    //异步执行充值操作
                                                    final TokenCurrencyType t = tokenType;
                                                    CompletableFuture.runAsync(() -> {
                                                        AddressWebhooksDTO dto = new AddressWebhooksDTO();
                                                        dto.setTxid(txid);
                                                        dto.setCreate_time(blocktime);
                                                        dto.setBlock("" + blockNum);
                                                        dto.setFrom_address(finalOwnerAddress);
                                                        dto.setTo_address(finalToAddress);
                                                        dto.setType(t);
                                                        dto.setSn("C" + CommonFunction.generalSn(id));
                                                        dto.setValue(amount);
                                                        chargeService.receive(dto);
                                                    }, Constants.COMPLETABLE_FUTURE_EXECUTOR);
                                                }
                                                if(from) {
                                                    //异步执行提现操作
                                                    CompletableFuture.runAsync(() -> {
                                                        Charge c = chargeService.getOne(Wrappers.<Charge>lambdaQuery().eq(Charge::getTxid, txid).eq(Charge::getCharge_type, ChargeType.withdraw));
                                                        if(c != null) {
                                                            chargeService.withdrawWebhooks(ChargeWebhooksDTO.builder()
                                                                    .status("success").txid(txid)
                                                                    .miner_fee(net_fee.add(energy_fee)).sn(c.getSn())
                                                                    .miner_fee_type(TokenCurrencyType.trx).build());
                                                        } else {
                                                            ChargeSettlement cs = chargeSettlementService.getOne(Wrappers.<ChargeSettlement>lambdaQuery().eq(ChargeSettlement::getTxid, txid));
                                                            if (cs != null) {
                                                                chargeService.withdrawWebhooks(ChargeWebhooksDTO.builder()
                                                                        .status("success").txid(txid)
                                                                        .miner_fee(net_fee.add(energy_fee)).sn(cs.getSn())
                                                                        .miner_fee_type(TokenCurrencyType.trx).build());
                                                            }
                                                        }
                                                    }, Constants.COMPLETABLE_FUTURE_EXECUTOR);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            SmartContractOuterClass.TriggerSmartContract ac = null;
                            try {
                                ac = contract.getParameter().unpack(SmartContractOuterClass.TriggerSmartContract.class);
                            } catch (InvalidProtocolBufferException e) {
                                ErrorCodeEnum.SYSTEM_ERROR.throwException();
                            }
                            String ownerAddress = Base58Utils.encode58Check(ac.getOwnerAddress().toByteArray());
                            String contractAddress = Base58Utils.encode58Check(ac.getContractAddress().toByteArray());
                            if (USDT_CONTRACT.equals(contractAddress) || USDC_CONTRACT.equals(contractAddress)) {
                                String toAddress = null;
                                BigInteger amount = BigInteger.ZERO;
                                String hexString = ByteArray.toHexString(ac.getData().toByteArray());
                                if (hexString.length() > 8) {
                                    String data = hexString.substring(8);
                                    List<String> strList = Base58Utils.getStrList(data, 64);
                                    if (strList.size() == 2) {
                                        StringBuilder stringBuilder = new StringBuilder(strList.get(0));
                                        stringBuilder.delete(0, 24);
                                        stringBuilder.insert(0, 41);
                                        toAddress = stringBuilder.toString();
                                        toAddress = Base58Utils.encode58Check(ByteArray.fromHexString(toAddress));
                                        String amountStr = strList.get(1);
                                        amountStr = new BigInteger(amountStr, 16).toString(10);
                                        amount = new BigInteger(amountStr);
                                    }
                                }
                                boolean to = toAddress != null && addressService.getByTron(toAddress) != null;
                                boolean from = ownerAddress.equals(myAddress);
                                boolean valueBool = ((amount.compareTo(BigInteger.ZERO) > 0) && (amount.toString().length() <= 38));
                                if (valueBool && (to || from)) {
                                    Protocol.ResourceReceipt receipt = transactionInfo.getReceipt();
                                    BigInteger net_fee = new BigInteger(receipt.getNetFee() + "");
                                    BigInteger energy_fee = new BigInteger(receipt.getEnergyFee() + "");
                                    Trc20Tx trc20Tx = Trc20Tx.builder().id(CommonFunction.generalId())
                                            .txid(txid).block(blockNum).owner_address(ownerAddress)
                                            .contract_address(contractAddress)
                                            .to_address(toAddress).amount(amount)
                                            .create_time(blocktime).status(status)
                                            .net_fee(net_fee)
                                            .energy_fee(energy_fee)
                                            .net_usage(receipt.getNetUsage())
                                            .energy_usage(receipt.getEnergyUsage())
                                            .energy_usage_total(receipt.getEnergyUsageTotal())
                                            .origin_energy_usage(receipt.getOriginEnergyUsage())
                                            .build();
                                    trc20TxMapper.insert(trc20Tx);
                                    if(from) {
                                        CompletableFuture.runAsync(() -> {
                                            Charge c = chargeService.getOne(Wrappers.<Charge>lambdaQuery().eq(Charge::getTxid, txid));
                                            if(c != null) {
                                                chargeService.withdrawWebhooks(ChargeWebhooksDTO.builder()
                                                        .status("fail").txid(txid)
                                                        .miner_fee(net_fee.add(energy_fee)).sn(c.getSn())
                                                        .miner_fee_type(TokenCurrencyType.trx).build());
                                            } else {
                                                ChargeSettlement cs = chargeSettlementService.getOne(Wrappers.<ChargeSettlement>lambdaQuery().eq(ChargeSettlement::getTxid, txid));
                                                if (cs != null) {
                                                    chargeService.withdrawWebhooks(ChargeWebhooksDTO.builder()
                                                            .status("fail").txid(txid)
                                                            .miner_fee(net_fee.add(energy_fee)).sn(cs.getSn())
                                                            .miner_fee_type(TokenCurrencyType.trx).build());
                                                }
                                            }
                                        }, Constants.COMPLETABLE_FUTURE_EXECUTOR);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
