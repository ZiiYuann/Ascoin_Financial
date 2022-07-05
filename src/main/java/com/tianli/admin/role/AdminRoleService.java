package com.tianli.admin.role;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.admin.role.mapper.AdminRole;
import com.tianli.admin.role.mapper.AdminRoleMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 * 管理员角色表 服务实现类
 * </p>
 *
 * @author hd
 * @since 2020-12-15
 */
@Service
public class AdminRoleService extends ServiceImpl<AdminRoleMapper, AdminRole> {

    @Resource
    AdminRoleMapper adminRoleMapper;

    public boolean delete(Long id) {
        int delete = adminRoleMapper.delete(new LambdaUpdateWrapper<AdminRole>().eq(AdminRole::getUid, id));
        return delete == 1;
    }
}
