package com.tianli.management.role;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.tianli.admin.role.AdminRoleService;
import com.tianli.admin.role.mapper.AdminRole;
import com.tianli.common.init.admin.AdminContent;
import com.tianli.common.init.admin.AdminInfo;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exception.Result;
import com.tianli.role.RoleService;
import com.tianli.role.annotation.AdminPrivilege;
import com.tianli.role.annotation.Privilege;
import com.tianli.role.mapper.Role;
import com.tianli.tool.MapTool;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 管理员角色表 前端控制器
 * </p>
 *
 * @author hd
 * @since 2020-12-15
 */
@RestController
@RequestMapping("/role")
public class RoleController {

    @PostMapping("create")
    @AdminPrivilege(and = Privilege.角色管理)
    public Result createRole(@RequestBody @Valid RoleDTO roleDTO){
        Role role = roleService.getByName(roleDTO.getName());
        if(Objects.nonNull(role)){
            ErrorCodeEnum.throwException("角色名重复，请重新输入");
        }
        roleService.insertRole(roleDTO);
        return Result.success();
    }

    @PostMapping("update")
    @AdminPrivilege(and = Privilege.角色管理)
    public Result updateRole(@RequestBody @Valid RoleDTO roleDTO){
        if(Objects.isNull(roleDTO.getId())){
            ErrorCodeEnum.SYSTEM_BUSY.throwException();
        }
        Role role = roleService.getByName(roleDTO.getName());
        if(Objects.nonNull(role) && !Objects.equals(role.getId(),roleDTO.getId())){
            ErrorCodeEnum.throwException("角色名重复，请重新输入");
        }
        roleService.UpdateRole(roleDTO);
        return Result.success();
    }

    @PostMapping("status/{rid}/{status}")
    @AdminPrivilege(and = Privilege.角色管理)
    public Result updateStatusRole(@PathVariable("rid") Long id, @PathVariable("status") RoleStatus status){
        if(Objects.isNull(status)){
            ErrorCodeEnum.SYSTEM_BUSY.throwException();
        }
        roleService.update(new LambdaUpdateWrapper<Role>().set(Role::getStatus, status).eq(Role::getId, id));
        return Result.success();
    }

    @DeleteMapping("delete/{rid}")
    @AdminPrivilege(and = Privilege.角色管理)
    public Result deleteRole(@PathVariable("rid") Long id){
        Role role = roleService.getOne(new LambdaQueryWrapper<Role>().eq(Role::getId, id));
        if(role.getAdmin_number()>0){
            ErrorCodeEnum.throwException("删除失败,该角色下存在管理员");
        }
        roleService.deleteRole(id);
        return Result.success();
    }

    @GetMapping("list")
    @AdminPrivilege(and = Privilege.角色管理)
    public Result roleList(){
        List<RoleVO> roleVOList = roleService.selectAll();
        return Result.success(roleVOList);
    }

    @GetMapping("privilege/list")
    public Result privilegeList(){
        List<PrivilegeListVO> privilege = roleService.getPrivilegeList();
        return Result.success(privilege);
    }

    @GetMapping("/get/id")
    @AdminPrivilege(and = Privilege.角色管理)
    public Result adminForId(){
        AdminInfo adminInfo = AdminContent.get();
        if (Objects.isNull(adminInfo)) ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        Long adminId = adminInfo.getAid();
        AdminRole adminRole = adminRoleService.getOne(new LambdaQueryWrapper<AdminRole>().eq(AdminRole::getUid, adminId));
        if (Objects.isNull(adminRole)) ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        return Result.instance().setData(MapTool.Map().put("roleId", adminRole.getRole_id()));
    }

    @Resource
    private RoleService roleService;

    @Resource
    private AdminRoleService adminRoleService;
}

