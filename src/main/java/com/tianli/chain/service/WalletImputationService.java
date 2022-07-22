package com.tianli.chain.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.chain.converter.ChainConverter;
import com.tianli.chain.dto.TRONTokenReq;
import com.tianli.chain.entity.WalletImputation;
import com.tianli.chain.entity.WalletImputationTemporary;
import com.tianli.chain.enums.ImputationStatus;
import com.tianli.chain.mapper.ChainLogMapper;
import com.tianli.chain.mapper.WalletImputationMapper;
import com.tianli.chain.mapper.WalletImputationTemporaryMapper;
import com.tianli.currency.enums.CurrencyAdaptType;
import com.tianli.management.query.WalletImputationQuery;
import com.tianli.chain.vo.WalletImputationVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class WalletImputationService extends ServiceImpl<WalletImputationMapper, WalletImputation> {

    @Resource
    private WalletImputationMapper walletImputationMapper;
    @Resource
    private ChainConverter chainConverter;
    @Resource
    private WalletImputationTemporaryMapper walletImputationTemporaryMapper;

    /**
     * 通过订单插入或修改归集信息
     */
    @Transactional
    public void insert(Long uid, CurrencyAdaptType currencyAdaptType,TRONTokenReq tronTokenReq){
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<WalletImputation> query = new LambdaQueryWrapper<WalletImputation>().eq(WalletImputation::getUid, uid)
                .eq(WalletImputation::getNetwork, currencyAdaptType.getNetwork())
                .eq(WalletImputation::getCoin, currencyAdaptType.getCurrencyCoin())
                .eq(WalletImputation::getAddress, tronTokenReq.getTo())
                .last("for update");

        // 操作归集信息的时候不允许管理端进行归集操作
        WalletImputation walletImputation = walletImputationMapper.selectOne(query);

        if(Objects.nonNull(walletImputation) ){
            // 如果处于进行中
            if(ImputationStatus.processing.equals(walletImputation.getStatus())){
                WalletImputationTemporary walletImputationTemporary = chainConverter.toTemporary(walletImputation);
                walletImputationTemporary.setAmount(tronTokenReq.getValue());
                walletImputationTemporaryMapper.insert(walletImputationTemporary);
            }

            if(ImputationStatus.success.equals(walletImputation.getStatus())){
                walletImputation.setAmount(tronTokenReq.getValue());
                walletImputation.setUpdateTime(now);
                walletImputation.setStatus(ImputationStatus.wait);
                walletImputationMapper.updateById(walletImputation);
            }

            if(ImputationStatus.wait.equals(walletImputation.getStatus())){
                walletImputation.setAmount(walletImputation.getAmount().add(tronTokenReq.getValue()));
                walletImputation.setUpdateTime(now);
                walletImputationMapper.updateById(walletImputation);
            }
            return;
        }

        WalletImputation walletImputationInsert = WalletImputation.builder()
                .uid(uid)
                .network(currencyAdaptType.getNetwork())
                .coin(currencyAdaptType.getCurrencyCoin())
                .address(tronTokenReq.getTo())
                .status(ImputationStatus.wait)
                .createTime(now).updateTime(now)
                .build();
        walletImputationMapper.insert(walletImputationInsert);
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

        return walletImputationMapper.selectPage(page, queryWrapper).convert(chainConverter::toWalletImputationVO);
    }


}
