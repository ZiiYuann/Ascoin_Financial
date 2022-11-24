package com.tianli.chain.vo;

import com.tianli.chain.enums.ImputationStatus;
import com.tianli.common.blockchain.NetworkType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-25
 **/
@Data
public class WalletImputationLogVO {

    /**
     * id
     */
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
     * from_address
     */
    private String fromAddress;

    /**
     * 网络
     */
    private NetworkType network;

    /**
     * 币别
     */
    private String coin;

    /**
     * 归集状态
     */
    private ImputationStatus status;

    /**
     * create_time
     */
    private LocalDateTime createTime;

}
