package com.tianli.wallet;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tianli.common.CommonFunction;
import com.tianli.currency_token.mapper.ChainType;
import com.tianli.currency_token.mapper.CurrencyCoinEnum;
import com.tianli.currency_token.transfer.mapper.TokenContract;
import com.tianli.currency_token.transfer.service.TokenContractService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.management.recycle.record.RecycleRecord;
import com.tianli.management.recycle.record.RecycleRecordService;
import com.tianli.management.ruleconfig.ConfigConstants;
import com.tianli.mconfig.ConfigService;
import com.tianli.tool.TXUtil;
import com.tianli.wallet.enums.TXChainTypeEnum;
import com.tianli.wallet.mapper.MainWalletLog;
import com.tianli.wallet.mapper.MainWalletLogMapper;
import com.tianli.wallet.vo.TXBlockQueryVo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.tianli.management.ruleconfig.ConfigConstants.TX_INSIDER;

/**
 * @author lzy
 * @date 2022/4/26 16:18
 */
@Service
public class WalletTransferServiceV2 {


    @Resource
    private ConfigService configService;

    @Resource
    TokenContractService tokenContractService;

    @Resource
    private MainWalletLogMapper mainWalletLogMapper;

    @Resource
    RecycleRecordService recycleRecordService;

    @Transactional(rollbackFor = Exception.class)
    public void mainBscWalletScan() {
        String myBscAddress = configService.get(ConfigConstants.BSC_MAIN_WALLET_ADDRESS);
        String block = configService.get(ConfigConstants.BSC_MAIN_WALLET_TRANSFER_BLOCK);
        mainWalletScan(myBscAddress, block, ChainType.bep20, TXChainTypeEnum.BSC, CurrencyCoinEnum.bnb, ConfigConstants.BSC_MAIN_WALLET_TRANSFER_BLOCK);
    }

    @Transactional(rollbackFor = Exception.class)
    public void mainEthWalletScan() {
        String myBscAddress = configService.get(ConfigConstants.ETH_MAIN_WALLET_ADDRESS);
        String block = configService.get(ConfigConstants.ETH_MAIN_WALLET_TRANSFER_BLOCK);
        mainWalletScan(myBscAddress, block, ChainType.erc20, TXChainTypeEnum.ETH, CurrencyCoinEnum.eth, ConfigConstants.ETH_MAIN_WALLET_TRANSFER_BLOCK);
    }

    @Transactional(rollbackFor = Exception.class)
    public void mainTrcWalletScan() {
        String myAddress = configService.get(ConfigConstants.TRON_MAIN_WALLET_ADDRESS);
        String block = configService._get(ConfigConstants.TRON_MAIN_WALLET_TRANSFER_BLOCK);
        mainWalletScan(myAddress, block, ChainType.trc20, TXChainTypeEnum.TRON, CurrencyCoinEnum.trx, ConfigConstants.TRON_MAIN_WALLET_TRANSFER_BLOCK);
    }

    public static void main(String[] args) {
        Long lastBlock = TXUtil.getBlockNumber("https://nft-data-center.assure.pro/api/tx/blockNumber", TXChainTypeEnum.TRON);
        System.out.println(lastBlock);
    }

    @Transactional(rollbackFor = Exception.class)
    public void mainWalletCollection() {
        List<RecycleRecord> recycleRecords = recycleRecordService.queryNotProcess();
        if (CollUtil.isEmpty(recycleRecords)) {
            return;
        }
        String txInsiderUrl = configService.get(TX_INSIDER);
        for (RecycleRecord recycleRecord : recycleRecords) {
            String txHash = recycleRecord.getTx_hash();
            ChainType chainType = recycleRecord.getChain_type();
            switch (chainType) {
                case trc20:
                    Object tronData = TXUtil.insiderTrading(txInsiderUrl, TXChainTypeEnum.TRON, txHash);
                    this.tronCollection(tronData, recycleRecord);
                    break;
                case bep20:
                    Object bepData = TXUtil.insiderTrading(txInsiderUrl, TXChainTypeEnum.BSC, txHash);
                    this.bscOrEthCollection(bepData, ChainType.bep20, CurrencyCoinEnum.bnb, recycleRecord);
                    break;
                case erc20:
                    Object ethData = TXUtil.insiderTrading(txInsiderUrl, TXChainTypeEnum.ETH, txHash);
                    this.bscOrEthCollection(ethData, ChainType.erc20, CurrencyCoinEnum.eth, recycleRecord);
                    break;
            }
        }
    }

    private void bscOrEthCollection(Object data, ChainType chainType, CurrencyCoinEnum currencyCoinEnum, RecycleRecord recycleRecord) {
        if (ObjectUtil.isNull(data)) {
            return;
        }
        JSONArray jsonArray = JSONUtil.parseArray(data);
        if (CollUtil.isNotEmpty(jsonArray)) {
            for (Object token : jsonArray) {
                JSONObject jsonToken = JSONUtil.parseObj(token);
                BigInteger value = jsonToken.getBigInteger("value");
                if (StrUtil.isBlank(jsonToken.getStr("contractAddress")) && BigInteger.ZERO.compareTo(value) < 0) {
                    MainWalletLog mainWalletLog = MainWalletLog.builder()
                            .amount(value)
                            .block(jsonToken.getStr("blockNumber"))
                            .currency_type(currencyCoinEnum.name())
                            .chain_type(chainType)
                            .create_time(LocalDateTime.now())
                            .direction("in")
                            .id(CommonFunction.generalId())
                            .from_address(jsonToken.getStr("from"))
                            .to_address(jsonToken.getStr("to"))
                            .txid(recycleRecord.getTx_hash())
                            .build();
                    mainWalletLogMapper.insert(mainWalletLog);
                }
            }
        }
        recycleRecord.setProcess(Boolean.TRUE);
        recycleRecordService.updateById(recycleRecord);
    }

    private void tronCollection(Object tronData, RecycleRecord recycleRecord) {
        if (ObjectUtil.isNull(tronData)) {
            return;
        }
        JSONArray jsonArray = JSONUtil.parseArray(tronData);
        for (Object o : jsonArray) {
            for (Object tr : JSONUtil.parseArray(o)) {
                JSONObject trJsonObject = JSONUtil.parseObj(tr);
                JSONArray tokenList = trJsonObject.getJSONArray("token_list");
                if (CollUtil.isNotEmpty(tokenList)) {
                    for (Object token : tokenList) {
                        BigInteger callValue = JSONUtil.parseObj(token).getBigInteger("call_value");
                        if (ObjectUtil.isNotNull(callValue) && BigInteger.ZERO.compareTo(callValue) < 0) {
                            MainWalletLog mainWalletLog = MainWalletLog.builder()
                                    .amount(callValue)
                                    .block(trJsonObject.getStr("block"))
                                    .currency_type(CurrencyCoinEnum.trx.name())
                                    .chain_type(ChainType.trc20)
                                    .create_time(Convert.toLocalDateTime(trJsonObject.getLong("date_created")))
                                    .direction("in")
                                    .id(CommonFunction.generalId())
                                    .from_address(trJsonObject.getStr("contract"))
                                    .to_address(trJsonObject.getStr("transfer_to_address"))
                                    .txid(trJsonObject.getStr("hash"))
                                    .build();
                            mainWalletLogMapper.insert(mainWalletLog);
                        }
                    }
                }
            }
        }
        recycleRecord.setProcess(Boolean.TRUE);
        recycleRecordService.updateById(recycleRecord);
    }

    /**
     * @param myAddress        主钱包地址
     * @param block            系统最后一次查询的区块数
     * @param chainType        主链类型 用于查询该主链下的合约地址
     * @param txChainTypeEnum  主链类型 用于请求接口参数
     * @param currencyCoinEnum 该主链的主币类型
     * @param blockNumKey      主链所对应数据库存储的区块key
     */
    private void mainWalletScan(String myAddress, String block,
                                ChainType chainType, TXChainTypeEnum txChainTypeEnum,
                                CurrencyCoinEnum currencyCoinEnum, String blockNumKey) {
        long start = Long.parseLong(block);
        String txUrlBlockNumber = configService.get(ConfigConstants.TX_URL_BLOCK_NUMBER);
        Long lastBlock = TXUtil.getBlockNumber(txUrlBlockNumber, txChainTypeEnum);
        if (start + 1 > lastBlock) {
            return;
        }
        String txUrlList = configService.get(ConfigConstants.TX_URL_LIST);
        List<TokenContract> tokenContractList = tokenContractService.findByChainType(chainType);
        List<String> contractAddress = tokenContractList.stream().map(TokenContract::getContract_address).collect(Collectors.toList());
        List<TXBlockQueryVo> txBlockQueryVos = TXUtil.blockQuery(txUrlList, txChainTypeEnum, contractAddress, start + 1);
        //更新区块数
        boolean cas = configService.cas(blockNumKey, block, String.valueOf(Long.parseLong(block) + 1));
        if (!cas) ErrorCodeEnum.SYSTEM_ERROR.throwException();
        if (CollUtil.isEmpty(txBlockQueryVos)) {
            return;
        }
        Map<String, TokenContract> tokenContractMap = tokenContractList.stream()
                .map(tokenContract -> tokenContract.setContract_address(tokenContract.getContract_address().toLowerCase()))
                .collect(Collectors.toMap(TokenContract::getContract_address, Function.identity(), (v1, v2) -> v1));
        for (TXBlockQueryVo txBlockQueryVo : txBlockQueryVos) {
            if (StrUtil.equalsIgnoreCase(myAddress, txBlockQueryVo.getFrom())) {
                insertMainWalletLog(currencyCoinEnum, tokenContractMap, txBlockQueryVo, "out", chainType);
            }
            if (StrUtil.equalsIgnoreCase(myAddress, txBlockQueryVo.getTo())) {
                insertMainWalletLog(currencyCoinEnum, tokenContractMap, txBlockQueryVo, "in", chainType);
            }
        }
    }


    private void insertMainWalletLog(CurrencyCoinEnum mainToken, Map<String, TokenContract> tokenContractMap,
                                     TXBlockQueryVo txBlockQueryVo, String direction, ChainType chainType) {
        String currency_type;
        if (StrUtil.isBlank(txBlockQueryVo.getContractAddress())) {
            currency_type = mainToken.name();
        } else {
            TokenContract tokenContract = tokenContractMap.get(txBlockQueryVo.getContractAddress().toLowerCase());
            if (ObjectUtil.isNotNull(tokenContract)) {
                currency_type = tokenContract.getToken().name();
            } else {
                return;
            }
        }
        MainWalletLog mainWalletLog = MainWalletLog.builder()
                .amount(txBlockQueryVo.getValue())
                .block(txBlockQueryVo.getBlock().toString())
                .currency_type(currency_type)
                .chain_type(chainType)
                .create_time(LocalDateTime.now())
                .direction(direction)
                .id(CommonFunction.generalId())
                .from_address(txBlockQueryVo.getFrom())
                .to_address(txBlockQueryVo.getTo())
                .txid(txBlockQueryVo.getHash())
                .build();
        mainWalletLogMapper.insert(mainWalletLog);
    }
}
