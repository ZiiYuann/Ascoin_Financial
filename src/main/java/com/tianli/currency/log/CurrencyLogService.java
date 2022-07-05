package com.tianli.currency.log;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.common.CommonFunction;
import com.tianli.currency.CurrencyTokenEnum;
import com.tianli.currency.CurrencyTypeEnum;
import com.tianli.management.agentadmin.dto.RakeRecordDTO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 余额变动记录表 服务实现类
 * </p>
 *
 * @author hd
 * @since 2020-12-04
 */
@Service
public class CurrencyLogService extends ServiceImpl<CurrencyLogMapper, CurrencyLog>{

    public void add(long uid, CurrencyTypeEnum type, CurrencyLogType logType, BigInteger amount, String sn,
                    String des, BigInteger balance, BigInteger freeze, BigInteger remain) {
        add(uid, type, CurrencyTokenEnum.usdt_omni, logType, amount, sn, des, balance, freeze, remain);
    }
    public void add(long uid, CurrencyTypeEnum type, CurrencyTokenEnum token, CurrencyLogType logType, BigInteger amount, String sn,
                    String des, BigInteger balance, BigInteger freeze, BigInteger remain) {
        CurrencyLog currencyLog = CurrencyLog.builder()
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
        currencyLogMapper.insert(currencyLog);
    }

    public BigInteger totalRebateAmountWithInterval(long uid, LocalDateTime startTime, LocalDateTime endTime){
        return baseMapper.selectTotalRebateAmountWithInterval(uid, startTime, endTime);
    }

    public BigInteger totalRebateAmount(long uid){
        return baseMapper.selectTotalRebateAmount(uid);
    }

    public long rakeRecordCount(Long uid, String phone, String bet_id, String startTime, String endTime) {
        return currencyLogMapper.rakeRecordCount(uid, phone, bet_id, startTime, endTime);
    }

    public List<RakeRecordDTO> rakeRecordList(Long uid, String phone, String bet_id, String startTime, String endTime, Integer page, Integer size) {
        return currencyLogMapper.rakeRecordList(uid, phone, bet_id, startTime, endTime, Math.max((page-1)*size,0), size);
    }

    public BigInteger sumMiningAmount(Long uid) {
        return currencyLogMapper.selectSumMiningAmount(uid);
    }

    @Resource
    private CurrencyLogMapper currencyLogMapper;

}
