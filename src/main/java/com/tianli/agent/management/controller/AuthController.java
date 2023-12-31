package com.tianli.agent.management.controller;

import com.tianli.agent.management.auth.AgentContent;
import com.tianli.agent.management.auth.AgentInfo;
import com.tianli.agent.management.auth.AgentPrivilege;
import com.tianli.agent.management.bo.AuthUserBO;
import com.tianli.agent.management.bo.RePwdBO;
import com.tianli.agent.management.service.AuthService;
import com.tianli.agent.management.vo.LoginTokenVO;
import com.tianli.exception.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/agent/management")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * 用户登陆
     */
    @PostMapping("/login")
    public Result<LoginTokenVO> login(@RequestBody @Valid AuthUserBO authUserBO){
        LoginTokenVO tokenVO = authService.login(authUserBO);
        return Result.success(tokenVO);
    }

    /**
     *修改密码
     */
    @PostMapping("/change/password")
    @AgentPrivilege
    public Result<Void> changePassword(@RequestBody @Valid RePwdBO rePwdBO){
        authService.changePassword(rePwdBO);
        return Result.success();
    }

    /**
     * 用户详情
     */
    @GetMapping("/userInfo")
    @AgentPrivilege
    public Result<AgentInfo> userInfo(){
        AgentInfo agentInfo = AgentContent.get();
        return Result.success(agentInfo);
    }

}
