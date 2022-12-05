package com.tianli.chain.vo;

import com.tianli.chain.enums.ChainType;
import com.tianli.common.blockchain.NetworkType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    private ChainType chain;

    private NetworkType network;

    private boolean mainToken;

    private int decimals;

    private int withdrawDecimals;

}
