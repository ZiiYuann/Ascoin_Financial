package com.tianli.account.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.account.mapper.AccountBalanceOperationLogMapper;
import com.tianli.common.CommonFunction;
import com.tianli.currency.CurrencyTokenEnum;
import com.tianli.account.enums.ProductType;
import com.tianli.account.entity.AccountBalanceOperationLog;
import com.tianli.currency.log.CurrencyLogType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * <p>
 * 余额变动记录表 服务实现类
 * </p>
 *
 * @author hd
 * @since 2020-12-04
 */
@Service
public class AccountBalanceOperationLogService extends ServiceImpl<AccountBalanceOperationLogMapper, AccountBalanceOperationLog>{

    /**
     * 添加余额操作日志
     */
    public void save(long uid, ProductType type, CurrencyTokenEnum token, CurrencyLogType logType, BigInteger amount, String sn,
                     String des, BigInteger balance, BigInteger freeze, BigInteger remain) {
        AccountBalanceOperationLog currencyLog = AccountBalanceOperationLog.builder()
                .id(CommonFunction.generalId())
                .uid(uid)
                .type(type)
                .token(token)
                .sn(sn)
                .log_type(logType)
                .des(des)
                .balance(balance)
                .freeze(freeze)
                .remain(remain)
                .amount(amount)
                .create_time(LocalDateTime.now())
                .build();
        accountBalanceOperationLogMapper.insert(currencyLog);
    }

    public BigInteger sumMiningAmount(Long uid) {
        return accountBalanceOperationLogMapper.selectSumMiningAmount(uid);
    }

    @Resource
    private AccountBalanceOperationLogMapper accountBalanceOperationLogMapper;

}
