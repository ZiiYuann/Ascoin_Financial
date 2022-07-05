package com.tianli.dividends.settlement;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.dividends.settlement.mapper.ChargeSettlement;
import com.tianli.dividends.settlement.mapper.ChargeSettlementMapper;
import com.tianli.dividends.settlement.mapper.ChargeSettlementStatus;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * <p>
 * 充值提现表 服务实现类
 * </p>
 *
 * @author hd
 * @since 2020-12-11
 */
@Service
public class ChargeSettlementService extends ServiceImpl<ChargeSettlementMapper, ChargeSettlement> {

    public boolean success(LocalDateTime now, ChargeSettlementStatus transaction_success, Long id, ChargeSettlementStatus transacting, BigInteger miner_fee, TokenCurrencyType miner_fee_type) {
        return baseMapper.updateSuccess(now, transaction_success, id, transacting, miner_fee, miner_fee_type) > 0;
    }

    public boolean fail(LocalDateTime now, ChargeSettlementStatus transaction_success, Long id, ChargeSettlementStatus transacting, BigInteger miner_fee, TokenCurrencyType miner_fee_type) {
        return baseMapper.updateFail(now, transaction_success, id, transacting, miner_fee, miner_fee_type) > 0;
    }
}
