package com.tianli.team.mapper;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigInteger;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class TeamAgentPageDTO {

    /**
     * 用户id
     */
    private Long id;

    /**
     * 创建时间
     */
    private LocalDateTime create_time;

    /**
     * 昵称
     */
    private String nick;

    /**
     * 账号(缓存)
     */
    private String username;

    /**
     * 头像地址
     */
    private String avatar;


    /**
     * 自身下单金额
     */
    private BigInteger team_amount;

    /**
     * 自身下单金额
     */
    private Long team_number;

    /**
     * 是否是超级代理商
     */
    private Boolean super_agent;

    /**
     * 是否有下级代理商
     */
    private Boolean hasSubAgent;

}
