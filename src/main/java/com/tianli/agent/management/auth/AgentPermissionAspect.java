package com.tianli.agent.management.auth;

import com.tianli.common.Constants;
import com.tianli.common.RedisConstants;
import com.tianli.exception.ErrorCodeEnum;
import org.aspectj.lang.annotation.*;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

@Aspect
@Component
public class AgentPermissionAspect {

    @Resource
    private HttpServletRequest httpServletRequest;

    @Autowired
    private RedissonClient redissonClient;


    @Before(value = "@annotation(privilege)")
    public void verifyActiveAdmin(AgentPrivilege privilege){
        String REQUEST_HEAD = "token";
        String token = httpServletRequest.getHeader(REQUEST_HEAD);
        if(Objects.isNull(token)){
            ErrorCodeEnum.UNLOIGN.throwException();
        }
        String sessionKey = RedisConstants.AGENT_SESSION_KEY + token;
        RBucket<String> bucket = redissonClient.getBucket(sessionKey);
        if(!bucket.isExists()){
            ErrorCodeEnum.UNLOIGN.throwException();
        }
        AgentInfo agentInfo = Constants.GSON.fromJson(bucket.get(), AgentInfo.class);
        AgentContent.set(agentInfo);

    }

    @AfterThrowing(value = "@annotation(privilege)")
    public void afterThrowing(AgentPrivilege privilege){
        AgentContent.remove();
    }

    @After(value = "@annotation(privilege)")
    public void removeActiveAdmin(AgentPrivilege privilege){
        AgentContent.remove();
    }
}
