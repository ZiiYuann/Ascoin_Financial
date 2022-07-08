package com.tianli.currency;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.common.CommonFunction;
import com.tianli.common.ConfigConstants;
import com.tianli.currency.log.CurrencyLogType;
import com.tianli.currency.mapper.DiscountCurrency;
import com.tianli.currency.mapper.DiscountCurrencyLog;
import com.tianli.currency.mapper.DiscountCurrencyLogMapper;
import com.tianli.currency.mapper.DiscountCurrencyMapper;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.mconfig.ConfigService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class DiscountCurrencyService extends ServiceImpl<DiscountCurrencyMapper, DiscountCurrency> {

    @Resource
    private ConfigService configService;

    @Resource
    private DiscountCurrencyMapper discountCurrencyMapper;

    @Resource
    private DiscountCurrencyLogMapper discountCurrencyLogMapper;

    public DiscountCurrencyLog receiveNewGift(long id) {
        boolean check = checkNewReceive(id);
        if (check) {
            ErrorCodeEnum.TOO_FREQUENT.throwException();
        }
        String discount_amount = configService.getOrDefaultNoCache(ConfigConstants.NEW_GIFT_AMOUNT, "50");
        BigInteger balance = TokenCurrencyType.usdt_omni.amount(discount_amount);
        int insert = discountCurrencyMapper.increaseNew(id, balance);
        if (insert <= 0) ErrorCodeEnum.TIME_CONFLICT.throwException();
        DiscountCurrency discountCurrency = discountCurrencyMapper.selectById(id);
        DiscountCurrencyLog currencyLog = DiscountCurrencyLog.builder()
                .id(CommonFunction.generalId())
                .create_time(LocalDateTime.now())
                .uid(id)
                .token(CurrencyTokenEnum.usdt_omni)
                .amount(balance)
                .to_balance(discountCurrency.getBalance())
                .node("领取新人礼包")
                .build();
        discountCurrencyLogMapper.insert(currencyLog);
        return currencyLog;
    }

    public void reduce(long uid, BigInteger amount) {
        reduce(uid, amount, CurrencyTokenEnum.usdt_omni, -1, "减少优惠金");

    }

    public DiscountCurrency findById(Long uid) {
        DiscountCurrency discountCurrency = this.getById(uid);
        if (ObjectUtil.isNull(discountCurrency)) {
            discountCurrency = DiscountCurrency.builder().id(uid)
                    .balance(BigInteger.ZERO)
                    .token(CurrencyTokenEnum.usdt_omni)
                    .new_gift(Boolean.FALSE)
                    .kyc_certification(Boolean.FALSE)
                    .build();
            super.save(discountCurrency);
        }
        return discountCurrency;
    }

    public void reduce(long uid, BigInteger amount, CurrencyTokenEnum token, long relate_id, String desc) {
        ensureExist(uid);
        int result = discountCurrencyMapper.withdraw(uid, amount);
        if (result <= 0) ErrorCodeEnum.CREDIT_LACK.throwException();
        DiscountCurrency discountCurrency = discountCurrencyMapper.selectById(uid);
        discountCurrencyLogMapper.insert(DiscountCurrencyLog.builder()
                .id(CommonFunction.generalId())
                .create_time(LocalDateTime.now())
                .uid(uid)
                .type(CurrencyLogType.reduce)
                .token(token)
                .amount(amount)
                .to_balance(discountCurrency.getBalance())
                .relate_id(relate_id)
                .node(desc)
                .build());
    }

    public void increase(Long uid, BigInteger discountAmount) {
        increase(uid, discountAmount, CurrencyTokenEnum.usdt_omni, -1, "增加优惠金");
    }

    public void KYC(Long uid, BigInteger discountAmount) {
        increase(uid, discountAmount, CurrencyTokenEnum.usdt_omni, -1, "KYC认证");
    }

    public void KYCCertification(Long uid) {
        DiscountCurrency discountCurrency = DiscountCurrency.builder().id(uid).kyc_certification(Boolean.TRUE).build();
        this.updateById(discountCurrency);

    }

    public void increase(Long uid, BigInteger discountAmount, CurrencyTokenEnum token, long relate_id, String desc) {
        ensureExist(uid);
        int result = discountCurrencyMapper.increase(uid, discountAmount);
        if (result <= 0) ErrorCodeEnum.CREDIT_LACK.throwException();
        DiscountCurrency discountCurrency = discountCurrencyMapper.selectById(uid);
        discountCurrencyLogMapper.insert(DiscountCurrencyLog.builder()
                .id(CommonFunction.generalId())
                .create_time(LocalDateTime.now())
                .uid(uid)
                .type(CurrencyLogType.increase)
                .token(token)
                .amount(discountAmount)
                .to_balance(discountCurrency.getBalance())
                .relate_id(relate_id)
                .node(desc)
                .build());
    }

    private void ensureExist(Long uid) {
        DiscountCurrency byId = super.getById(uid);
        if (Objects.isNull(byId)) {
            super.save(DiscountCurrency.builder().id(uid)
                    .balance(BigInteger.ZERO)
                    .token(CurrencyTokenEnum.usdt_omni)
                    .new_gift(Boolean.FALSE)
                    .kyc_certification(Boolean.FALSE)
                    .build());
        }
    }

    public boolean checkNewReceive(Long uid) {
        ensureExist(uid);
        DiscountCurrency discountCurrency = super.getById(uid);
        return discountCurrency.getNew_gift();
    }

    public BigInteger betAmountReduce(Long uid, BigInteger amount, CurrencyTokenEnum usdt_omni, long generalId, String des) {
        DiscountCurrency discountCurrency = this.findById(uid);
        BigInteger balance = discountCurrency.getBalance();
        if (balance.compareTo(BigInteger.ZERO) <= 0) {
            return BigInteger.ZERO;
        }
        BigInteger useBalance;
        if (balance.compareTo(amount) >= 0) {
            useBalance = amount;
        } else {
            useBalance = balance;
        }
        this.reduce(uid, useBalance, usdt_omni, generalId, des);
        return useBalance;
    }

    public BigInteger betIncrease(Long uid, Long id, CurrencyTokenEnum usdt_omni, BigInteger returnAmount, BigInteger discountAmount, String name) {
        if (discountAmount.compareTo(BigInteger.ZERO) <= 0) {
            return BigInteger.ZERO;
        }
        BigInteger returnDiscountAmount;
        if (discountAmount.compareTo(returnAmount) >= 0) {
            returnDiscountAmount = returnAmount;
        } else {
            returnDiscountAmount = discountAmount;
        }
        this.increase(uid, returnDiscountAmount, usdt_omni, id, name);
        return returnDiscountAmount;
    }
}
