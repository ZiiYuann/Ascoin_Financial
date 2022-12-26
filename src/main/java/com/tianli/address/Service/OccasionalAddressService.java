package com.tianli.address.Service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.address.mapper.OccasionalAddress;
import com.tianli.address.mapper.OccasionalAddressMapper;
import com.tianli.chain.enums.ChainType;
import com.tianli.chain.service.contract.ComputeAddress;
import com.tianli.common.CommonFunction;
import com.tianli.exception.ErrorCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


/**
 * @Author cs
 * @Date 2022-12-26 17:26
 */
@Slf4j
@Service
public class OccasionalAddressService extends ServiceImpl<OccasionalAddressMapper, OccasionalAddress> implements BeanFactoryAware {
    private final static String OCCASIONAL_ADDRESS_LOCK = "occasional:address:lock:";
    private List<ComputeAddress> computeAddressList;
    private DefaultListableBeanFactory beanFactory;

    @Resource
    private RedissonClient redisson;

    public String get(long uid, ChainType chain) {
        OccasionalAddress address = this.getOne(Wrappers.lambdaQuery(OccasionalAddress.class).eq(OccasionalAddress::getUid, uid).eq(OccasionalAddress::getChain, chain));
        if(address == null) {
            RLock lock = redisson.getLock(OCCASIONAL_ADDRESS_LOCK + uid + ":" + chain);
            try {
                lock.lock();
                address = this.getOne(Wrappers.lambdaQuery(OccasionalAddress.class).eq(OccasionalAddress::getUid, uid).eq(OccasionalAddress::getChain, chain));
                if(address == null) {
                    String addressStr = computeAddress(uid, chain);
                    address = OccasionalAddress.builder()
                            .id(CommonFunction.generalId())
                            .address(addressStr)
                            .uid(uid)
                            .chain(chain)
                            .createTime(LocalDateTime.now()).build();
                    this.save(address);
                }
            } catch (IOException e) {
                log.error("compute address failed uid:{} chain:{}", uid, chain, e);
                throw ErrorCodeEnum.GENERATE_CHARGE_ADDRESS_FAILED.generalException();
            } finally {
                if(lock.isLocked() && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
        return address.getAddress();
    }

    private String computeAddress(long uid, ChainType chain) throws IOException {
        for (ComputeAddress computeAddress : computeAddressList) {
            if(computeAddress.match(chain)) {
                return computeAddress.computeAddress(uid);
            }
        }
        log.error("{} chain will be used to compute address not found", chain);
        throw ErrorCodeEnum.GENERATE_CHARGE_ADDRESS_FAILED.generalException();
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = (DefaultListableBeanFactory) beanFactory;
        computeAddressList = new ArrayList<>(this.beanFactory.getBeansOfType(ComputeAddress.class).values());
    }
}
