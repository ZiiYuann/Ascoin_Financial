package com.tianli.management.query;

import com.tianli.chain.enums.ChainType;
import com.tianli.common.blockchain.NetworkType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * @author chenb
 * @apiNote
 * @since 2022-11-29
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CoinsQuery {

    private String name;

    private String contract;

    private ChainType chain;

    private NetworkType network;

    private Byte status;

}
