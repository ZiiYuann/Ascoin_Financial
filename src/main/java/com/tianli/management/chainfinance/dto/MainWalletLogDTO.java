package com.tianli.management.chainfinance.dto;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.currency_token.mapper.ChainType;
import com.tianli.currency_token.transfer.mapper.TokenContract;
import com.tianli.wallet.mapper.MainWalletLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MainWalletLogDTO {

    private Long id;

    private LocalDateTime create_time;

    private String currency_type;

    private ChainType chain_type;

    private BigInteger amount;

    private String from_address;

    private String to_address;

    private String direction;

    private String txid;

    private String block;

    private double money;

    public static MainWalletLogDTO trans(MainWalletLog walletLog, List<TokenContract> tokenContractList) {
        MainWalletLogDTOBuilder builder = MainWalletLogDTO.builder()
                .amount(walletLog.getAmount())
                .id(walletLog.getId())
                .money(TokenCurrencyType.usdt_omni.money(walletLog.getAmount()))
                .block(walletLog.getBlock())
                .chain_type(walletLog.getChain_type())
                .create_time(walletLog.getCreate_time())
                .from_address(walletLog.getFrom_address())
                .to_address(walletLog.getTo_address())
                .direction(walletLog.getDirection())
                .currency_type(walletLog.getCurrency_type())
                .txid(walletLog.getTxid());
        for (TokenContract tokenContract : tokenContractList) {
            if (StrUtil.equals(tokenContract.getToken().name(), walletLog.getCurrency_type()) &&
                    ObjectUtil.equal(tokenContract.getChain(), walletLog.getChain_type())) {
                builder.money(Convert.toDouble(tokenContract.money(walletLog.getAmount())));
            }
        }
        return builder.build();
    }
}
