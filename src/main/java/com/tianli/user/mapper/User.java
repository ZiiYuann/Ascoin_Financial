package com.tianli.user.mapper;

import lombok.Data;
import org.springframework.data.annotation.Transient;

import java.time.LocalDateTime;

/**
 * @Author wangqiyun
 * @Date 2019-11-06 17:39
 */
@Data
public class User {
    /**
     * 主键
     */
    private Long id;
    /**
     * 创建时间
     */
    private LocalDateTime create_time;
    /**
     * 账号
     */
    private String username;
    /**
     * 身份
     */
    private UserIdentity identity;
    /**
     * 状态
     */
    private UserStatus status;
    /**
     * 推荐码
     */
    private String referral_code;
    /**
     * 是否开启BF代币优惠支付
     */
    private Boolean BF;
    /**
     * 上次访问时间
     */
    private LocalDateTime last_time;
    /**
     * 上次访问ip
     */
    private String last_ip;

    /**
     * 用户的唯一标识
     */
    @Transient
    private String hash_key;

    /**
     * 是否使用过机器人
     */
    private Boolean use_robot;

    /**
     * 备注
     */
    private String node;

    /**
     * 用户类型
     */
    private Integer user_type;

    /**
     * 冻结（0:正常，1:冻结）
     */
    private Integer freeze;

    /**
     * 信用评分
     */
    private Integer credit_score;
}
