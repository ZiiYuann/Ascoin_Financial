package com.tianli.management.admin;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tianli.admin.AccountService;
import com.tianli.admin.AdminService;
import com.tianli.admin.adminiplog.AdminIpLogService;
import com.tianli.admin.adminiplog.mapper.AdminIpLog;
import com.tianli.admin.mapper.Admin;
import com.tianli.admin.mapper.AdminStatus;
import com.tianli.admin.totp.AdminTotpService;
import com.tianli.captcha.CaptchaService;
import com.tianli.common.CommonFunction;
import com.tianli.common.Constants;
import com.tianli.common.IpTool;
import com.tianli.common.init.admin.AdminContent;
import com.tianli.common.init.admin.AdminInfo;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.management.adminipwihtelist.AdminIpWhiteListService;
import com.tianli.role.RoleService;
import com.tianli.role.annotation.AdminPrivilege;
import com.tianli.role.annotation.Privilege;
import com.tianli.tool.MapTool;
import com.tianli.user.controller.UserRegDTO;
import com.tianli.user.mapper.UserIdentity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * <p>
 * 管理员 前端控制器
 * </p>
 *
 * @author hd
 * @since 2020-12-15
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Resource
    private IpTool ipTool;

    @Resource
    private AdminIpLogService adminIpLogService;

    @PostMapping("/login")
    public Result login(@RequestBody @Valid LoginDTO loginDTO) {
        if (!captchaService.verify(loginDTO.getCode())) {
            throw ErrorCodeEnum.CODE_ERROR.generalException();
        }
        String ip = ipTool.getIp();
//        if(!adminIpWhiteListService.ipAllowed(ip)) {
//            throw ErrorCodeEnum.ACCESS_DENY.generalException();
//        }
        Admin admin = adminService.getByUsername(loginDTO.getUsername());
        if (admin == null) {
            throw ErrorCodeEnum.PASSWORD_ERROR.generalException();
        }
        if(!adminTotpService.loginVerify(loginDTO.getUsername(), loginDTO.getGoogle_code())){
            throw ErrorCodeEnum.CODE_ERROR.generalException();
        }
       /* Role role = roleService.getByAid(admin.getId());
        if(!adminIpWhiteListService.ipAllowed(ip, role)) {
            throw ErrorCodeEnum.ACCESS_DENY.generalException();
        }*/
        if (AdminStatus.disable.equals(admin.getStatus())) {
            throw ErrorCodeEnum.ACCOUNT_BAND.generalException();
        }
        if (!passwordEncoder.matches(loginDTO.getPassword(), admin.getPassword())) {
            throw ErrorCodeEnum.PASSWORD_ERROR.generalException();
        }
        accountService.login(admin.getId());
        Long admin_id = admin.getId();
        CompletableFuture.runAsync(() -> {
            // 异步记录ip信息 登录的log日志
            adminIpLogService.save(AdminIpLog.builder().id(CommonFunction.generalId()).admin_id(admin_id).create_time(LocalDateTime.now()).last_ip(ip).build());
            adminService.update(new LambdaUpdateWrapper<Admin>().eq(Admin::getId, admin_id).set(Admin::getLast_ip, ip));
        }, Constants.COMPLETABLE_FUTURE_EXECUTOR);
        List<String> privilegeList = adminService.getPrivilege(admin.getId());
        return Result.instance().setData(privilegeList);
    }



    @PostMapping("/agent/login/{identity}")
    public Result reg(@PathVariable("identity") UserIdentity identity, @RequestBody @Valid UserRegDTO userRegDTO) {
        return Result.success(adminService.reg(identity,userRegDTO));
    }

    @PostMapping("/update/pwd")
    @AdminPrivilege
    public Result updatePwd(@RequestBody @Valid UpdatePwdDTO loginDTO) {
        AdminInfo adminInfo = AdminContent.get();
        Long aid = adminInfo.getAid();
        Admin admin = adminService.getById(aid);
        if(Objects.isNull(admin)){
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }
        Admin update;
        if (ObjectUtil.equal(loginDTO.getType(),1)) {
            if(!passwordEncoder.matches(loginDTO.getOldPassword(), admin.getOperation_password())){
                ErrorCodeEnum.ORIGINAL_PASSWORD_ERROR.throwException();
            }
            update = Admin.builder().id(aid).operation_password(passwordEncoder.encode(loginDTO.getPassword())).build();
        } else {
            if(!passwordEncoder.matches(loginDTO.getOldPassword(), admin.getPassword())){
                ErrorCodeEnum.ORIGINAL_PASSWORD_ERROR.throwException();
            }
            update = Admin.builder().id(aid).password(passwordEncoder.encode(loginDTO.getPassword())).build();
        }
        adminService.updateById(update);
        return Result.instance();
    }


    @PostMapping("/update/pwd/{id}")
    @AdminPrivilege(and = Privilege.管理员管理)
    public Result updatePwd(@PathVariable("id") Long id, @RequestBody @Valid AdminUpdatePwdDTO updatePwd){
        adminService.updatePwd(id,updatePwd);
        return Result.instance();
    }

    @PostMapping("create")
    @AdminPrivilege(and = Privilege.管理员管理)
    public Result createAdmin(@RequestBody @Valid CreateAdminDTO admin){
        adminService.createAdmin(admin);
        return Result.success();
    }

    @PostMapping("update")
    @AdminPrivilege(and = Privilege.管理员管理)
    public Result updateAdmin(@RequestBody @Valid UpdateAdminDTO admin){
        adminService.updateAdmin(admin);
        return Result.success();
    }

    @PostMapping("status/{id}/{status}")
    @AdminPrivilege(and = Privilege.管理员管理)
    public Result disableAdmin(@PathVariable("id") Long id, @PathVariable("status") AdminStatus status){
        if(Objects.isNull(status)){
            ErrorCodeEnum.SYSTEM_BUSY.throwException();
        }
        adminService.update(new LambdaUpdateWrapper<Admin>().eq(Admin::getId, id).set(Admin::getStatus, status));
        return Result.success();
    }

    @DeleteMapping("delete/{id}")
    @AdminPrivilege(and = Privilege.管理员管理)
    public Result deleteAdmin(@PathVariable("id") Long id){
        adminService.deleteAdmin(id);
        return Result.success();
    }

    @GetMapping("page")
    @AdminPrivilege(and = Privilege.管理员管理)
    public Result adminPage(String role_name, String username,
                            @RequestParam(value = "page", defaultValue = "1") Integer page,
                            @RequestParam(value = "size", defaultValue = "10") Integer size){

        List<AdminPageVO> vo = adminService.pageAdmin(role_name, username, size, page);
        int count = adminService.countAdmin(role_name,username);
        int totalPage = (count - 1)/size + 1;
        return Result.instance().setData(MapTool.Map().put("page",vo).put("totalNum",count).put("totalPage",totalPage));
    }

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private AccountService accountService;

    @Resource
    private CaptchaService captchaService;

    @Resource
    private AdminService adminService;

    @Resource
    private AdminIpWhiteListService adminIpWhiteListService;

    @Resource
    private RoleService roleService;

    @Resource
    private AdminTotpService adminTotpService;

}

