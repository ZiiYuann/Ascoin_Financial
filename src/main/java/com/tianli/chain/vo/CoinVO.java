package com.tianli.chain.vo;

import com.tianli.chain.enums.ChainType;
import com.tianli.common.blockchain.NetworkType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2022-12-05
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoinVO {
    private Long id;

    private String name;

    private String contract;

    /**
     * app和后台交互的
     */
    private ChainType chain;

    /**
     * app给用户展示的
     */
    private String chainName;

    private NetworkType network;

    private boolean mainToken;

    private int decimals;

    private int withdrawDecimals;

    private BigDecimal withdrawMin;

    private BigDecimal withdrawFixedAmount;

    private String logo;

}
