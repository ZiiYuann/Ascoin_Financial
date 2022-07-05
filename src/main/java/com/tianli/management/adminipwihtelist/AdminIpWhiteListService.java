package com.tianli.management.adminipwihtelist;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.management.adminipwihtelist.mapper.AdminIpWhiteList;
import com.tianli.management.adminipwihtelist.mapper.AdminIpWhiteListMapper;
import com.tianli.role.mapper.Role;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminIpWhiteListService extends ServiceImpl<AdminIpWhiteListMapper, AdminIpWhiteList> {

    public boolean ipAllowed(String ip, Role role) {
        if(!"超级管理员".equals(role.getName()) && !"财务".equals(role.getName())) return true;
        List<AdminIpWhiteList> l = this.list(new LambdaQueryWrapper<AdminIpWhiteList>()
                .eq(AdminIpWhiteList::getIs_deleted, 0).eq(AdminIpWhiteList::getIp, ip));
        return l.size() > 0;
    }
}
