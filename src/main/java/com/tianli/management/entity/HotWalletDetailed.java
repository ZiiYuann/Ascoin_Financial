package com.tianli.management.entity;

import com.tianli.chain.enums.ChainType;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.management.enums.HotWalletOperationType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-08-17
 **/
@Data
public class HotWalletDetailed {

    /**
     * 如果id存在则是修改
     */
    private Long id;

    private String uid;

    private BigDecimal amount;

    private CurrencyCoin coin;

    private ChainType chain;

    private String fromAddress;

    private String toAddress;

    private String hash;

    private HotWalletOperationType type;

    private String remarks;

    private LocalDateTime createTime;
}
