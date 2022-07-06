package com.tianli.sso.permission;

import com.tianli.sso.service.AdminOssService;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author chensong
 * @date 2021-01-28 09:58
 * @since 1.0.0
 */
@Aspect
@Component
public class PermissionAspect {

    @Resource
    private HttpServletRequest request;
    @Resource
    private AdminOssService adminOssService;

    @Before(value = "@annotation(privilege)")
    public void verifyActiveAdmin(AdminPrivilege privilege){
        String url = request.getRequestURL().toString();
        if(url.contains("127.0.0.1")||url.contains("localhost")){
            return;
        }
        adminOssService.loginStatus();
    }
}
