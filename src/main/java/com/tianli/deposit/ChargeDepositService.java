package com.tianli.deposit;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.currency.enums.TokenAdapter;
import com.tianli.deposit.mapper.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * <p>
 * 充值提现表  保证金 服务实现类
 * </p>
 *
 * @author hd
 * @since 2020-12-19
 */
@Service
public class ChargeDepositService extends ServiceImpl<ChargeDepositMapper, ChargeDeposit> {

    public Map<String, BigDecimal> getSumAmount(String nike, String phone, String txid, ChargeDepositStatus status, String type, String startTime, String endTime, ChargeDepositType chargeDepositType, TokenAdapter tokenAdapter) {
        return baseMapper.selectSumAmount(nike, phone, txid, status, type, startTime, endTime, chargeDepositType, tokenAdapter);
    }
    public Map<String, BigDecimal> getSumAmount(String nike, String phone, ChargeDepositStatus status, String type, String startTime, String endTime, ChargeDepositType chargeDepositType, TokenAdapter tokenAdapter) {
        return getSumAmount(nike, phone, null, status, type, startTime, endTime, chargeDepositType, tokenAdapter);
    }

    public boolean success(LocalDateTime now, ChargeDepositStatus transaction_success, String txid, Long id, ChargeDepositStatus transacting) {
        return baseMapper.updateSuccess(now, transaction_success, txid, id, transacting) > 0;
    }

    public boolean fail(LocalDateTime now, ChargeDepositStatus transaction_success, Long id, ChargeDepositStatus transacting) {
        return baseMapper.updateFail(now, transaction_success, id, transacting) > 0;
    }
}
