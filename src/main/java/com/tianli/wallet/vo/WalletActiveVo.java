package com.tianli.wallet.vo;

import com.tianli.wallet.enums.AccountActiveStatus;

import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-06
 **/
public class WalletActiveVo {
    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    private LocalDateTime updateTime;

    /**
     * 钱包状态 {@link  AccountActiveStatus}
     */
    private AccountActiveStatus status;
}
