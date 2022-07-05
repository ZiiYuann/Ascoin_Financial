package com.tianli.bet.controller;

import com.tianli.agent.mapper.Agent;
import lombok.Data;

import java.util.LinkedList;

/**
 * @author XWZ
 * @Date 2021/6/21 9:28 上午
 */
@Data
public class BetAgentVO {
    private Long userId;
    private LinkedList<Agent> agentList;
}
