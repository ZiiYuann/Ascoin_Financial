package com.tianli.chain.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.chain.entity.ChainCallbackLog;
import com.tianli.chain.enums.ChainType;
import com.tianli.chain.mapper.ChainCallbackLogMapper;
import com.tianli.charge.enums.ChargeType;
import com.tianli.common.CommonFunction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-08-02
 **/
@Slf4j
@Service
public class ChainCallbackLogService extends ServiceImpl<ChainCallbackLogMapper, ChainCallbackLog> {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ChainCallbackLog insert(ChargeType type, ChainType chain,String log){
        ChainCallbackLog chainCallbackLog = new ChainCallbackLog();
        chainCallbackLog.setId(CommonFunction.generalId());
        chainCallbackLog.setType(type);
        chainCallbackLog.setChain(chain);
        chainCallbackLog.setLog(log);
        chainCallbackLog.setStatus("create");
        chainCallbackLog.setCreateTime(LocalDateTime.now());
        baseMapper.insert(chainCallbackLog);
        return chainCallbackLog;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean updateById(ChainCallbackLog entity) {
        return super.updateById(entity);
    }
}
