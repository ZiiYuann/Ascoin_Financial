package com.tianli.wallet;

import com.tianli.charge.ChargeService;
import com.tianli.common.CommonFunction;
import com.tianli.common.async.AsyncService;
import com.tianli.common.lock.RedisLock;
import com.tianli.currency.CurrencyTokenEnum;
import com.tianli.currency.GraphService;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.currency.TransferGraphVO;
import com.tianli.currency_token.mapper.ChainType;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.management.ruleconfig.ConfigConstants;
import com.tianli.mconfig.ConfigService;
import com.tianli.wallet.mapper.MainWalletLog;
import com.tianli.wallet.mapper.MainWalletLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.mountcloud.graphql.GraphqlClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RestController;
import org.tron.api.GrpcAPI;
import org.tron.api.WalletGrpc;
import org.tron.common.utils.Base58Utils;
import org.tron.common.utils.ByteArray;
import org.tron.protos.Protocol;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RestController
public class WalletTransferService {

//    public static final String BSC_BLOCK_COUNT = "bsc_block_count";
//    public static final String ETH_BLOCK_COUNT = "eth_block_count";
    public final static String USDT_TRC20_CONTRACT = "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t";
    public final static String USDT_BEP20_CONTRACT = "0x55d398326f99059fF775485246999027B3197955";
    public final static String USDT_ERC20_CONTRACT = "0xdAC17F958D2ee523a2206206994597C13D831ec7";
    public final static String USDC_TRC20_CONTRACT = "TEkxiTehnzSmSe2XqrBj4w32RUN966rdz8";
    public final static String USDC_BEP20_CONTRACT = "0x8AC76a51cc950d9822D68b83fE1Ad97B32Cd580d";
    public final static String USDC_ERC20_CONTRACT = "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48";
    public static final String TRANSFER = "ddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef";


    @Transactional(rollbackFor = Exception.class)
    public void mainBscWalletScan(GraphqlClient graphqlClient) throws IOException {
        int step = 1;
        String myBscAddress = configService.get(ConfigConstants.BSC_MAIN_WALLET_ADDRESS);
        String block = configService.get(ConfigConstants.BSC_MAIN_WALLET_TRANSFER_BLOCK);
        long start = Long.parseLong(block);
        Long graphLastBlock = graphService.getGraphLastBlock(graphqlClient);
        if (graphLastBlock < start) {
            return;
        }

        List<TransferGraphVO> transferGraphVOSFrom = graphService.getTransferByFrom(graphqlClient, start, start + step, myBscAddress);
        transferGraphVOSFrom.forEach(transferGraphVO -> {
            CurrencyTokenEnum token = CurrencyTokenEnum.getToken(transferGraphVO.getCoinAddress().toLowerCase());
            if(USDT_BEP20_CONTRACT.equalsIgnoreCase(transferGraphVO.getCoinAddress()) || USDC_BEP20_CONTRACT.equalsIgnoreCase(transferGraphVO.getCoinAddress())){
                MainWalletLog mainWalletLog = MainWalletLog.builder()
                        .amount(transferGraphVO.getValue().divide(ChargeService.TEN_BILLION))
                        .block(transferGraphVO.getBlock().toString())
                        .chain_type(ChainType.bep20)
                        .create_time(new Timestamp(transferGraphVO.getTransferTime().longValue() * 1000).toLocalDateTime())
                        .currency_type(token.name())
                        .direction("out")
                        .id(CommonFunction.generalId())
                        .from_address(transferGraphVO.getFrom())
                        .to_address(transferGraphVO.getTo())
                        .txid(transferGraphVO.getId())
                        .build();
                mainWalletLogMapper.insert(mainWalletLog);
            }
        });


        List<TransferGraphVO> transferGraphVOSTo = graphService.getTransferByTo(graphqlClient, start, start + 1, myBscAddress);
        transferGraphVOSTo.forEach(transferGraphVO -> {
            CurrencyTokenEnum token = CurrencyTokenEnum.getToken(transferGraphVO.getCoinAddress().toLowerCase());
            if(USDT_BEP20_CONTRACT.equalsIgnoreCase(transferGraphVO.getCoinAddress()) || USDC_BEP20_CONTRACT.equalsIgnoreCase(transferGraphVO.getCoinAddress())){
                MainWalletLog mainWalletLog = MainWalletLog.builder()
                        .amount(transferGraphVO.getValue().divide(ChargeService.TEN_BILLION))
                        .block(transferGraphVO.getBlock().toString())
                        .chain_type(ChainType.bep20)
                        .create_time(new Timestamp(transferGraphVO.getTransferTime().longValue() * 1000).toLocalDateTime())
                        .currency_type(token.name())
                        .direction("in")
                        .id(CommonFunction.generalId())
                        .from_address(transferGraphVO.getFrom())
                        .to_address(transferGraphVO.getTo())
                        .txid(transferGraphVO.getId())
                        .build();
                mainWalletLogMapper.insert(mainWalletLog);
            }
        });

        boolean cas = configService.cas(ConfigConstants.BSC_MAIN_WALLET_TRANSFER_BLOCK, block, String.valueOf(Long.parseLong(block) + step));
        if (!cas) ErrorCodeEnum.SYSTEM_ERROR.throwException();
    }



    @Transactional(rollbackFor = Exception.class)
    public void mainEthWalletScan(GraphqlClient graphqlClient) throws IOException {
        int step = 1;
        String myBscAddress = configService.get(ConfigConstants.ETH_MAIN_WALLET_ADDRESS);
        String block = configService.get(ConfigConstants.ETH_MAIN_WALLET_TRANSFER_BLOCK);
        long start = Long.parseLong(block);
        Long graphLastBlock = graphService.getGraphLastBlock(graphqlClient);
        if (graphLastBlock < start) {
            return;
        }

        List<TransferGraphVO> transferGraphVOSFrom = graphService.getTransferByFrom(graphqlClient, start, start + step, myBscAddress);
        transferGraphVOSFrom.forEach(transferGraphVO -> {
            CurrencyTokenEnum token = CurrencyTokenEnum.getToken(transferGraphVO.getCoinAddress().toLowerCase());
            if(USDT_ERC20_CONTRACT.equalsIgnoreCase(transferGraphVO.getCoinAddress()) || USDC_ERC20_CONTRACT.equalsIgnoreCase(transferGraphVO.getCoinAddress())){
                MainWalletLog mainWalletLog = MainWalletLog.builder()
                        .amount(transferGraphVO.getValue().multiply(ChargeService.ONE_HUNDRED))
                        .block(transferGraphVO.getBlock().toString())
                        .chain_type(ChainType.erc20)
                        .create_time(new Timestamp(transferGraphVO.getTransferTime().longValue() * 1000).toLocalDateTime())
                        .currency_type(token.name())
                        .direction("out")
                        .id(CommonFunction.generalId())
                        .from_address(transferGraphVO.getFrom())
                        .to_address(transferGraphVO.getTo())
                        .txid(transferGraphVO.getId())
                        .build();
                mainWalletLogMapper.insert(mainWalletLog);
            }
        });


        List<TransferGraphVO> transferGraphVOSTo = graphService.getTransferByTo(graphqlClient, start, start + 1, myBscAddress);
        transferGraphVOSTo.forEach(transferGraphVO -> {
            CurrencyTokenEnum token = CurrencyTokenEnum.getToken(transferGraphVO.getCoinAddress().toLowerCase());
            if(USDT_ERC20_CONTRACT.equalsIgnoreCase(transferGraphVO.getCoinAddress()) || USDC_ERC20_CONTRACT.equalsIgnoreCase(transferGraphVO.getCoinAddress())){
                MainWalletLog mainWalletLog = MainWalletLog.builder()
                        .amount(transferGraphVO.getValue().multiply(ChargeService.ONE_HUNDRED))
                        .block(transferGraphVO.getBlock().toString())
                        .chain_type(ChainType.erc20)
                        .create_time(new Timestamp(transferGraphVO.getTransferTime().longValue() * 1000).toLocalDateTime())
                        .currency_type(token.name())
                        .direction("in")
                        .id(CommonFunction.generalId())
                        .from_address(transferGraphVO.getFrom())
                        .to_address(transferGraphVO.getTo())
                        .txid(transferGraphVO.getId())
                        .build();
                mainWalletLogMapper.insert(mainWalletLog);
            }
        });

        boolean cas = configService.cas(ConfigConstants.ETH_MAIN_WALLET_TRANSFER_BLOCK, block, String.valueOf(Long.parseLong(block) + step));
        if (!cas) ErrorCodeEnum.SYSTEM_ERROR.throwException();
    }


    @Transactional(rollbackFor = Exception.class)
    public void mainTrcWalletScan(Long blockNum) {

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
                            if (success && transactionInfo != null) {
                                for (Protocol.TransactionInfo.Log log : transactionInfo.getLogList()) {
                                    if (log.getTopicsCount() == 3) {
                                        String contractAddress = Base58Utils.encode58Check(ByteArray.fromHexString("41" + ByteArray.toHexString(log.getAddress().toByteArray())));
                                        //判断币种类型， 当前只有usdt 如果后续需要加币 修改这里
                                        TokenCurrencyType tokenType = null;
                                        if(USDT_TRC20_CONTRACT.equals(contractAddress)) tokenType = TokenCurrencyType.usdt_trc20;
                                        if(USDC_TRC20_CONTRACT.equals(contractAddress)) tokenType = TokenCurrencyType.usdc_trc20;
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
                                                if(valueBool && ownerAddress.equals(myAddress)) {
                                                    MainWalletLog mainWalletLog = MainWalletLog.builder()
                                                            .amount(amount.multiply(ChargeService.ONE_HUNDRED))
                                                            .block(blockNum.toString())
                                                            .chain_type(ChainType.trc20)
                                                            .create_time(blocktime)
                                                            .currency_type(tokenType.name())
                                                            .direction("out")
                                                            .id(CommonFunction.generalId())
                                                            .from_address(ownerAddress)
                                                            .to_address(toAddress)
                                                            .txid(txid)
                                                            .build();
                                                    mainWalletLogMapper.insert(mainWalletLog);
                                                } else if(valueBool && toAddress.equals(myAddress)) {
                                                    MainWalletLog mainWalletLog = MainWalletLog.builder()
                                                            .amount(amount.multiply(ChargeService.ONE_HUNDRED))
                                                            .block(blockNum.toString())
                                                            .chain_type(ChainType.trc20)
                                                            .create_time(blocktime)
                                                            .currency_type(tokenType.name())
                                                            .direction("in")
                                                            .id(CommonFunction.generalId())
                                                            .from_address(ownerAddress)
                                                            .to_address(toAddress)
                                                            .txid(txid)
                                                            .build();
                                                    mainWalletLogMapper.insert(mainWalletLog);
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
        }


        boolean cas = configService.cas(ConfigConstants.TRON_MAIN_WALLET_TRANSFER_BLOCK, oldBlockNum.toString(), blockNum.toString());
        if (!cas) ErrorCodeEnum.SYSTEM_ERROR.throwException();
    }

    @Resource
    private AsyncService asyncService;
    @Resource
    private RedisLock redisLock;
    @Resource
    private ConfigService configService;
    @Resource
    private GraphService graphService;
    @Resource
    private MainWalletLogMapper mainWalletLogMapper;
    @Resource
    private WalletGrpc.WalletBlockingStub blockingStub;
}
