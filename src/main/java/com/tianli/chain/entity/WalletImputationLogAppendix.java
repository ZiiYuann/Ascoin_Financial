package com.tianli.chain.entity;

import com.tianli.common.blockchain.NetworkType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 归集日志附录表
 * @author chenb
 * @apiNote
 * @since 2022-07-25
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletImputationLogAppendix {

    private Long id;

    private String txid;

    private NetworkType network;

    private BigDecimal amount;

    private String toAddress;

    private String fromAddress;

}
