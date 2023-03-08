package com.tianli.address.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.address.mapper.AddressMnemonic;
import com.tianli.address.mapper.AddressMnemonicMapper;
import com.tianli.common.CommonFunction;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.tool.DataSecurityTool;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import party.loveit.bip44forjava.utils.Bip44Utils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @Author cs
 * @Date 2022-12-26 15:28
 */
@Slf4j
@Service
public class AddressMnemonicService extends ServiceImpl<AddressMnemonicMapper, AddressMnemonic> {
    private static final String ADDRESS_MNEMONIC_LOCK = "address:mnemonic:lock:";
    @Resource
    private RedissonClient redisson;
    @Resource
    private DataSecurityTool dataSecurityTool;

    public String getMnemonic(long addressId) {
        AddressMnemonic addressMnemonic = this.getOne(Wrappers.lambdaQuery(AddressMnemonic.class).eq(AddressMnemonic::getAddressId, addressId));
        if(addressMnemonic == null) {
            RLock lock = redisson.getLock(ADDRESS_MNEMONIC_LOCK + addressId);
            try {
                lock.lock();
                addressMnemonic = this.getOne(Wrappers.lambdaQuery(AddressMnemonic.class).eq(AddressMnemonic::getAddressId, addressId));
                if(addressMnemonic == null) {
                    List<String> keys = Bip44Utils.generateMnemonicWords();
                    String mnemonic = StringUtils.collectionToDelimitedString(keys, " ");
                    addressMnemonic = AddressMnemonic.builder().id(CommonFunction.generalId()).addressId(addressId).mnemonic(dataSecurityTool.encryptWithPublicKey(mnemonic)).createTime(LocalDateTime.now()).build();
                    this.save(addressMnemonic);
                }
            } catch (Exception e) {
                log.error("addressId: {} save charge address mnemonic failed", addressId, e);
                throw ErrorCodeEnum.GENERATE_MNEMONIC_FAILED.generalException();
            } finally {
                if(lock.isLocked() && lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
        return dataSecurityTool.decryptWithPrivateKey(addressMnemonic.getMnemonic());
    }
}
