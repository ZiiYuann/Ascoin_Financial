package com.tianli.chain.vo;

import com.tianli.common.blockchain.NetworkType;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-25
 **/
@Data
public class WalletImputationLogAppendixVO {

    private Long id;

    private String txid;

    private NetworkType network;

    private BigDecimal amount;

    private String toAddress;

    private String fromAddress;

}
