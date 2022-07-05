package com.tianli.rake.controller;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 *
 * 抽水表
 *
 * @author linyifan
 * @date 2/19/21 3:04 PM
 */

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class RakePageVO {

    /**
     * 创建时间
     */
    private LocalDateTime create_time;

    /**
     * 下注金额
     */
    private double amount;

    /**
     * 下注用户名
     */
    private String uid_username;

    /**
     * 下注用户昵称
     */
    private String uid_nick;

    /**
     * 下注用户头像
     */
    private String uid_avatar;

    /**
     * 抽水金额
     */
    private double rake_amount;

}
