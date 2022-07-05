package com.tianli.management.currency;

import lombok.Data;

import java.math.BigInteger;

/**
 * @author chensong
 * @date 2021-02-25 14:53
 * @since 1.0.0
 */
@Data
public class ArtificialRechargePageDTO {
    /**
     * 充值id
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nick;

    /**
     * 充值数额
     */
    private BigInteger amount;

    /**
     * 充值管理员
     */
    private String admin_nick;

    /**
     * 操作时间
     */
    private String create_time;

    /**
     * 记录类型（充值，撤回）
     */
    private String des;
}
