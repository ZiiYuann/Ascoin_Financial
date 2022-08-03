package com.tianli.chain.service;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.address.mapper.Address;
import com.tianli.chain.converter.ChainConverter;
import com.tianli.chain.dto.TRONTokenReq;
import com.tianli.chain.entity.WalletImputation;
import com.tianli.chain.entity.WalletImputationLog;
import com.tianli.chain.entity.WalletImputationLogAppendix;
import com.tianli.chain.entity.WalletImputationTemporary;
import com.tianli.chain.enums.ImputationStatus;
import com.tianli.chain.mapper.WalletImputationMapper;
import com.tianli.chain.mapper.WalletImputationTemporaryMapper;
import com.tianli.chain.service.contract.ContractAdapter;
import com.tianli.chain.vo.WalletImputationVO;
import com.tianli.common.CommonFunction;
import com.tianli.common.blockchain.NetworkType;
import com.tianli.currency.enums.CurrencyAdaptType;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.management.query.WalletImputationManualQuery;
import com.tianli.management.query.WalletImputationQuery;
import com.tianli.mconfig.ConfigService;
import com.tianli.tool.TXUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WalletImputationService extends ServiceImpl<WalletImputationMapper, WalletImputation> {

    @Resource
    private WalletImputationMapper walletImputationMapper;
    @Resource
    private ChainConverter chainConverter;
    @Resource
    private WalletImputationTemporaryMapper walletImputationTemporaryMapper;
    @Resource
    private ContractAdapter baseContractService;
    @Resource
    private WalletImputationLogService walletImputationLogService;
    @Resource
    private WalletImputationLogAppendixService walletImputationLogAppendixService;
    @Resource
    private ConfigService configService;

    /**
     * 通过订单插入或修改归集信息
     */
    @Transactional
    public void insert(Long uid, Address address, CurrencyAdaptType currencyAdaptType
            , TRONTokenReq tronTokenReq, BigDecimal finalAmount) {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<WalletImputation> query = new LambdaQueryWrapper<WalletImputation>().eq(WalletImputation::getUid, uid)
                .eq(WalletImputation::getNetwork, currencyAdaptType.getNetwork())
                .eq(WalletImputation::getCoin, currencyAdaptType.getCurrencyCoin())
                .eq(WalletImputation::getAddress, tronTokenReq.getTo())
                .last("for update");

        // 操作归集信息的时候不允许管理端进行归集操作
        WalletImputation walletImputation = walletImputationMapper.selectOne(query);

        if (Objects.nonNull(walletImputation)) {
            // 如果处于进行中
            if (ImputationStatus.processing.equals(walletImputation.getStatus())) {
                WalletImputationTemporary walletImputationTemporary = chainConverter.toTemporary(walletImputation);
                walletImputationTemporary.setAmount(finalAmount);
                walletImputationTemporaryMapper.insert(walletImputationTemporary);
            }

            if (ImputationStatus.success.equals(walletImputation.getStatus())) {
                walletImputation.setAmount(finalAmount);
                walletImputation.setUpdateTime(now);
                walletImputation.setStatus(ImputationStatus.wait);
                walletImputationMapper.updateById(walletImputation);
            }

            if (ImputationStatus.wait.equals(walletImputation.getStatus())) {
                walletImputation.setAmount(walletImputation.getAmount().add(finalAmount));
                walletImputation.setUpdateTime(now);
                walletImputationMapper.updateById(walletImputation);
            }
            return;
        }

        WalletImputation walletImputationInsert = WalletImputation.builder()
                .uid(uid)
                .network(currencyAdaptType.getNetwork())
                .coin(currencyAdaptType.getCurrencyCoin())
                .addressId(address.getId())
                .address(tronTokenReq.getTo())
                .status(ImputationStatus.wait)
                .amount(finalAmount)
                .createTime(now).updateTime(now)
                .build();
        walletImputationMapper.insert(walletImputationInsert);
    }

    /**
     * 归集列表数据
     */
    public IPage<WalletImputationVO> walletImputationVOPage(IPage<WalletImputation> page, WalletImputationQuery query) {

        LambdaQueryWrapper<WalletImputation> queryWrapper = new LambdaQueryWrapper<>();

        if (Objects.nonNull(query.getUid())) {
            queryWrapper = queryWrapper.like(WalletImputation::getUid, query.getUid());
        }
        if (Objects.nonNull(query.getCoin())) {
            queryWrapper = queryWrapper.eq(WalletImputation::getCoin, query.getCoin());
        }

        if (Objects.nonNull(query.getNetwork())) {
            queryWrapper = queryWrapper.eq(WalletImputation::getNetwork, query.getNetwork());
        }

        if(Objects.nonNull(query.getStatus())){
            queryWrapper = queryWrapper.eq(WalletImputation::getStatus, query.getStatus());
        }else {
            queryWrapper = queryWrapper.in(WalletImputation::getStatus, List.of(ImputationStatus.wait,ImputationStatus.processing));
        }

        return walletImputationMapper.selectPage(page, queryWrapper).convert(chainConverter::toWalletImputationVO);
    }

    /**
     * 进行归集操作
     */
    @Transactional
    public void imputationOperation(WalletImputationManualQuery query) {
        List<Long> imputationIds = query.getImputationIds();
        List<WalletImputation> walletImputations = walletImputationMapper.selectBatchIds(imputationIds);
        long coinCount = walletImputations.stream().map(WalletImputation::getCoin).distinct().count();
        long networkCount = walletImputations.stream().map(WalletImputation::getNetwork).distinct().count();

        if (coinCount > 1 || networkCount > 1) {
            log.info("不允许多个网络或者多个币别同时进行归集，ids:{}", imputationIds);
            ErrorCodeEnum.throwException("不允许多个网络或者多个币别同时进行归集");
        }
        var coin = walletImputations.stream().map(WalletImputation::getCoin).findAny().orElseThrow();
        var network = walletImputations.stream().map(WalletImputation::getNetwork).findAny().orElseThrow();

        List<String> addresses = walletImputations.stream().map(WalletImputation::getAddress).collect(Collectors.toList());
        List<Long> addressIds = walletImputations.stream().map(WalletImputation::getAddressId).collect(Collectors.toList());
        String hash = baseContractService.getOne(network).recycle(null, CurrencyAdaptType.get(coin,network),addressIds, addresses);
        // 事务问题如何解决？

        if(StringUtils.isBlank(hash)){
            ErrorCodeEnum.throwException("上链失败");
        }

        BigDecimal amount = walletImputations.stream().map(WalletImputation::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        WalletImputationLog walletImputationLog = WalletImputationLog.builder()
                .id(CommonFunction.generalId())
                .amount(amount)
                .txid(hash)
                .coin(coin)
                .network(network)
                .status(ImputationStatus.success)
                .createTime(LocalDateTime.now()).build();
        walletImputationLogService.save(walletImputationLog);

        List<WalletImputationLogAppendix> logAppendices = walletImputations.stream().map(walletImputation -> {
            WalletImputationLogAppendix appendix = new WalletImputationLogAppendix();
            appendix.setAmount(walletImputation.getAmount());
            appendix.setNetwork(walletImputation.getNetwork());
            appendix.setFromAddress(walletImputation.getAddress());
            appendix.setTxid(hash);
            return appendix;
        }).collect(Collectors.toList());
        walletImputationLogAppendixService.saveBatch(logAppendices);

        var walletImputationsUpdate = walletImputations.stream().map(walletImputation -> {
            walletImputation.setStatus(ImputationStatus.success);
            return walletImputation;
        }).collect(Collectors.toList());
        this.updateBatchById(walletImputationsUpdate);

    }

    /**
     * 更新归集状态
     */
    @Transactional
    public void updateWalletImputationStatus(List<WalletImputation> walletImputations, NetworkType network, String hash, WalletImputationLog walletImputationLog) {
        String url = configService.get("data_center_url");
        Object o = TXUtil.insiderTrading(url, network.getChainType(), hash);
        if (ObjectUtil.isNull(o)) {
            return;
        }
        walletImputations.stream().forEach(walletImputation -> walletImputation.setStatus(ImputationStatus.success));
        walletImputationLog.setStatus(ImputationStatus.success);
        this.updateBatchById(walletImputations);
        walletImputationLogService.updateById(walletImputationLog);
    }
}
