package com.tianli.loan.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.common.CommonFunction;
import com.tianli.currency.CurrencyService;
import com.tianli.currency.CurrencyTokenEnum;
import com.tianli.currency.CurrencyTypeEnum;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.currency.log.CurrencyLogService;
import com.tianli.currency.log.CurrencyLogType;
import com.tianli.currency.mapper.Currency;
import com.tianli.currency_token.mapper.CurrencyCoinEnum;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.loan.dao.LoanCurrencyMapper;
import com.tianli.loan.entity.Loan;
import com.tianli.loan.entity.LoanCurrency;
import com.tianli.loan.entity.LoanCurrencyLog;
import com.tianli.loan.service.ILoanCurrencyLogService;
import com.tianli.loan.service.ILoanCurrencyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 用户贷款余额表 服务实现类
 * </p>
 *
 * @author lzy
 * @since 2022-05-26
 */
@Service
public class LoanCurrencyServiceImpl extends ServiceImpl<LoanCurrencyMapper, LoanCurrency> implements ILoanCurrencyService {

    @Resource
    LoanCurrencyMapper loanCurrencyMapper;

    @Resource
    ILoanCurrencyLogService loanCurrencyLogService;

    @Resource
    CurrencyLogService currencyLogService;

    @Resource
    CurrencyService currencyService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void increase(Loan loan) {
        BigDecimal actualAmount = loan.getActual_amount();
        if (ObjectUtil.isNull(actualAmount) || actualAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw ErrorCodeEnum.ARGUEMENT_ERROR.generalException();
        }
        LoanCurrency loanCurrency = increase(loan.getUid(), loan.getId(), CurrencyCoinEnum.getCurrencyCoinEnum(loan.getToken()), actualAmount, "");
        Currency currency = currencyService.get(loan.getUid(), CurrencyTypeEnum.normal);
        BigInteger balance = currency.getBalance().add(TokenCurrencyType.usdt_omni.amount(loanCurrency.getBalance()));
        BigInteger remain = currency.getRemain().add(TokenCurrencyType.usdt_omni.amount(loanCurrency.getBalance()));
        currencyLogService.add(loan.getUid(), CurrencyTypeEnum.loan, CurrencyTokenEnum.usdt_omni,
                CurrencyLogType.increase, TokenCurrencyType.usdt_omni.amount(loan.getActual_amount()),
                loan.getId().toString(), CurrencyLogDes.借款.name(), balance,
                currency.getFreeze(), remain);
    }

    private LoanCurrency increase(Long uid, Long id, CurrencyCoinEnum token, BigDecimal amount, String dsc) {
        this.findByUid(uid, token);
        int result = loanCurrencyMapper.increase(amount, uid, token.name());
        if (result <= 0) {
            throw ErrorCodeEnum.SYSTEM_BUSY.generalException();
        }
        LoanCurrency loanCurrency = this.findByUid(uid, token);
        loanCurrencyLogService.save(LoanCurrencyLog.builder()
                .id(CommonFunction.generalId())
                .uid(loanCurrency.getUid())
                .amount(amount)
                .to_balance(loanCurrency.getBalance())
                .token(loanCurrency.getToken())
                .create_time(LocalDateTime.now())
                .create_time_ms(System.currentTimeMillis())
                .relate_id(id)
                .type(CurrencyLogType.increase.name())
                .node(dsc)
                .build());
        return loanCurrency;
    }

    @Override
    public void increase(Long uid, BigInteger loan_amount, CurrencyCoinEnum token, Long id, String des) {
        this.findByUid(uid, token);
        increase(uid, id, token, TokenCurrencyType.usdt_omni._money(loan_amount), des);
    }

    @Override
    public BigInteger betIncrease(Long uid, Long id, CurrencyCoinEnum token, BigInteger returnAmount, BigInteger subtract, String dsc) {
        if (subtract.compareTo(BigInteger.ZERO) <= 0) {
            return BigInteger.ZERO;
        }
        BigInteger returnLoanAmount;
        if (subtract.compareTo(returnAmount) >= 0) {
            returnLoanAmount = returnAmount;
        } else {
            returnLoanAmount = subtract;
        }
        this.increase(uid, id, token, TokenCurrencyType.usdt_omni._money(returnLoanAmount), dsc);
        return returnLoanAmount;
    }

    @Override
    public List<LoanCurrency> findByUids(List<Long> userIds, CurrencyCoinEnum currencyCoinEnum) {
        return this.list(Wrappers.lambdaQuery(LoanCurrency.class)
                .in(LoanCurrency::getUid, userIds)
                .eq(LoanCurrency::getToken, currencyCoinEnum));
    }

    @Override
    public void reduce(Long uid, BigInteger amount, CurrencyCoinEnum token, long generalId, String dsc) {
        this.findByUid(uid, token);
        int result = loanCurrencyMapper.reduce(uid, TokenCurrencyType.usdt_omni._money(amount), token.name());
        if (result <= 0) {
            throw ErrorCodeEnum.SYSTEM_BUSY.generalException();
        }
        LoanCurrency loanCurrency = this.findByUid(uid, token);
        loanCurrencyLogService.save(LoanCurrencyLog.builder()
                .id(CommonFunction.generalId())
                .uid(loanCurrency.getUid())
                .amount(TokenCurrencyType.usdt_omni._money(amount))
                .to_balance(loanCurrency.getBalance())
                .token(loanCurrency.getToken())
                .create_time(LocalDateTime.now())
                .create_time_ms(System.currentTimeMillis())
                .relate_id(generalId)
                .type(CurrencyLogType.reduce.name())
                .node(dsc)
                .build());
    }

    @Override
    public BigInteger betAmountReduce(Long uid, BigInteger amount, CurrencyCoinEnum token, long generalId, String des) {
        LoanCurrency loanCurrency = this.findByUid(uid, token);
        BigInteger balance = TokenCurrencyType.usdt_omni.amount(loanCurrency.getBalance());
        BigInteger useBalance;
        if (balance.compareTo(BigInteger.ZERO) <= 0) {
            return BigInteger.ZERO;
        }
        if (balance.compareTo(amount) >= 0) {
            useBalance = amount;
        } else {
            useBalance = balance;
        }
        this.reduce(uid, useBalance, token, generalId, des);
        return useBalance;
    }

    @Override
    public LoanCurrency findByUid(Long uid, CurrencyCoinEnum currencyCoinEnum) {
        LoanCurrency loanCurrency = this.getOne(Wrappers.lambdaQuery(LoanCurrency.class)
                .eq(LoanCurrency::getUid, uid)
                .eq(LoanCurrency::getToken, currencyCoinEnum));
        if (ObjectUtil.isNull(loanCurrency)) {
            loanCurrency = LoanCurrency.builder()
                    .id(CommonFunction.generalId())
                    .uid(uid).balance(BigDecimal.ZERO)
                    .token(currencyCoinEnum.getName())
                    .create_time(LocalDateTime.now())
                    .create_time_ms(System.currentTimeMillis())
                    .build();
            this.save(loanCurrency);
        }
        return loanCurrency;
    }


}
