package com.tianli.management.vo;

import com.tianli.chain.enums.ChainType;
import com.tianli.common.blockchain.NetworkType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2022-08-18
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotWalletBalanceVO {

    private String CoinName;

    private ChainType chain;

    private NetworkType network;

    private BigDecimal amount;

    private List<HotWalletBalanceVO> tokens;

}
