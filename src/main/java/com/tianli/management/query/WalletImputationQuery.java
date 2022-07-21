package com.tianli.management.query;

import com.tianli.chain.enums.ImputationStatus;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.common.blockchain.NetworkType;
import lombok.Data;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-21
 **/
@Data
public class WalletImputationQuery {

    private CurrencyCoin coin;

    private Long uid;

    private NetworkType network;

    private ImputationStatus status;
}
