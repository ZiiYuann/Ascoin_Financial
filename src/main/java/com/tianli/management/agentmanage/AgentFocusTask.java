package com.tianli.management.agentmanage;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tianli.agent.AgentService;
import com.tianli.agent.mapper.Agent;
import com.tianli.currency.CurrencyService;
import com.tianli.exception.ErrorCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author chensong
 * @date 2021-01-13 16:46
 * @since 1.0.0
 */
@Slf4j
@Component
public class AgentFocusTask {

    @Resource
    private CurrencyService currencyService;
    @Resource
    private AgentService agentService;

//    @Scheduled(cron = "0/30 * *  * * ? ")
    public void updateAgentFocus(){
        List<Long> list = currencyService.listAgentFocus();
        if(!agentService.update(new LambdaUpdateWrapper<Agent>().set(Agent::getFocus,false))){
            ErrorCodeEnum.SYSTEM_ERROR.throwException();
        }
        if(CollectionUtils.isEmpty(list)) return;
        boolean update = agentService.update(new LambdaUpdateWrapper<Agent>()
                .set(Agent::getFocus, true)
                .in(Agent::getId, list));
        if(!update){
            ErrorCodeEnum.SYSTEM_ERROR.throwException();
        }
    }
}
