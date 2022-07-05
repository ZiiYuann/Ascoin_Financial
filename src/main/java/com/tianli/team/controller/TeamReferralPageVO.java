package com.tianli.team.controller;

import com.tianli.currency.TokenCurrencyType;
import com.tianli.team.mapper.TeamReferralPageDTO;
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
public class TeamReferralPageVO {

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

    private Long create_time_ms;

    /**
     * 自身下单金额
     */
    private double my_amount;

    /**
     * 团队人数
     */
    private Integer team_number;

    /**
     * 团队是否有下级
     */
    private boolean hasSubAgent;

    public static TeamReferralPageVO trans(TeamReferralPageDTO dto) {
        LocalDateTime create_time = dto.getCreate_time();
        Instant instant = create_time.atZone(ZoneId.systemDefault()).toInstant();
        return TeamReferralPageVO.builder()
                .id(dto.getId())
                .avatar(dto.getAvatar())
                .nick(dto.getNick())
                .username(dto.getUsername())
                .create_time(create_time)
                .create_time_ms(instant.toEpochMilli())
                .my_amount(TokenCurrencyType.usdt_omni.money(dto.getMy_amount()))
                .team_number(dto.getTeam_number())
                .hasSubAgent(dto.isHasSubAgent())
                .build();
    }

}
