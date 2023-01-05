package com.tianli.address.Service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.address.mapper.OccasionalAddress;
import com.tianli.address.mapper.OccasionalAddressMapper;
import com.tianli.chain.enums.ChainType;
import com.tianli.chain.service.UutokenHttpService;
import com.tianli.chain.service.contract.ContractAdapter;
import com.tianli.common.CommonFunction;
import com.tianli.common.blockchain.NetworkType;
import com.tianli.exception.ErrorCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;


/**
 * @Author cs
 * @Date 2022-12-26 17:26
 */
@Slf4j
@Service
public class OccasionalAddressService extends ServiceImpl<OccasionalAddressMapper, OccasionalAddress> {
    private final static String OCCASIONAL_ADDRESS_LOCK = "occasional:address:lock:";

    @Resource
    private RedissonClient redisson;
    @Resource
    private ContractAdapter contractAdapter;
    @Resource
    private UutokenHttpService uutokenHttpService;

    public OccasionalAddress get(String address, ChainType chain) {
        return this.getOne(Wrappers.lambdaQuery(OccasionalAddress.class).eq(OccasionalAddress::getAddress, address).eq(OccasionalAddress::getChain, chain));
    }

    public String get(long addressId, ChainType chain) {
        OccasionalAddress address = this.getOne(Wrappers.lambdaQuery(OccasionalAddress.class).eq(OccasionalAddress::getAddressId, addressId).eq(OccasionalAddress::getChain, chain));
        if(address == null) {
            RLock lock = redisson.getLock(OCCASIONAL_ADDRESS_LOCK + addressId + ":" + chain);
            try {
                lock.lock();
                address = this.getOne(Wrappers.lambdaQuery(OccasionalAddress.class).eq(OccasionalAddress::getAddressId, addressId).eq(OccasionalAddress::getChain, chain));
                if(address == null) {
                    String addressStr = contractAdapter.getOne(NetworkType.getInstance(chain)).computeAddress(addressId);
                    address = OccasionalAddress.builder()
                            .id(CommonFunction.generalId())
                            .address(addressStr)
                            .addressId(addressId)
                            .chain(chain)
                            .createTime(LocalDateTime.now()).build();
                    this.save(address);
                    uutokenHttpService.registerAddress(chain, addressStr);
                }
            } catch (IOException e) {
                log.error("compute address failed addressId:{} chain:{}", addressId, chain, e);
                throw ErrorCodeEnum.GENERATE_CHARGE_ADDRESS_FAILED.generalException();
            } finally {
                if(lock.isLocked() && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
        return address.getAddress();
    }

}
