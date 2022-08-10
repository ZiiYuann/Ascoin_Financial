package com.tianli.chain.entity;

import com.tianli.chain.enums.ImputationStatus;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.common.blockchain.NetworkType;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-21
 **/
@Data
@Builder
public class WalletImputationLog {

    /**
     * id
     */
    @Id
    private Long id;

    /**
     * txid
     */
    private String txid;

    /**
     * 归集金额
     */
    private BigDecimal amount;

    /**
     * to_address
     */
    private String fromAddress;

    /**
     * 网络
     */
    private NetworkType network;

    /**
     * 币别
     */
    private CurrencyCoin coin;

    /**
     * 归集状态
     */
    private ImputationStatus status;

    /**
     * create_time
     */
    private LocalDateTime createTime;

    private LocalDateTime finishTime;

}
