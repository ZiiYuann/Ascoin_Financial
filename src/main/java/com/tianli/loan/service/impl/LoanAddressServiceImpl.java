package com.tianli.loan.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tianli.common.CommonFunction;
import com.tianli.common.blockchain.BscTriggerContract;
import com.tianli.common.blockchain.EthTriggerContract;
import com.tianli.common.blockchain.TronTriggerContract;
import com.tianli.common.lock.RedisLock;
import com.tianli.currency_token.mapper.ChainType;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.loan.entity.LoanAddress;
import com.tianli.loan.dao.LoanAddressMapper;
import com.tianli.loan.service.ILoanAddressService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 用户还款地址表 服务实现类
 * </p>
 *
 * @author lzy
 * @since 2022-05-31
 */
@Service
public class LoanAddressServiceImpl extends ServiceImpl<LoanAddressMapper, LoanAddress> implements ILoanAddressService {

    @Resource
    RedisLock redisLock;

    @Resource
    private BscTriggerContract bscTriggerContract;
    @Resource
    private TronTriggerContract tronTriggerContract;
    @Resource
    private EthTriggerContract ethTriggerContract;

    @SneakyThrows
    @Override
    public LoanAddress findByUid(Long uid) {
        LoanAddress loanAddress = this.getOne(Wrappers.lambdaQuery(LoanAddress.class).eq(LoanAddress::getUid, uid));
        if (ObjectUtil.isNull(loanAddress)) {
            String LOAN_ADDRESS_KEY = "loan_address_uid:{}";
            redisLock.lock(StrUtil.format(LOAN_ADDRESS_KEY, uid), 60L, TimeUnit.SECONDS);
            long generalId = CommonFunction.generalId();
            String bsc = bscTriggerContract.computeAddress(generalId);
            String tron = tronTriggerContract.computeAddress(generalId);
            String eth = ethTriggerContract.computeAddress(generalId);
            if (StringUtils.isEmpty(bsc) || StringUtils.isEmpty(tron) || StringUtils.isEmpty(eth)) {
                ErrorCodeEnum.NETWORK_ERROR.throwException();
            }
            loanAddress = new LoanAddress(generalId, uid, null, eth, tron, bsc);
            this.save(loanAddress);
        }
        return loanAddress;
    }

    @Override
    public LoanAddress findByAddress(String address, ChainType chainType) {
        LambdaQueryWrapper<LoanAddress> query = Wrappers.lambdaQuery(LoanAddress.class);
        switch (chainType) {
            case bep20:
                query.eq(LoanAddress::getBsc, address);
                break;
            case erc20:
                query.eq(LoanAddress::getEth, address);
                break;
            case trc20:
                query.eq(LoanAddress::getTron, address);
                break;
            default:
                throw ErrorCodeEnum.ARGUEMENT_ERROR.generalException();
        }
        return this.getOne(query);
    }
}
