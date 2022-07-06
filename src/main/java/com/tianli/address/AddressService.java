package com.tianli.address;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.tianli.address.mapper.Address;
import com.tianli.address.mapper.AddressMapper;
import com.tianli.common.CommonFunction;
import com.tianli.common.blockchain.BscTriggerContract;
import com.tianli.common.blockchain.EthTriggerContract;
import com.tianli.common.blockchain.TronTriggerContract;
import com.tianli.common.lock.RedisLock;
import com.tianli.currency.CurrencyTypeEnum;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.mconfig.ConfigService;
import com.tianli.tool.MapBuilder;
import com.tianli.tool.MapTool;
import com.tianli.tool.crypto.Crypto;
import com.tianli.tool.http.HttpHandler;
import com.tianli.tool.http.HttpRequest;
import com.tianli.tool.judge.JsonObjectTool;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.crypto.util.DigestFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
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

    public Address get_(long uid, CurrencyTypeEnum typeEnum) throws IOException {
        redisLock.lock("AddressService.get_" + uid + "_" + typeEnum.name(), 1L, TimeUnit.MINUTES);
        Address address = super.getOne(new LambdaQueryWrapper<Address>().eq(Address::getType, typeEnum).eq(Address::getUid, uid));
        if (address != null) return address;
        long generalId = CommonFunction.generalId();
        String bsc = bscTriggerContract.computeAddress(generalId);
        String tron = tronTriggerContract.computeAddress(generalId);
        String eth = ethTriggerContract.computeAddress(generalId);
        if (StringUtils.isEmpty(bsc) || StringUtils.isEmpty(tron) || StringUtils.isEmpty(eth)) ErrorCodeEnum.NETWORK_ERROR.throwException();
        address = Address.builder()
                .id(generalId)
                .uid(uid)
                .create_time(LocalDateTime.now())
                .type(typeEnum)
                .tron(tron)
                .bsc(bsc)
                .eth(eth)
                .build();
        if (!super.save(address)) ErrorCodeEnum.SYSTEM_ERROR.throwException();
        return address;
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
