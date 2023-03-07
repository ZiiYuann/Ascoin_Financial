package com.tianli.account.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.account.entity.AccountBalance;
import com.tianli.account.entity.AccountBalanceOperationLog;
import com.tianli.account.enums.AccountOperationType;
import com.tianli.account.mapper.AccountBalanceOperationLogMapper;
import com.tianli.charge.enums.ChargeType;
import com.tianli.common.CommonFunction;
import com.tianli.common.blockchain.NetworkType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
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
public class AccountBalanceOperationLogService extends ServiceImpl<AccountBalanceOperationLogMapper, AccountBalanceOperationLog> {

    @Resource
    private AccountBalanceOperationLogMapper accountBalanceOperationLogMapper;

    /**
     * 添加余额操作日志
     */
    public void save(AccountBalance accountBalance, ChargeType type, String coin, NetworkType networkType
            , BigDecimal amount, String sn) {
        AccountBalanceOperationLog currencyLog = AccountBalanceOperationLog.builder()
                .id(CommonFunction.generalId())
                .uid(accountBalance.getUid())
                .accountBalanceId(accountBalance.getId())
                .coin(coin)
                .network(networkType)
                .chargeType(type)
                .orderNo(sn)
                .des(type.getNameZn())
                .balance(accountBalance.getBalance())
                .freeze(accountBalance.getFreeze())
                .remain(accountBalance.getRemain())
                .amount(amount)
                .createTime(LocalDateTime.now())
                .build();
        accountBalanceOperationLogMapper.insert(currencyLog);
    }

}
