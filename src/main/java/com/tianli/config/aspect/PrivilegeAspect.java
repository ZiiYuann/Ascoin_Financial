package com.tianli.config.aspect;

import com.google.common.collect.Sets;
import com.tianli.sso.AdminAndRoles;
import com.tianli.sso.AdminService;
import com.tianli.common.init.admin.AdminContent;
import com.tianli.common.init.admin.AdminInfo;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.role.annotation.AdminPrivilege;
import com.tianli.role.annotation.Privilege;
import com.tianli.role.mapper.Role;
import com.tianli.role.permission.mapper.RolePermission;
import com.tianli.sso.permission.AdminPrivilege;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Aspect
@Component
@Slf4j
@Order(0)
public class PrivilegeAspect {

    @Resource
    AdminService adminService;

    @Resource
    HttpServletRequest httpServletRequest;

    /**
     * 拦截注解privilege
     * @param privilege 注解
     */
    @Before(value = "@annotation(privilege)")
    public void judgePrivilege(AdminPrivilege privilege) {
        // 获取当前域名
        String domainString = httpServletRequest.getRequestURL().toString().replaceAll(httpServletRequest.getRequestURI(), "");
        if(log.isDebugEnabled()){
            log.info("请求域名: {}", domainString);
        }
        String local = "127.0.0.1";
        String local_ = "localhost";
        if(domainString.contains(local_) || domainString.contains(local)){
            return;
        }
        AdminAndRoles my = adminService.my();
        Role role = my.getRole();
        if(Objects.nonNull(role) && Objects.equals(role.getName(), "admin")){
            List<RolePermission> rolePermissionList = my.getRolePermissionList();
            Long id = role.getId();
            RolePermission rolePermission = RolePermission.builder().permission("充值提现").role_id(id).build();
            rolePermissionList.add(rolePermission);
        }
        if (!this.judgePrivilege(my, privilege.or(), privilege.and())) {
            throw ErrorCodeEnum.ACCESS_DENY.generalException();
        }
        AdminInfo adminInfo = AdminContent.get();
        adminInfo.setAid(my.getId());
        adminInfo.setRole(role);
        adminInfo.setUsername(my.getUsername());
        adminInfo.setPhone(my.getPhone());
    }

    @After(value = "@annotation(privilege)")
    public void remove(AdminPrivilege privilege) {
        AdminContent.remove();
    }

    /**
     * 判断当前登录用户权限
     */
    private boolean judgePrivilege(AdminAndRoles adminAndRoles, Privilege[] or, Privilege[] and) {
        Set<Privilege> set;
        List<RolePermission> rolePermissions;
        if (Objects.nonNull(adminAndRoles.getRole()) && Objects.nonNull(rolePermissions = adminAndRoles.getRolePermissionList())) {
            set = rolePermissions.stream()
                    .map(RolePermission::getPermission)
                    .filter(e -> privilegeString.contains(e))
                    .map(Privilege::valueOf)
                    .collect(Collectors.toSet());
        }else {
            set = Sets.newHashSet();
        }

        for (Privilege p : or) {
            if (set.contains(p)) {
                return true;
            }
        }

        for (Privilege p : and) {
            if (!set.contains(p)) {
                return false;
            }
        }
        return true;
    }

    public PrivilegeAspect() {
        privilegeString.addAll(Stream.of(Privilege.values())
                .map(e -> e.name())
                .collect(Collectors.toSet()));
    }

    private Set<String> privilegeString = Sets.newHashSet();

}

