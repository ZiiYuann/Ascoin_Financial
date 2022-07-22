package com.tianli.chain.entity;

import com.tianli.chain.enums.ImputationStatus;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.common.blockchain.NetworkType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-22
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WalletImputationTemporary {

    private Long id;

    private Long uid;

    private String address;

    private BigDecimal amount;

    private CurrencyCoin coin;

    private NetworkType network;

    private LocalDateTime createTime;

}
