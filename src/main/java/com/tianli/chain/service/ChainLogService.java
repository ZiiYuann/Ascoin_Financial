package com.tianli.chain.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.chain.mapper.ChainLog;
import com.tianli.chain.mapper.ChainLogMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ChainLogService extends ServiceImpl<ChainLogMapper, ChainLog> {

    @Resource
    ChainLogMapper chainLogMapper;

    @Transactional
    public void replaceBatch(List<ChainLog> chainLogList) {
        for (ChainLog chainLog : chainLogList) {
            chainLogMapper.replace(chainLog);
        }
    }
}
