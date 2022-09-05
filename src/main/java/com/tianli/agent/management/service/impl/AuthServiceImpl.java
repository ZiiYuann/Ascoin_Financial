package com.tianli.agent.management.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONUtil;
import com.google.gson.JsonNull;
import com.tianli.agent.management.auth.AgentContent;
import com.tianli.agent.management.auth.AgentInfo;
import com.tianli.agent.management.bo.AuthUserBO;
import com.tianli.agent.management.bo.RePwdBO;
import com.tianli.agent.management.service.AuthService;
import com.tianli.agent.management.vo.LoginTokenVO;
import com.tianli.common.Constants;
import com.tianli.common.RedisConstants;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.management.entity.WalletAgent;
import com.tianli.management.service.IWalletAgentService;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Transactional
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private IWalletAgentService walletAgentService;

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public LoginTokenVO login(AuthUserBO loginUserBO) {
        String uuid = loginUserBO.getUuid();
        String code = loginUserBO.getCode();
        String username = loginUserBO.getUsername();
        String password = loginUserBO.getPassword();
        String verifyKey = RedisConstants.CAPTCHA_CODE_KEY + uuid;
        RBucket<String> bucket = redissonClient.getBucket(verifyKey);
        if(StrUtil.isBlank(bucket.get()) || !code.equalsIgnoreCase(bucket.get())){
            ErrorCodeEnum.CODE_ERROR.throwException();
        }
        WalletAgent walletAgent = walletAgentService.getByAgentName(username);
        if(Objects.isNull(walletAgent) || !walletAgent.getLoginPassword().equals(SecureUtil.md5(password))){
            ErrorCodeEnum.PASSWORD_ERROR.throwException();
        }
        bucket.delete();
        String token = IdUtil.simpleUUID();
        String sessionKey = RedisConstants.AGENT_SESSION_KEY+token;
        AgentInfo agentInfo = AgentInfo.builder()
                .agentId(walletAgent.getUid())
                .agentName(walletAgent.getAgentName())
                .build();
        RBucket<String> tokenBucket = redissonClient.getBucket(sessionKey);
        tokenBucket.set(Constants.GSON.toJson(agentInfo),4L,TimeUnit.HOURS);
        return LoginTokenVO.builder()
                .token(token)
                .userId(walletAgent.getUid())
                .username(walletAgent.getAgentName())
                .build();
    }


    @Override
    public void changePassword(RePwdBO rePwdBO) {
        String oldPassword = rePwdBO.getOldPassword();
        String newPassword = rePwdBO.getNewPassword();
        String agentName = AgentContent.get().getAgentName();
        WalletAgent walletAgent = walletAgentService.getByAgentName(agentName);
        if(Objects.isNull(walletAgent) || !walletAgent.getLoginPassword().equals(SecureUtil.md5(oldPassword))){
            ErrorCodeEnum.PASSWORD_ERROR.throwException();
        }
        walletAgent.setLoginPassword(SecureUtil.md5(newPassword));
        walletAgentService.updateById(walletAgent);
    }
}
