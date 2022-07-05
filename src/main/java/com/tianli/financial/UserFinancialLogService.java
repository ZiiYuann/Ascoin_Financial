package com.tianli.financial;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.financial.controller.UserFinancialPage;
import com.tianli.financial.mapper.UserFinancialLog;
import com.tianli.financial.mapper.UserFinancialLogMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class UserFinancialLogService extends ServiceImpl<UserFinancialLogMapper, UserFinancialLog> {

    public List<UserFinancialPage> getUserFinancialPage(Long uid) {
        return userFinancialLogMapper.getUserFinancialPage(uid);
    }

    @Resource
    private UserFinancialLogMapper userFinancialLogMapper;

}
