package com.tianli.role;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.common.CommonFunction;
import com.tianli.management.role.PrivilegeListVO;
import com.tianli.management.role.RoleDTO;
import com.tianli.management.role.RoleVO;
import com.tianli.role.mapper.Role;
import com.tianli.role.mapper.RoleMapper;
import com.tianli.role.permission.mapper.RolePermission;
import com.tianli.role.permission.mapper.RolePermissionMapper;
import com.tianli.role.privilege.mapper.RolePrivilegeListMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 管理员角色表 服务实现类
 * </p>
 *
 * @author hd
 * @since 2020-12-15
 */
@Service
public class RoleService extends ServiceImpl<RoleMapper, Role> {

    public Role getByAid(Long aid) {
        return baseMapper.selectByAid(aid);
    }

    public Role getByName(String name) {
        if(StringUtils.isBlank(name)){
            return null;
        }
        return super.getOne(new LambdaQueryWrapper<Role>().eq(Role::getName,name));
    }

    @Transactional
    public void insertRole(RoleDTO roleDTO) {
        long roleId = CommonFunction.generalId();
        Role role = Role.builder().id(roleId).create_time(LocalDateTime.now()).name(roleDTO.getName()).note(roleDTO.getNote()).build();
        roleMapper.insert(role);

        for (String permission : roleDTO.getPermission()) {
            rolePermissionMapper.insert(RolePermission.builder()
                    .role_id(roleId).permission(permission).build());
        }

    }

    @Transactional
    public void UpdateRole(RoleDTO roleDTO) {
        Role role = Role.builder()
                .id(roleDTO.getId())
                .name(roleDTO.getName())
                .note(roleDTO.getNote())
                .build();
        roleMapper.updateById(role);
        //删除原来的权限
        rolePermissionMapper.delete(new LambdaUpdateWrapper<RolePermission>()
                .eq(RolePermission::getRole_id, roleDTO.getId()));
        //添加新的权限
        for (String permission : roleDTO.getPermission()) {
            rolePermissionMapper.insert(RolePermission.builder()
                    .role_id(roleDTO.getId()).permission(permission).build());
        }
    }


    @Transactional
    public void deleteRole(Long id) {
        roleMapper.delete(new LambdaUpdateWrapper<Role>().eq(Role::getId, id));
        rolePermissionMapper.delete(new LambdaUpdateWrapper<RolePermission>().eq(RolePermission::getRole_id, id));
    }

    public List<RoleVO> selectAll() {
        List<Role> roles = roleMapper.selectList(new LambdaQueryWrapper<Role>().orderByDesc(Role::getCreate_time));
        List<RoleVO> roleVOList = new ArrayList<>();
        for (Role role : roles) {
            List<String> permissionList = rolePermissionMapper.selectPermissionList(role.getId());
            roleVOList.add(RoleVO.builder()
                    .id(role.getId())
                    .name(role.getName())
                    .adminNumber(role.getAdmin_number())
                    .note(role.getNote())
                    .status(role.getStatus())
                    .createTime(role.getCreate_time())
                    .permission(permissionList).build());
        }
        return roleVOList;
    }

    public List<PrivilegeListVO> getPrivilegeList() {
        List<PrivilegeListVO> privilegeList = rolePrivilegeListMapper.selectParent();
        for (PrivilegeListVO vo : privilegeList) {
            vo.setList(rolePrivilegeListMapper.selectSecond(vo.getId()));
        }
        return privilegeList;
    }

    public boolean adminNumberPlusOne(Long id) {
        Integer row = roleMapper.adminNumberPlusOne(id);
        return row == 1;
    }

    public boolean adminNumberMinusOne(Long id) {
        Integer row = roleMapper.adminNumberMinusOne(id);
        return row == 1;
    }

    @Resource
    RoleMapper roleMapper;
    @Resource
    RolePermissionMapper rolePermissionMapper;
    @Resource
    RolePrivilegeListMapper rolePrivilegeListMapper;


}
