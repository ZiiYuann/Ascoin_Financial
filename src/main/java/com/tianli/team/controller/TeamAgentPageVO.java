package com.tianli.team.controller;

import com.tianli.currency.TokenCurrencyType;
import com.tianli.team.mapper.TeamAgentPageDTO;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class TeamAgentPageVO {

    /**
     * 用户id
     */
    private Long uid;

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
    private Long create_time_ms;

    /**
     * 自身下单金额
     */
    private double team_amount;

    /**
     * 自身下单金额
     */
    private Long team_number;

//    /**
//     * 是否是超级代理商
//     */
//    private Boolean super_agent;

    /**
     * 是否有下级代理商
     */
    private Boolean hasSubAgent;

    public static TeamAgentPageVO trans(TeamAgentPageDTO dto) {
        LocalDateTime create_time = dto.getCreate_time();
        Instant instant = create_time.atZone(ZoneId.systemDefault()).toInstant();
        return TeamAgentPageVO.builder()
                .uid(dto.getId())
                .avatar(dto.getAvatar())
                .nick(dto.getNick())
                .username(dto.getUsername())
                .create_time(create_time)
                .create_time_ms(instant.toEpochMilli())
                .team_amount(TokenCurrencyType.usdt_omni.money(dto.getTeam_amount()))
                .team_number(dto.getTeam_number())
//                .super_agent(dto.getSuper_agent())
                .hasSubAgent(dto.getHasSubAgent())
                .build();
    }

}
