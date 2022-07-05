package com.tianli.admin;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.admin.mapper.Admin;
import com.tianli.admin.mapper.AdminMapper;
import com.tianli.admin.mapper.AdminStatus;
import com.tianli.admin.role.AdminRoleService;
import com.tianli.admin.role.mapper.AdminRole;
import com.tianli.captcha.email.service.CaptchaEmailService;
import com.tianli.captcha.phone.mapper.CaptchaPhoneType;
import com.tianli.captcha.phone.service.CaptchaPhoneService;
import com.tianli.common.CommonFunction;
import com.tianli.common.lock.RedisLock;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.management.admin.AdminPageVO;
import com.tianli.management.admin.AdminUpdatePwdDTO;
import com.tianli.management.admin.CreateAdminDTO;
import com.tianli.management.admin.UpdateAdminDTO;
import com.tianli.role.RoleService;
import com.tianli.role.mapper.Role;
import com.tianli.role.permission.RolePermissionService;
import com.tianli.role.permission.mapper.RolePermission;
import com.tianli.tool.MapTool;
import com.tianli.user.UserService;
import com.tianli.user.controller.UserRegDTO;
import com.tianli.user.mapper.User;
import com.tianli.user.mapper.UserIdentity;
import com.tianli.user.token.UserTokenService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 管理员 服务实现类
 * </p>
 *
 * @author hd
 * @since 2020-12-15
 */
@Service
public class AdminService extends ServiceImpl<AdminMapper, Admin> {

    public Admin getByUsername(String username) {
        if (StringUtils.isBlank(username)) {
            return null;
        }
        return super.getOne(
                new LambdaQueryWrapper<Admin>()
                        .eq(Admin::getUsername, username));
    }

    public AdminAndRoles my() {
        Long id = accountService.getLogin();
        if (id == null) throw ErrorCodeEnum.UNLOIGN.generalException();
        Admin admin = super.getById(id);
        if (admin == null) {
            throw ErrorCodeEnum.UNLOIGN.generalException();
        }
        if (Objects.equals(AdminStatus.disable, admin.getStatus())) {
            throw ErrorCodeEnum.ACCOUNT_BAND.generalException();
        }
        AdminAndRoles adminAndRoles = AdminAndRoles.builder()
                .id(id)
                .username(admin.getUsername())
                .status(admin.getStatus())
                .phone(admin.getPhone()).build();
        Role role = roleService.getByAid(admin.getId());
        if (Objects.isNull(role)) {
            return adminAndRoles;
        }
        adminAndRoles.setRole(role);
        List<RolePermission> rolePermissions = rolePermissionService.list(
                new LambdaQueryWrapper<RolePermission>()
                        .eq(RolePermission::getRole_id, role.getId()));
        adminAndRoles.setRolePermissionList(rolePermissions);
        return adminAndRoles;
    }

    @Transactional
    public void deleteAdmin(Long id) {
        //admin表中删除记录
        int delete = adminMapper.delete(new LambdaUpdateWrapper<Admin>().eq(Admin::getId, id));
        //在admin_role中查询rid
        AdminRole adminRole = adminRoleService.getOne(new LambdaQueryWrapper<AdminRole>().eq(AdminRole::getUid, id));
        //在admin_role表中删除数据
        boolean deleteAdminRole = adminRoleService.delete(id);
        //role表中对应admin_number-1
        boolean numSub = roleService.adminNumberMinusOne(adminRole.getRole_id());
        if (!((delete == 1) && deleteAdminRole && numSub)) {
            ErrorCodeEnum.SYSTEM_BUSY.throwException();
        }
    }

    public List<AdminPageVO> pageAdmin(String role_name, String username, Integer size, Integer page) {
        return adminMapper.pageAdmin(role_name, username, size, size * (page - 1));
    }

    public int countAdmin(String role_name, String username) {
        return adminMapper.countAdmin(role_name, username);
    }

    public List<String> getPrivilege(Long id) {
        return adminMapper.getPrivilege(id);
    }

    @Transactional
    public Long createAdmin(CreateAdminDTO admin) {
        //查询用户名是否重复
        Admin one = adminMapper.selectOne(new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, admin.getUsername()));
        if (Objects.nonNull(one)) {
            ErrorCodeEnum.throwException("用户名重复，请重新输入");
        }
        //查询角色是否存在
        Role role = roleService.getOne(new LambdaQueryWrapper<Role>().eq(Role::getName, admin.getRole_name()));
        if (Objects.isNull(role)) {
            ErrorCodeEnum.throwException("该角色不存在");
        }
        //admin表插入数据
        long id = CommonFunction.generalId();
        int saveAdmin = adminMapper.insert(Admin.builder()
                .id(id)
                .create_time(LocalDateTime.now())
                .phone(admin.getPhone())
                .note(admin.getNote())
                .username(admin.getUsername())
                .nickname(admin.getNickname())
                .password(passwordEncoder.encode(admin.getPassword()))
                .operation_password(passwordEncoder.encode(admin.getOperation_password()))
                .build());
        //admin_role表插入数据
        boolean saveAdminRole = adminRoleService.save(AdminRole.builder().uid(id).role_id(role.getId()).build());
        //修改role表中的admin_number字段
        boolean updateAdminNumber = roleService.adminNumberPlusOne(role.getId());

        if (!(saveAdmin == 1) && saveAdminRole && updateAdminNumber) {
            ErrorCodeEnum.SYSTEM_BUSY.throwException();
        }
        return id;
    }


    @Transactional
    public void updateAdmin(UpdateAdminDTO admin) {
        //当前管理员是否存在
        Admin adminById = adminMapper.selectById(admin.getId());
        if (Objects.isNull(adminById)) {
            ErrorCodeEnum.USER_NOT_EXIST.throwException();
        }
        //查询是否有其他的重复用户名
        Admin one = adminMapper.selectOne(new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, admin.getUsername()));
        if (Objects.nonNull(one) && !Objects.equals(one.getId(), admin.getId())) {
            ErrorCodeEnum.throwException("用户名重复，请重新输入");
        }
        //查询新的role是否存在
        Role newRole = roleService.getOne(new LambdaQueryWrapper<Role>().eq(Role::getName, admin.getRole_name()));
        if (Objects.isNull(newRole)) {
            ErrorCodeEnum.throwException("该角色不存在");
        }
        //修改admin表中数据
        int updateAdmin = adminMapper.updateById(Admin.builder()
                .id(admin.getId())
                .username(admin.getUsername())
                .phone(admin.getPhone())
                .note(admin.getNote())
                .nickname(admin.getNickname()).build());
        if (updateAdmin != 1) {
            ErrorCodeEnum.SYSTEM_BUSY.throwException();
        }
        //查询旧的roleId
        AdminRole adminRole = adminRoleService.getOne(new LambdaQueryWrapper<AdminRole>().eq(AdminRole::getUid, admin.getId()));
        if (!Objects.equals(adminRole.getRole_id(), newRole.getId())) {
            //修改管理员对应的角色
            boolean updateRole = adminRoleService.update(
                    new LambdaUpdateWrapper<AdminRole>()
                            .eq(AdminRole::getUid, admin.getId())
                            .set(AdminRole::getRole_id, newRole.getId()));
            //旧角色admin_number-1
            boolean numSub = roleService.adminNumberMinusOne(adminRole.getRole_id());
            //新角色admin_number+1
            boolean numPlus = roleService.adminNumberPlusOne(newRole.getId());
            if (!(updateRole && numPlus && numSub)) {
                ErrorCodeEnum.SYSTEM_BUSY.throwException();
            }
        }
    }

    @Transactional
    public Map<String, Object> reg(UserIdentity identity, UserRegDTO userRegDTO) {
        redisLock.lock("UserController.reg_" + userRegDTO.getUsername(), 1L, TimeUnit.MINUTES);
//        captchaPhoneService.verify(userRegDTO.getUsername(), CaptchaPhoneType.registrationAgent, userRegDTO.getCode());
//        captchaEmailService.verify(userRegDTO.getUsername(), CaptchaPhoneType.registrationAgent, userRegDTO.getCode());
        User user = userService._getByUsername(userRegDTO.getUsername());
        if (Objects.isNull(user)) {
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }
        if (!Objects.equals(user.getIdentity(), identity)) {
            ErrorCodeEnum.INSUFFICIENT_USER_RIGHTS.throwException();
        }
        String token = userTokenService.login(user);
        return MapTool.Map()
                .put("id", user.getId())
                .put("token", token);
    }

    public void updatePwd(Long id, AdminUpdatePwdDTO updatePwd) {
        Admin admin = this.getById(id);
        if (Objects.isNull(admin)) {
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }
        String password = passwordEncoder.encode(updatePwd.getPassword());
        Admin.AdminBuilder adminBuilder = Admin.builder().id(id);
        if (ObjectUtil.equal(updatePwd.getType(), 1)) {
            adminBuilder.operation_password(password);
        } else {
            adminBuilder.password(password);
        }
        this.updateById(adminBuilder.build());
    }

    @Resource
    private AdminMapper adminMapper;

    @Resource
    private RoleService roleService;

    @Resource
    private RolePermissionService rolePermissionService;

    @Resource
    private AccountService accountService;

    @Resource
    private AdminRoleService adminRoleService;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private UserTokenService userTokenService;

    @Resource
    private UserService userService;

    @Resource
    private CaptchaPhoneService captchaPhoneService;

    @Resource
    private CaptchaEmailService captchaEmailService;

    @Resource
    private RedisLock redisLock;

}
