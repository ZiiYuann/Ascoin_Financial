package com.tianli.chain.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.chain.converter.ChainConverter;
import com.tianli.chain.entity.WalletImputation;
import com.tianli.chain.mapper.ChainLogMapper;
import com.tianli.chain.mapper.WalletImputationMapper;
import com.tianli.management.query.WalletImputationQuery;
import com.tianli.chain.vo.WalletImputationVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

@Service
public class WalletImputationService extends ServiceImpl<WalletImputationMapper, WalletImputation> {

    @Resource
    private ChainLogMapper chainLogMapper;
    @Resource
    private ChainConverter chainConverter;

    public void insert(WalletImputation walletImputation){
        chainLogMapper.insert(walletImputation);
    }

    /**
     * 归集列表数据
     */
    public IPage<WalletImputationVO> walletImputationVOPage(IPage<WalletImputation> page, WalletImputationQuery query){

        LambdaQueryWrapper<WalletImputation> queryWrapper = new LambdaQueryWrapper<>();

        if(Objects.nonNull(query.getUid())){
            queryWrapper = queryWrapper.eq(WalletImputation :: getUid,query.getUid());
        }
       if(Objects.nonNull(query.getCoin())){
            queryWrapper = queryWrapper.eq(WalletImputation :: getCoin,query.getCoin());
        }
       if(Objects.nonNull(query.getUid())){
            queryWrapper = queryWrapper.eq(WalletImputation :: getNetwork,query.getNetwork());
        }
       if(Objects.nonNull(query.getUid())){
            queryWrapper = queryWrapper.eq(WalletImputation :: getStatus,query.getStatus());
        }

        return chainLogMapper.selectPage(page, queryWrapper).convert(chainConverter::toWalletImputationVO);
    }


}
