package com.tianli.management.agentadmin.vo;

import com.tianli.agent.mapper.Agent;
import com.tianli.user.statistics.mapper.UserStatistics;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author chensong
 * @date 2021-02-19 16:51
 * @since 1.0.0
 */
@Data
@Builder
public class LowAgentPageVO {

    private static final long serialVersionUID = 1L;

    /**
     * 用户id
     */
    private Long id;

    /**
     * 创建时间
     */
    private LocalDateTime create_time;

    /**
     * 代理商名称
     */
    private String nick;

    /**
     * 代理商手机号
     */
    private String username;

    /**
     * 普通场返佣比例
     */
    private Double normal_rebate_proportion;

    /**
     * 稳赚场返佣比例
     */
//    private Double steady_rebate_proportion;

    /**
     * 团队人数
     */
    private Long team_number;

    /**
     * 备注
     */
    private String note;

    /**
     * 是否是超级代理商
     */
    private Boolean super_agent;

    public static LowAgentPageVO trans(Agent agent,UserStatistics statistics){
        Long teamNumber = agent.getSuper_agent() ? statistics.getTeam_number():statistics.getReferral_number();
        return LowAgentPageVO.builder()
                .id(agent.getId())
                .nick(agent.getNick())
                .username(agent.getUsername())
                .create_time(agent.getCreate_time())
                .normal_rebate_proportion(agent.getNormal_rebate_proportion()*100.0)
//                .steady_rebate_proportion(agent.getSteady_rebate_proportion()*100.0)
                .super_agent(agent.getSuper_agent())
                .team_number(teamNumber)
                .note(agent.getNote())
                .build();
    }
}
