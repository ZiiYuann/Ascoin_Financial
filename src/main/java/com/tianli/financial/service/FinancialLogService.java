package com.tianli.financial.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.financial.controller.UserFinancialPage;
import com.tianli.financial.entity.FinancialLog;
import com.tianli.financial.mapper.FinancialLogMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class FinancialLogService extends ServiceImpl<FinancialLogMapper, FinancialLog> {

    public List<UserFinancialPage> getUserFinancialPage(Long uid) {
        return userFinancialLogMapper.getUserFinancialPage(uid);
    }

    @Resource
    private FinancialLogMapper userFinancialLogMapper;

}
