package com.tianli.sso.permission;

import com.tianli.sso.permission.admin.AdminContent;
import com.tianli.sso.permission.admin.AdminInfo;
import com.tianli.sso.service.AdminOssService;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.annotation.After;
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
        String contextPath = privilege.api();
        if (StringUtils.isBlank(contextPath)) {
            contextPath = request.getServletPath();
        }
        AdminInfo adminInfo = AdminContent.get();
        adminInfo.setApi(contextPath);
        adminOssService.loginStatus();
    }

    @After(value = "@annotation(privilege)")
    public void removeActiveAdmin(AdminPrivilege privilege){
        AdminContent.remove();
    }
}
