package com.tianli.address;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.address.mapper.Address;
import com.tianli.address.mapper.AddressMapper;
import com.tianli.chain.dto.CallbackPathDTO;
import com.tianli.chain.service.ChainService;
import com.tianli.chain.service.contract.*;
import com.tianli.common.CommonFunction;
import com.tianli.common.ConfigConstants;
import com.tianli.common.blockchain.NetworkType;
import com.tianli.common.lock.RedisLock;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.mconfig.ConfigService;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 用户充值地址表 服务实现类
 * </p>
 *
 * @author hd
 * @since 2020-12-14
 */
@Service
public class AddressService extends ServiceImpl<AddressMapper, Address> {

    @Resource
    private RedisLock redisLock;
    @Resource
    private ContractAdapter baseContractService;
    @Resource
    private ConfigService configService;
    @Resource
    private ChainService chainService;

    /**
     * 激活钱包，并且会推送数据到数据中心
     */
    @Transactional
    public Address activityAccount(Long uid){
        Address address = getAndInit(uid);
        chainService.pushCondition(address, new CallbackPathDTO("/api/charge/recharge"));
        return address;
    }

    /**
     * 获取用户的账户地址 如果没有的话会初始化
     */
    @SneakyThrows
    @Transactional
    public Address getAndInit(long uid){
        redisLock.lock("AddressService.get_" + uid + "_", 1L, TimeUnit.MINUTES);
        Address address = this.get(uid);
        if (address != null) return address;
        long generalId = CommonFunction.generalId();
        String bsc = baseContractService.getOne(NetworkType.bep20).computeAddress(generalId);
        String tron = baseContractService.getOne(NetworkType.trc20).computeAddress(generalId);
        String eth = baseContractService.getOne(NetworkType.erc20).computeAddress(generalId);
        if (StringUtils.isEmpty(bsc) || StringUtils.isEmpty(tron) || StringUtils.isEmpty(eth)) ErrorCodeEnum.NETWORK_ERROR.throwException();
        address = Address.builder()
                .id(generalId)
                .uid(uid)
                .createTime(LocalDateTime.now())
                .tron(tron)
                .bsc(bsc)
                .eth(eth)
                .build();
        if (!super.save(address)) ErrorCodeEnum.SYSTEM_ERROR.throwException();
        return address;
    }

    /**
     * 获取用户云钱包地址
     */
    public Address get(long uid){
        return super.getOne(new LambdaQueryWrapper<Address>().eq(Address::getUid, uid));
    }

    /**
     * 获取系统钱包地址
     */
    public Address getConfigAddress(){
        String bscWalletAddress = configService.get(ConfigConstants.BSC_MAIN_WALLET_ADDRESS);
        String ethWalletAddress = configService.get(ConfigConstants.ETH_MAIN_WALLET_ADDRESS);
        String tronWalletAddress = configService.get(ConfigConstants.TRON_MAIN_WALLET_ADDRESS);

       return Address.builder()
                .bsc(bscWalletAddress)
                .eth(ethWalletAddress)
                .tron(tronWalletAddress)
                .build();
    }

    public Address getByEth(String toAddress) {
        return baseMapper.getByEth(toAddress);
    }

    public Address getByTron(String toAddress) {
        return baseMapper.getByTron(toAddress);
    }

    public Address getByBsc(String toAddress) {
        return baseMapper.getByBsc(toAddress);
    }

    public BigInteger activeCount(LocalDateTime startTime ,LocalDateTime endTime){
        LambdaQueryWrapper<Address> queryWrapper =
                new LambdaQueryWrapper<Address>().between(Address::getCreateTime, startTime, endTime);
        Integer integer = baseMapper.selectCount(queryWrapper);
        return BigInteger.valueOf(integer);
    }
}
