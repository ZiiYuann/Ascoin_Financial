package com.tianli.management.salesman.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.management.salesman.dao.SalesmanUserMapper;
import com.tianli.management.salesman.entity.SalesmanUser;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author lzy
 * @date 2022/4/6 4:12 下午
 */
@Service
public class SalesmanUserService extends ServiceImpl<SalesmanUserMapper, SalesmanUser> {

    public void removeByUserIds(List<Long> ids) {
        this.remove(Wrappers.lambdaQuery(SalesmanUser.class).in(SalesmanUser::getUser_id, ids));
    }
}
