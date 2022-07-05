package com.tianli.agent.team.mapper;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * <p>
 * 代理商团队表(缓存表)
 * </p>
 *
 * @author hd
 * @since 2020-12-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class AgentTeam {

    /**
     * 邀请人id
     */
    private Long referral_id;

    /**
     * 用户id
     */
    private Long uid;

    /**
     * 邀请时间
     */
    private LocalDateTime referral_time;


}
