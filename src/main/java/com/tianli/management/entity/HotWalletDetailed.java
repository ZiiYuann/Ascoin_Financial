package com.tianli.management.entity;

import com.tianli.chain.enums.ChainType;
import com.tianli.management.enums.HotWalletOperationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-08-17
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotWalletDetailed {

    /**
     * 如果id存在则是修改
     */
    private Long id;

    private String uid;

    private BigDecimal amount;

    private String coin;

    private ChainType chain;

    private String fromAddress;

    private String toAddress;

    private String hash;

    private HotWalletOperationType type;

    private String remarks;

    private LocalDateTime createTime;
}
