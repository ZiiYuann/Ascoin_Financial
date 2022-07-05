package com.tianli.management.agentmanage;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tianli.agent.AgentService;
import com.tianli.agent.mapper.Agent;
import com.tianli.common.async.AsyncService;
import com.tianli.management.ruleconfig.ConfigConstants;
import com.tianli.mconfig.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author chensong
 * @date 2021-02-19 17:06
 * @since 1.0.0
 */
@Slf4j
@Component
public class SuperAgentTask {
    @Resource
    private AgentService agentService;
    @Resource
    private ConfigService configService;
    @Resource
    private AsyncService asyncService;

    @Scheduled(cron = "0 * *  * * ? ")
    public void updateAgentFocus() {
        asyncService.async(() -> {
            Integer super_agent_referral = Integer.parseInt(configService.get(ConfigConstants.SUPER_AGENT_REFERRAL));
            Integer super_agent_subordinate = Integer.parseInt(configService.get(ConfigConstants.SUPER_AGENT_SUBORDINATE));
            Integer super_agent_subordinate_referral = Integer.parseInt(configService.get(ConfigConstants.SUPER_AGENT_SUBORDINATE_REFERRAL));
            agentService.update(new LambdaUpdateWrapper<Agent>().set(Agent::getSuper_agent, false).eq(Agent::getSuper_agent, true));
            agentService.updateSuperAgent(super_agent_referral, super_agent_subordinate, super_agent_subordinate_referral);
        });
    }
}
