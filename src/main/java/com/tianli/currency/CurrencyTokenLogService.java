package com.tianli.currency;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.common.CommonFunction;
import com.tianli.common.blockchain.CurrencyCoinEnum;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.currency.log.CurrencyLogType;
import com.tianli.currency.mapper.CurrencyTokenLog;
import com.tianli.currency.mapper.CurrencyTokenLogMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class CurrencyTokenLogService extends ServiceImpl<CurrencyTokenLogMapper, CurrencyTokenLog> {

    public void add(long uid, CurrencyTypeEnum type, CurrencyCoinEnum token, CurrencyLogType logType, BigDecimal amount, String sn,
                    CurrencyLogDes des, BigDecimal balance, BigDecimal freeze, BigDecimal remain) {
        CurrencyTokenLog currencyTokenLog = CurrencyTokenLog.builder()
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
        currencyTokenLogMapper.insert(currencyTokenLog);
    }

    public CurrencyTokenLog findBySn(String sn, Long uid, CurrencyLogType currencyLogType, CurrencyTypeEnum currencyTypeEnum) {
        return this.getOne(Wrappers.lambdaQuery(CurrencyTokenLog.class)
                .eq(CurrencyTokenLog::getSn, sn)
                .eq(CurrencyTokenLog::getUid, uid)
                .eq(CurrencyTokenLog::getLog_type, currencyLogType)
                .eq(CurrencyTokenLog::getType, currencyTypeEnum));
    }

    @Resource
    private CurrencyTokenLogMapper currencyTokenLogMapper;
}
