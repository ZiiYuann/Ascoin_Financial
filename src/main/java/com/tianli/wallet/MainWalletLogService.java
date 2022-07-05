package com.tianli.wallet;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.currency_token.mapper.ChainType;
import com.tianli.currency_token.mapper.CurrencyCoinEnum;
import com.tianli.currency_token.transfer.mapper.TokenContract;
import com.tianli.currency_token.transfer.service.TokenContractService;
import com.tianli.tool.MapTool;
import com.tianli.wallet.mapper.MainWalletLog;
import com.tianli.wallet.mapper.MainWalletLogMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class MainWalletLogService extends ServiceImpl<MainWalletLogMapper, MainWalletLog> {

    @Resource
    MainWalletLogMapper mainWalletLogMapper;

    @Resource
    TokenContractService tokenContractService;

    public Map<String, Object> sumAmount(String address, String txid, ChainType chain_type, String start, String end) {
        String chainType = null;
        if (ObjectUtil.isNotNull(chain_type)) {
            chainType = chain_type.name();
        }
        Map<String, Object> sumAmount = mainWalletLogMapper.sumAmount(address, txid, chainType, start, end);
        List<TokenContract> usdtToken = tokenContractService.findByToken(CurrencyCoinEnum.usdt);
        Map<ChainType, TokenContract> usdtTokenMap = usdtToken.stream().collect(Collectors.toMap(TokenContract::getChain, Function.identity()));
        List<TokenContract> usdcToken = tokenContractService.findByToken(CurrencyCoinEnum.usdc);
        Map<ChainType, TokenContract> usdcTokenMap = usdcToken.stream().collect(Collectors.toMap(TokenContract::getChain, Function.identity()));
        BigDecimal usdtBscInSumAmount = usdtTokenMap.get(ChainType.bep20).money(Convert.toBigInteger(sumAmount.get("usdtBscInSumAmount")));
        BigDecimal usdtTrcInSumAmount = usdtTokenMap.get(ChainType.trc20).money(Convert.toBigInteger(sumAmount.get("usdtTrcInSumAmount")));
        BigDecimal usdtErcInSumAmount = usdtTokenMap.get(ChainType.erc20).money(Convert.toBigInteger(sumAmount.get("usdtErcInSumAmount")));
        BigDecimal usdtBscOutSumAmount = usdtTokenMap.get(ChainType.bep20).money(Convert.toBigInteger(sumAmount.get("usdtBscOutSumAmount")));
        BigDecimal usdtTrcOutSumAmount = usdtTokenMap.get(ChainType.trc20).money(Convert.toBigInteger(sumAmount.get("usdtTrcOutSumAmount")));
        BigDecimal usdtErcOutSumAmount = usdtTokenMap.get(ChainType.erc20).money(Convert.toBigInteger(sumAmount.get("usdtErcOutSumAmount")));
        BigDecimal usdcBscInSumAmount = usdcTokenMap.get(ChainType.bep20).money(Convert.toBigInteger(sumAmount.get("usdcBscInSumAmount")));
        BigDecimal usdcTrcInSumAmount = usdcTokenMap.get(ChainType.trc20).money(Convert.toBigInteger(sumAmount.get("usdcTrcInSumAmount")));
        BigDecimal usdcErcInSumAmount = usdcTokenMap.get(ChainType.erc20).money(Convert.toBigInteger(sumAmount.get("usdcErcInSumAmount")));
        BigDecimal usdcBscOutSumAmount = usdcTokenMap.get(ChainType.bep20).money(Convert.toBigInteger(sumAmount.get("usdcBscOutSumAmount")));
        BigDecimal usdcTrcOutSumAmount = usdcTokenMap.get(ChainType.trc20).money(Convert.toBigInteger(sumAmount.get("usdcTrcOutSumAmount")));
        BigDecimal usdcErcOutSumAmount = usdcTokenMap.get(ChainType.erc20).money(Convert.toBigInteger(sumAmount.get("usdcErcOutSumAmount")));

        BigDecimal usdt_in = usdtBscInSumAmount.add(usdtTrcInSumAmount).add(usdtErcInSumAmount);
        BigDecimal usdt_out = usdtBscOutSumAmount.add(usdtTrcOutSumAmount).add(usdtErcOutSumAmount);
        BigDecimal usdc_in = usdcBscInSumAmount.add(usdcTrcInSumAmount).add(usdcErcInSumAmount);
        BigDecimal usdc_out = usdcBscOutSumAmount.add(usdcTrcOutSumAmount).add(usdcErcOutSumAmount);
        BigDecimal usdt = usdt_in.subtract(usdt_out);
        BigDecimal usdc = usdc_in.subtract(usdc_out);
        return MapTool.Map()
                .put("usdt_in", usdt_in)
                .put("usdt_out", usdt_out)
                .put("usdc_in", usdc_in)
                .put("usdc_out", usdc_out)
                .put("usdt", usdt)
                .put("usdc", usdc);
    }
}
