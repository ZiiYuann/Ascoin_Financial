package com.tianli.address;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.address.mapper.Address;
import com.tianli.address.mapper.AddressMapper;
import com.tianli.common.CommonFunction;
import com.tianli.common.blockchain.BscTriggerContract;
import com.tianli.common.blockchain.EthTriggerContract;
import com.tianli.common.blockchain.TronTriggerContract;
import com.tianli.common.lock.RedisLock;
import com.tianli.account.enums.ProductType;
import com.tianli.currency.enums.CurrencyAdaptType;
import com.tianli.exception.ErrorCodeEnum;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
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
    private BscTriggerContract bscTriggerContract;
    @Resource
    private TronTriggerContract tronTriggerContract;
    @Resource
    private EthTriggerContract ethTriggerContract;


    @Transactional
    public String getAddressByCurrencyAdaptType(Long uid,CurrencyAdaptType currencyAdaptType){
        Address address = this.get(uid);
        switch (currencyAdaptType.getCurrencyNetworkType()){
            case bep20: return address.getBsc();
            case erc20: return address.getEth();
            case trc20: return address.getTron();
            default: return null;
        }
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
        String bsc = bscTriggerContract.computeAddress(generalId);
        String tron = tronTriggerContract.computeAddress(generalId);
        String eth = ethTriggerContract.computeAddress(generalId);
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

    public Address getByEth(String toAddress) {
        return baseMapper.getByEth(toAddress);
    }

    public Address getByTron(String toAddress) {
        return baseMapper.getByTron(toAddress);
    }

    public Address getByBsc(String toAddress) {
        return baseMapper.getByBsc(toAddress);
    }
}
