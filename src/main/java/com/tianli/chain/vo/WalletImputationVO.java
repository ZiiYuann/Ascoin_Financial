package com.tianli.chain.vo;

import com.tianli.chain.enums.ImputationStatus;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.common.blockchain.NetworkType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-21
 **/
@Data
public class WalletImputationVO {

    private Long id;

    private Long uid;

    private String address;

    private BigDecimal amount;

    private CurrencyCoin coin;

    private NetworkType network;

    private ImputationStatus status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

}
