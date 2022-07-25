package com.tianli.chain.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.chain.converter.ChainConverter;
import com.tianli.chain.entity.WalletImputationLogAppendix;
import com.tianli.chain.mapper.WalletImputationLogAppendixMapper;
import com.tianli.chain.vo.WalletImputationLogAppendixVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-25
 **/
@Service
public class WalletImputationLogAppendixService extends ServiceImpl<WalletImputationLogAppendixMapper, WalletImputationLogAppendix> {

    @Resource
    private WalletImputationLogAppendixMapper walletImputationAppendixMapper;
    @Resource
    private ChainConverter chainConverter;

    public IPage<WalletImputationLogAppendixVO> pageByTxid(IPage<WalletImputationLogAppendix> page, String txid){
        var queryWrapper =
                new LambdaQueryWrapper<WalletImputationLogAppendix>().eq(WalletImputationLogAppendix::getTxid, txid);

       return walletImputationAppendixMapper.selectPage(page,queryWrapper).convert(chainConverter ::toWalletImputationLogAppendixVO);
    }
}
