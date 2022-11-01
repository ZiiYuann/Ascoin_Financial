package com.tianli.chain.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.address.AddressService;
import com.tianli.address.mapper.Address;
import com.tianli.chain.converter.ChainConverter;
import com.tianli.chain.dto.TRONTokenReq;
import com.tianli.chain.entity.WalletImputation;
import com.tianli.chain.entity.WalletImputationLog;
import com.tianli.chain.entity.WalletImputationLogAppendix;
import com.tianli.chain.enums.ImputationStatus;
import com.tianli.chain.mapper.WalletImputationMapper;
import com.tianli.chain.service.contract.ContractAdapter;
import com.tianli.chain.service.contract.ContractOperation;
import com.tianli.chain.vo.WalletImputationVO;
import com.tianli.charge.service.OrderService;
import com.tianli.common.CommonFunction;
import com.tianli.common.RedisLockConstants;
import com.tianli.common.blockchain.NetworkType;
import com.tianli.common.lock.RedisLock;
import com.tianli.currency.enums.TokenAdapter;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.management.dto.AmountDto;
import com.tianli.management.entity.HotWalletDetailed;
import com.tianli.management.enums.HotWalletOperationType;
import com.tianli.management.query.WalletImputationManualQuery;
import com.tianli.management.query.WalletImputationQuery;
import com.tianli.management.service.HotWalletDetailedService;
import com.tianli.management.vo.ImputationAmountVO;
import com.tianli.task.RetryScheduledExecutor;
import com.tianli.task.RetryTaskInfo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class WalletImputationService extends ServiceImpl<WalletImputationMapper, WalletImputation> {

    @Resource
    private WalletImputationMapper walletImputationMapper;
    @Resource
    private ChainConverter chainConverter;
    @Resource
    private ContractAdapter baseContractService;
    @Resource
    private WalletImputationLogService walletImputationLogService;
    @Resource
    private WalletImputationLogAppendixService walletImputationLogAppendixService;
    @Resource
    private RedisLock redisLock;
    @Resource
    private HotWalletDetailedService hotWalletDetailedService;
    @Resource
    private AddressService addressService;
    @Resource
    private OrderService orderService;


    /**
     * 通过订单插入或修改归集信息
     */
    @Transactional
    public void insert(Long uid, Address address, TokenAdapter tokenAdapter
            , TRONTokenReq tronTokenReq, BigDecimal finalAmount) {

        StringBuilder keyBuilder = new StringBuilder()
                .append(RedisLockConstants.RECYCLE_LOCK).append(":").append(tokenAdapter.getNetwork().name())
                .append(":").append(tokenAdapter.getCurrencyCoin().name()).append(":").append(tronTokenReq.getTo());
        redisLock.lock(keyBuilder.toString(), 1L, TimeUnit.MINUTES);

        LocalDateTime now = LocalDateTime.now();

        // uid,network,coin 处于wait状态的记录 <= 1
        LambdaQueryWrapper<WalletImputation> query = new LambdaQueryWrapper<WalletImputation>()
                .eq(WalletImputation::getUid, uid)
                .eq(WalletImputation::getNetwork, tokenAdapter.getNetwork())
                .eq(WalletImputation::getCoin, tokenAdapter.getCurrencyCoin())
                .eq(WalletImputation::getStatus, ImputationStatus.wait);

        // 操作归集信息的时候不允许管理端进行归集操作
        WalletImputation walletImputation = walletImputationMapper.selectOne(query);

        if (Objects.nonNull(walletImputation)) {
            walletImputationMapper.increase(walletImputation.getId(), finalAmount);
            return;
        }

        WalletImputation walletImputationInsert = WalletImputation.builder()
                .uid(uid)
                .network(tokenAdapter.getNetwork())
                .coin(tokenAdapter.getCurrencyCoin())
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

        if (Objects.nonNull(query.getStatus())) {
            queryWrapper = queryWrapper.eq(WalletImputation::getStatus, query.getStatus());
        } else {
            queryWrapper = queryWrapper.in(WalletImputation::getStatus
                    , List.of(ImputationStatus.wait, ImputationStatus.processing, ImputationStatus.fail));
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

        // 检测是否有订单操作归集信息
        walletImputations.forEach(walletImputation -> {
            StringBuilder keyBuilder = new StringBuilder()
                    .append(RedisLockConstants.RECYCLE_LOCK).append(":").append(walletImputation.getNetwork().name())
                    .append(":").append(walletImputation.getCoin().name()).append(":").append(walletImputation.getAddress());
            redisLock.isLock(keyBuilder.toString());
        });


        if (coinCount > 1 || networkCount > 1) {
            log.info("不允许多个网络或者多个币别同时进行归集，ids:{}", imputationIds);
            ErrorCodeEnum.throwException("不允许多个网络或者多个币别同时进行归集");
        }
        var coin = walletImputations.stream().map(WalletImputation::getCoin).findAny().orElseThrow();
        var network = walletImputations.stream().map(WalletImputation::getNetwork).findAny().orElseThrow();
        TokenAdapter tokenAdapter = TokenAdapter.get(coin, network);

        List<Long> addressIds = walletImputations.stream().map(WalletImputation::getAddressId).collect(Collectors.toList());
        String hash = baseContractService.getOne(network).recycle(null, addressIds, tokenAdapter.getContractAddressList());
        // 事务问题如何解决，如果中间出现异常，整个事务回滚，归集状态为wait，重新归集只收取手续费

        if (StringUtils.isBlank(hash)) {
            ErrorCodeEnum.throwException("上链失败");
        }

        BigDecimal amount = walletImputations.stream().map(WalletImputation::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        Long logId = CommonFunction.generalId();
        WalletImputationLog walletImputationLog = WalletImputationLog.builder()
                .id(logId)
                .amount(amount)
                .txid(hash)
                .coin(coin)
                .network(network)
                .status(ImputationStatus.processing)
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
            walletImputation.setStatus(ImputationStatus.processing);
            walletImputation.setLogId(logId);
            return walletImputation;
        }).collect(Collectors.toList());
        this.updateBatchById(walletImputationsUpdate);

        // 异步检测数据
        asynCheckImputationStatus();

    }

    /**
     * 手动执行归集操作
     */
    public String imputationOperationManual(NetworkType network, TokenAdapter tokenAdapter, List<String> addresses) {
        LambdaQueryWrapper<Address> query;
        if (NetworkType.trc20.equals(network)) {
            query = new LambdaQueryWrapper<Address>().in(Address::getTron, addresses);
        } else {
            query = new LambdaQueryWrapper<Address>().in(Address::getBsc, addresses);
        }
        List<Long> addressIds = Optional.ofNullable(addressService.list(query)).orElse(new ArrayList<>())
                .stream().map(Address::getId).collect(Collectors.toList());
        return baseContractService.getOne(network).recycle(null, addressIds, tokenAdapter.getContractAddressList());
    }

    /**
     * 进行归集补偿
     */
    @Transactional
    public void imputationCompensate(Long imputationId, ImputationStatus status) {
        WalletImputation walletImputation = walletImputationMapper.selectById(imputationId);

        if (Objects.isNull(walletImputation) || !ImputationStatus.processing.equals(walletImputation.getStatus())
                || Objects.isNull(walletImputation.getLogId())) {
            ErrorCodeEnum.throwException("本次归集不需要补偿");
        }

        Long logId = walletImputation.getLogId();
        Optional.of(walletImputation).ifPresent(o -> {
            o.setStatus(status);
            o.setUpdateTime(LocalDateTime.now());
            walletImputationMapper.updateById(o);
        });


        WalletImputationLog walletImputationLog = walletImputationLogService.getById(logId);
        if (Objects.nonNull(walletImputationLog)) {
            walletImputationLog.setFinishTime(LocalDateTime.now());
            walletImputationLog.setStatus(status);
            walletImputationLogService.updateById(walletImputationLog);
        }

    }

    /**
     * 进行归集补偿
     */
    @Transactional
    public void imputationCompensateScan(Long imputationId) {
        WalletImputation walletImputation = walletImputationMapper.selectById(imputationId);
        if (Objects.isNull(walletImputation) || !ImputationStatus.processing.equals(walletImputation.getStatus())
                || Objects.isNull(walletImputation.getLogId())) {
            ErrorCodeEnum.throwException("本次归集不需要补偿");
        }
        WalletImputationLog walletImputationLog = walletImputationLogService.getById(walletImputation.getLogId());
        List<WalletImputation> walletImputations = walletImputationMapper.selectList(new LambdaQueryWrapper<WalletImputation>()
                .eq(WalletImputation::getLogId, walletImputation.getLogId()));
        if (Objects.nonNull(walletImputationLog)) {
            checkImputationStatus(walletImputation.getLogId(), walletImputations);
        }
    }

    /**
     * 异步检测归集状态
     */
    private void asynCheckImputationStatus() {
        var query =
                new LambdaQueryWrapper<WalletImputation>().eq(WalletImputation::getStatus, ImputationStatus.processing);
        List<WalletImputation> walletImputations = walletImputationMapper.selectList(query);
        walletImputations.stream().collect(Collectors.groupingBy(WalletImputation::getLogId)).entrySet()
                .stream()
                .forEach(entry -> RetryScheduledExecutor.DEFAULT_EXECUTOR.schedule(() -> checkImputationStatus(entry.getKey(), entry.getValue()),
                        10, TimeUnit.MINUTES
                        , new RetryTaskInfo<>("asynCheckImputationStatus", "异步定时扫链检测归集状态", entry)));
    }

    @Transactional
    public void checkImputationStatus(Long logId, List<WalletImputation> walletImputations) {
        WalletImputationLog walletImputationLog = walletImputationLogService.getById(logId);
        if (!ImputationStatus.processing.equals(walletImputationLog.getStatus()) || StringUtils.isBlank(walletImputationLog.getTxid())) {
            log.error("当前数据异常，请校验{}", walletImputationLog);
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }

        NetworkType network = walletImputationLog.getNetwork();
        String txid = walletImputationLog.getTxid();
        ContractOperation contractOperation = baseContractService.getOne(network);

        ImputationStatus status = contractOperation.successByHash(txid) ? ImputationStatus.success : ImputationStatus.fail;

        // 更新信息
        LocalDateTime now = LocalDateTime.now();
        walletImputationLog.setStatus(status);
        walletImputationLog.setFinishTime(now);
        walletImputationLogService.updateById(walletImputationLog);

        walletImputations.forEach(walletImputation -> {
            walletImputation.setStatus(status);
            walletImputation.setUpdateTime(now);
        });
        this.updateBatchById(walletImputations);


        if (!ImputationStatus.success.equals(status)) {
            return;
        }

        Address configAddress = addressService.getConfigAddress();
        String toAddress = null;
        switch (network) {
            case bep20:
                toAddress = configAddress.getBsc();
                break;
            case erc20:
                toAddress = configAddress.getEth();
                break;
            case trc20:
                toAddress = configAddress.getTron();
                break;
            default:
                break;
        }

        // 插入热钱包操作数据表
        HotWalletDetailed hotWalletDetailed = HotWalletDetailed.builder()
                .id(CommonFunction.generalId())
                .uid("0")
                .amount(walletImputationLog.getAmount())
                .coin(walletImputationLog.getCoin())
                .chain(network.getChainType())
                .fromAddress(walletImputationLog.getFromAddress())
                .toAddress(toAddress)
                .hash(txid)
                .type(HotWalletOperationType.imputation)
                .createTime(walletImputationLog.getFinishTime()).build();
        hotWalletDetailedService.insert(hotWalletDetailed);
    }


    public ImputationAmountVO amount(WalletImputationQuery query) {
        WalletImputationQuery waitQuery = new WalletImputationQuery();
        waitQuery.setWait(true);
        BigDecimal totalAmount = orderService.calDollarAmount(this.getBaseMapper().imputationAmount(waitQuery));
        ImputationAmountVO imputationAmountVO = new ImputationAmountVO();
        imputationAmountVO.setTotalAmount(totalAmount);

        if (Objects.isNull(query.getStatus())) {
            query.setWait(true);
        }
        List<AmountDto> amountDtos = this.getBaseMapper().imputationAmount(query);
        Optional<AmountDto> optionalAmountDto = amountDtos.stream().filter(o -> o.getCoin().equals(query.getCoin())).findFirst();
        if (optionalAmountDto.isPresent()) {
            AmountDto amountDto = optionalAmountDto.get();
            imputationAmountVO.setAmount(amountDto.getAmount());
            imputationAmountVO.setCoin(query.getCoin());
        }
        return imputationAmountVO;
    }
}
