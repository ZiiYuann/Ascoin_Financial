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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;


/**
 * @Author cs
 * @Date 2022-12-26 17:26
 */
@Slf4j
@Service
public class OccasionalAddressService extends ServiceImpl<OccasionalAddressMapper, OccasionalAddress> {
    private final static String OCCASIONAL_ADDRESS_LOCK = "occasional:address:lock:";
    private final static String REGISTER_OCCASIONAL_ADDRESS_LOCK = "register:occasional:address:lock";

    @Resource
    private RedissonClient redisson;
    @Resource
    private ContractAdapter contractAdapter;
    @Resource
    private UutokenHttpService uutokenHttpService;

    public OccasionalAddress get(String address, ChainType chain) {
        return this.getOne(Wrappers.lambdaQuery(OccasionalAddress.class).eq(OccasionalAddress::getAddress, address).eq(OccasionalAddress::getChain, chain));
    }

    public void registered(String address, ChainType chain) {
        this.update(Wrappers.lambdaUpdate(OccasionalAddress.class).set(OccasionalAddress::getRegistered, true).eq(OccasionalAddress::getAddress, address).eq(OccasionalAddress::getChain, chain));
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
                            .createTime(LocalDateTime.now())
                            .registered(false)
                            .retryCount(0).build();
                    this.save(address);
                    boolean registered = uutokenHttpService.registerAddress(chain, addressStr);
                    if(registered) {
                        registered(addressStr, chain);
                    }
                }
            } catch (IOException e) {
                log.error("compute address failed addressId:{} chain:{}", addressId, chain, e);
                throw ErrorCodeEnum.GENERATE_CHARGE_ADDRESS_FAILED.generalException();
            } finally {
                if(lock.isLocked() && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        } else {
            if(!address.getRegistered()) {
                ErrorCodeEnum.CHARGE_ADDRESS_NOT_FOUND.throwException();
            }
        }
        return address.getAddress();
    }

    @Scheduled(cron = "0/3 * * * * ?")
    void register() {
        RLock lock = redisson.getLock(REGISTER_OCCASIONAL_ADDRESS_LOCK);
        if(lock.tryLock()) {
            try {
                LocalDateTime now = LocalDateTime.now();
                List<OccasionalAddress> list = this.list(Wrappers.lambdaQuery(OccasionalAddress.class)
                        .eq(OccasionalAddress::getRegistered, false)
                        .le(OccasionalAddress::getUpdateTime, now)
                        .lt(OccasionalAddress::getRetryCount, 10));
                for (OccasionalAddress address : list) {
                    try {
                        boolean registered = uutokenHttpService.registerAddress(address.getChain(), address.getAddress());
                        if(registered) {
                            address.setRegistered(true);
                        }
                    } catch (Exception e) {
                        log.error("pushMessage error:", e);
                    } finally {
                        address.setRetryCount(address.getRetryCount() + 1);
                        Long seconds = 5L;
                        long pushDelaySeconds = BigInteger.ONE.shiftLeft(address.getRetryCount()).longValue();
                        if (pushDelaySeconds > seconds) {
                            seconds = pushDelaySeconds;
                        }
                        address.setUpdateTime(now.plusSeconds(seconds));
                        updateById(address);
                    }
                }
            } finally {
                if(lock.isLocked() && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
    }

}
