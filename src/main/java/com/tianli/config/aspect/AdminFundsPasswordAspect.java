package com.tianli.config.aspect;

import com.tianli.admin.AdminService;
import com.tianli.admin.mapper.Admin;
import com.tianli.common.init.admin.AdminContent;
import com.tianli.common.init.admin.AdminInfo;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.role.annotation.FundsPasswordPrivilege;
import com.tianli.role.mapper.Role;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * <p>
 * 管理员操作资金时密码校验
 * </P>
 *
 * @author linyifan
 * @since 4/22/21 10:58 AM
 */

@Aspect
@Component
@Slf4j
public class AdminFundsPasswordAspect {

    @Resource
    HttpServletRequest httpServletRequest;


    @Resource
    AdminService adminService;

    @Resource
    private PasswordEncoder passwordEncoder;


    /**
     * FundsPasswordPrivilege
     *
     * @param fundsPasswordPrivilege 注解
     */
    @Before(value = "@annotation(fundsPasswordPrivilege)")
    public void judgePrivilege(FundsPasswordPrivilege fundsPasswordPrivilege) {
        AdminInfo adminInfo = AdminContent.get();
        if (Objects.isNull(adminInfo)) ErrorCodeEnum.ACCESS_DENY.throwException();
        Admin admin = adminService.getById(adminInfo.getAid());
        String password = httpServletRequest.getParameter("funds_password");
        if (!passwordEncoder.matches(password, admin.getOperation_password())) {
            ErrorCodeEnum.OPERATION_PASSWORD_ERROR.throwException();
        }
    }

}
