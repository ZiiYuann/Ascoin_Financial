package com.tianli.agent;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.agent.mapper.Agent;
import com.tianli.agent.mapper.AgentMapper;
import com.tianli.currency.CurrencyTokenEnum;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.management.agentmanage.controller.AgentRakeDetailDTO;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 代理商表 服务实现类
 * </p>
 *
 * @author hd
 * @since 2020-12-04
 */
@Service
public class AgentService extends ServiceImpl<AgentMapper, Agent> {

    /**
     * 获取代理商链
     *
     * @param uid 低级代理商uid
     * @return 他上级的全部代理商list
     */
    public LinkedList<Agent> agentChain(long uid) {
        LinkedList<Agent> agentList = new LinkedList<>();
        while (true){
            Agent byId = super.getById(uid);
            if(Objects.isNull(byId)){
                break;
            }
            agentList.addFirst(byId);
            Long senior_id = byId.getSenior_id();
            if(Objects.isNull(senior_id) || Objects.equals(senior_id, 0L)){
                break;
            }
            uid = senior_id;
        }
        return agentList;
    }

    public LinkedList<Agent> superiorAgentChain(long uid) {
        LinkedList<Agent> agents = agentChain(uid);
        if (CollectionUtils.isEmpty(agents)) {
            return agents;
        }
        agents.removeLast();
        return agents;
    }

    /**
     * 更新用户实际分红占比
     * @param uid 用户id = 代理商id
     * @param remain 缴纳金额
     */
    public void updateRealDividends(long uid, BigInteger remain) {
        Agent agent = super.getById(uid);
        if(Objects.isNull(agent)){
           return;
       }
        // 计算实际的分红比例
        double remainMoney = TokenCurrencyType.usdt_omni.money(remain);
        double expect_deposit = TokenCurrencyType.usdt_omni.money(agent.getExpect_deposit());
        double expect_dividends = agent.getExpect_dividends();
        double real_dividends = Math.min((remainMoney / expect_deposit) * expect_dividends, expect_dividends);
        super.update(new LambdaUpdateWrapper<Agent>().set(Agent::getReal_dividends, real_dividends).eq(Agent::getId, uid));
    }

    public void increaseProfit(long uid, BigInteger profit) {
        increaseProfit(uid, CurrencyTokenEnum.usdt_omni, profit);
    }

    public void increaseProfit(long uid, CurrencyTokenEnum token, BigInteger profit) {
        baseMapper.increaseProfit(uid, token, profit);
    }

    public void changeSettledNumber(long uid, BigInteger settledNumber) {
        baseMapper.increaseSettledNumber(uid, settledNumber);
    }

    public long updateSuperAgent(Integer super_agent_referral, Integer super_agent_subordinate, Integer super_agent_subordinate_referral) {
        return baseMapper.updateSuperAgent(super_agent_referral,super_agent_subordinate,super_agent_subordinate_referral);
    }

    public List<AgentRakeDetailDTO> rakeDetail(String phone, String startTime, String endTime, Integer page, Integer size) {
        return baseMapper.rakeDetail(phone, startTime, endTime, Math.max((page-1)*size,0), size);
    }

    public long rakeDetailCount(String phone, String startTime, String endTime) {
        return baseMapper.rakeDetailCount(phone, startTime, endTime);
    }
}
