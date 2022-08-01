package com.tianli.chain.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.chain.converter.ChainConverter;
import com.tianli.chain.entity.WalletImputationLog;
import com.tianli.chain.enums.ImputationStatus;
import com.tianli.chain.mapper.WalletImputationLogMapper;
import com.tianli.chain.vo.WalletImputationLogVO;
import com.tianli.management.query.WalletImputationLogQuery;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-25
 **/
@Service
public class WalletImputationLogService extends ServiceImpl<WalletImputationLogMapper, WalletImputationLog> {

    @Resource
    private WalletImputationLogMapper walletImputationLogMapper;
    @Resource
    private ChainConverter chainConverter;

    /**
     * 归集日志列表数据
     */
    public IPage<WalletImputationLogVO> walletImputationLogVOPage(IPage<WalletImputationLog> page, WalletImputationLogQuery query){

        LambdaQueryWrapper<WalletImputationLog> queryWrapper = new LambdaQueryWrapper<>();

        if(Objects.nonNull(query.getTxid())){
            queryWrapper = queryWrapper.like(WalletImputationLog :: getTxid,query.getTxid());
        }
        if(Objects.nonNull(query.getCoin())){
            queryWrapper = queryWrapper.eq(WalletImputationLog :: getCoin,query.getCoin());
        }
        if(Objects.nonNull(query.getNetwork())){
            queryWrapper = queryWrapper.eq(WalletImputationLog :: getNetwork,query.getNetwork());
        }
        if(Objects.nonNull(query.getStartTime()) && Objects.nonNull(query.getEndTime())) {
            queryWrapper = queryWrapper.between(WalletImputationLog :: getCreateTime,query.getStartTime(),query.getEndTime());
        }
        if(Objects.nonNull(query.getStatus())) {
            queryWrapper = queryWrapper.eq(WalletImputationLog :: getStatus,query.getStatus());
        }

        return walletImputationLogMapper.selectPage(page, queryWrapper).convert(chainConverter::toWalletImputationLogVO);
    }
}
