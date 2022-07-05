package com.tianli.team.mapper;

import lombok.Data;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
public class TeamReferralPageDTO {

    /**
     * id
     */
    private Long id;

    /**
     * 头像地址
     */
    private String avatar;

    /**
     * 昵称
     */
    private String nick;

    /**
     * 账号(缓存)
     */
    private String username;

    /**
     * 创建时间
     */
    private LocalDateTime create_time;

    /**
     * 自身下单金额
     */
    private BigInteger my_amount;

    /**
     * 自身下单金额
     */
    private Integer team_number;

    /**
     * 自身下单金额
     */
    private boolean hasSubAgent;

}
