package com.tianli.agent.management.auth;


import java.util.Objects;

public class AgentContent {
    private static final ThreadLocal<AgentInfo> THREAD_LOCAL = new ThreadLocal<>();

    private AgentContent() {

    }

    /**
     * 获取管理员信息
     */
    public static AgentInfo get() {
        AgentInfo user = THREAD_LOCAL.get();
        if (Objects.isNull(user)) {
            user = new AgentInfo();
            THREAD_LOCAL.set(user);
        }
        return user;
    }

    public static Long getAgentId() {
        return get().getAgentId();
    }

    public static void set(AgentInfo agentInfo) {
        THREAD_LOCAL.set(agentInfo);
    }

    /**
     * 移除管理员信息
     */
    public static void remove() {
        THREAD_LOCAL.remove();
    }
}
