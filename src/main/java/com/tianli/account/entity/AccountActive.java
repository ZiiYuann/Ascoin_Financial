package com.tianli.account.entity;

import com.tianli.wallet.enums.AccountActiveStatus;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote 账户激活
 * @since 2022-07-06
 **/
@Data
@Builder
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class AccountActive {

    Long id;

    /**
     * 用户id
     */
    Long uid;

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
    private byte status;

}
