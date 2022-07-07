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

    /**
     * 获取用户的账户地址 如果没有的话会初始化
     */
    @SneakyThrows
    @Transactional
    public Address getAndInit(long uid, ProductType typeEnum){
        redisLock.lock("AddressService.get_" + uid + "_" + typeEnum.name(), 1L, TimeUnit.MINUTES);
        Address address = this.get(uid,typeEnum);
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
                .type(typeEnum)
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
    public Address get(long uid, ProductType typeEnum){
        return super.getOne(new LambdaQueryWrapper<Address>().eq(Address::getType, typeEnum).eq(Address::getUid, uid));
    }

    public Address getByEth(String to_address) {
        return baseMapper.getByEth(to_address);
    }

    public Address getByTron(String to_address) {
        return baseMapper.getByTron(to_address);
    }

    public Address getByBsc(String to_address) {
        return baseMapper.getByBsc(to_address);
    }
}
