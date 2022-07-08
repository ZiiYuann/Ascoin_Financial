package com.tianli.currency;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.common.CommonFunction;
import com.tianli.common.blockchain.CurrencyCoinEnum;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.currency.log.CurrencyLogService;
import com.tianli.currency.log.CurrencyLogType;
import com.tianli.currency.mapper.Currency;
import com.tianli.currency.mapper.CurrencyMapper;
import com.tianli.currency.mapper.CurrencyToken;
import com.tianli.currency.mapper.CurrencyTokenMapper;
import com.tianli.exception.ErrorCodeEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

@Service
public class CurrencyTokenService extends ServiceImpl<CurrencyTokenMapper, CurrencyToken> {

    @Transactional
    public void increase(long uid, CurrencyTypeEnum type, CurrencyCoinEnum token, BigDecimal amount, String sn, CurrencyLogDes des) {
        CurrencyToken start = this.get(uid, type, token);
        long result = currencyTokenMapper.increase(uid, type, token, amount);
        CurrencyToken end = this.get(uid, type, token);
        currencyTokenLogService.add(uid, type, token, CurrencyLogType.increase, amount, sn, des, end.getBalance(), end.getFreeze(), end.getBalance());
        if (result <= 0L) {
            throw ErrorCodeEnum.INSUFFICIENT_BALANCE.generalException();
        }
    }

    @Transactional
    public void decrease(long uid, CurrencyTypeEnum type, CurrencyCoinEnum token, BigDecimal amount, String sn, CurrencyLogDes des) {
        CurrencyToken start = this.get(uid, type, token);
        long result = currencyTokenMapper.decrease(uid, type, token, amount);
        CurrencyToken end = this.get(uid, type, token);
        currencyTokenLogService.add(uid, type, token, CurrencyLogType.decrease, amount, sn, des, end.getBalance(), end.getFreeze(), end.getBalance());
        if (result <= 0L) {
            throw ErrorCodeEnum.INSUFFICIENT_BALANCE.generalException();
        }
    }

    @Transactional
    public void freeze(long uid, CurrencyTypeEnum type, CurrencyCoinEnum token, BigDecimal amount, String sn, CurrencyLogDes des) {
        CurrencyToken start = this.get(uid, type, token);
        long result = currencyTokenMapper.freeze(uid, type, token, amount);
        CurrencyToken end = this.get(uid, type, token);
        currencyTokenLogService.add(uid, type, token, CurrencyLogType.freeze, amount, sn, des, end.getBalance(), end.getFreeze(), end.getBalance());
        if (result <= 0L) {
            throw ErrorCodeEnum.INSUFFICIENT_BALANCE.generalException();
        }
    }

    @Transactional
    public void reduce(long uid, CurrencyTypeEnum type, CurrencyCoinEnum token, BigDecimal amount, String sn, CurrencyLogDes des) {
        CurrencyToken start = this.get(uid, type, token);
        long result = currencyTokenMapper.reduce(uid, type, token, amount);
        CurrencyToken end = this.get(uid, type, token);
        currencyTokenLogService.add(uid, type, token, CurrencyLogType.withdraw, amount, sn, des, end.getBalance(), end.getFreeze(), end.getBalance());
        if (result <= 0L) {
            throw ErrorCodeEnum.INSUFFICIENT_BALANCE.generalException();
        }
    }

    @Transactional
    public void unfreeze(long uid, CurrencyTypeEnum type, CurrencyCoinEnum token, BigDecimal amount, String sn, CurrencyLogDes des) {
        CurrencyToken start = this.get(uid, type, token);
        long result = currencyTokenMapper.unfreeze(uid, type, token, amount);
        CurrencyToken end = this.get(uid, type, token);
        currencyTokenLogService.add(uid, type, token, CurrencyLogType.unfreeze, amount, sn, des, end.getBalance(), end.getFreeze(), end.getBalance());
        if (result <= 0L) {
            throw ErrorCodeEnum.INSUFFICIENT_BALANCE.generalException();
        }
    }

    @Transactional
    public void transfer(long uid, CurrencyTypeEnum from, CurrencyTypeEnum to, BigDecimal amount, String sn) {
        long result_from = -1L;
        long result_to = -1L;
        CurrencyToken start = this.get(uid, CurrencyTypeEnum.actual, CurrencyCoinEnum.usdt);
        if (from.equals(CurrencyTypeEnum.actual)) {
            result_from = currencyTokenMapper.decrease(uid, from, CurrencyCoinEnum.usdt, amount);
            result_to = currencyMapper.increase(uid, to, CurrencyTokenEnum.usdt_omni.fromMoney(amount));
            CurrencyToken currency_from = currencyTokenMapper.get(uid, from, CurrencyCoinEnum.usdt);
            Currency currency_to = currencyMapper.get(uid, to);
            currencyTokenLogService.add(uid, from, CurrencyCoinEnum.usdt, CurrencyLogType.reduce, amount, sn, CurrencyLogDes.划出, currency_from.getBalance(), currency_from.getFreeze(), currency_from.getRemain());
            currencyLogService.add(uid, to, CurrencyLogType.increase, CurrencyTokenEnum.usdt_omni.fromMoney(amount), sn, CurrencyLogDes.划入.name(), currency_to.getBalance(), currency_to.getFreeze(), currency_to.getRemain());
        } else if (to.equals(CurrencyTypeEnum.actual)) {
            result_to = currencyTokenMapper.increase(uid, to, CurrencyCoinEnum.usdt, amount);
            result_from = currencyMapper.decrease(uid, from, CurrencyTokenEnum.usdt_omni.fromMoney(amount));
            CurrencyToken currency_to = currencyTokenMapper.get(uid, to, CurrencyCoinEnum.usdt);
            Currency currency_from = currencyMapper.get(uid, from);
            currencyTokenLogService.add(uid, to, CurrencyCoinEnum.usdt, CurrencyLogType.increase, amount, sn, CurrencyLogDes.划入, currency_to.getBalance(), currency_to.getFreeze(), currency_to.getRemain());
            currencyLogService.add(uid, from, CurrencyLogType.reduce, CurrencyTokenEnum.usdt_omni.fromMoney(amount), sn, CurrencyLogDes.划出.name(), currency_from.getBalance(), currency_from.getFreeze(), currency_from.getRemain());
        }
        if (result_from <= 0L || result_to <= 0L) {
            throw ErrorCodeEnum.INSUFFICIENT_BALANCE.generalException();
        }
    }

    public CurrencyToken get(long uid, CurrencyTypeEnum type, CurrencyCoinEnum token) {
        CurrencyToken currencyToken = _get(uid, type, token);
        if (currencyToken == null) {
            currencyToken = CurrencyToken.builder()
                    .id(CommonFunction.generalId())
                    .uid(uid)
                    .type(type)
                    .token(token)
                    .balance(BigDecimal.ZERO)
                    .freeze(BigDecimal.ZERO)
                    .remain(BigDecimal.ZERO)
                    .build();
            currencyTokenMapper.insert(currencyToken);
        }
        return currencyToken;
    }

    public CurrencyToken _get(long uid, CurrencyTypeEnum type, CurrencyCoinEnum token) {
        return currencyTokenMapper.get(uid, type, token);
    }
    /**
     * 查询用户所有可用的u数量包括现货和期货的usdt以及usdc
     *
     * @param uid
     * @return
     */
    public BigDecimal getCurrencyAllUsdt(Long uid) {
        BigDecimal amount = BigDecimal.ZERO;
        //查询现货
        List<CurrencyToken> currencyTokenList = this.list(Wrappers.lambdaQuery(CurrencyToken.class)
                .eq(CurrencyToken::getUid, uid)
                .in(CurrencyToken::getToken, ListUtil.of(CurrencyCoinEnum.usdc, CurrencyCoinEnum.usdt))
                .eq(CurrencyToken::getType, CurrencyTypeEnum.actual));
        if (CollUtil.isNotEmpty(currencyTokenList)) {
            for (CurrencyToken currencyToken : currencyTokenList) {
                amount = amount.add(currencyToken.getBalance());
            }
        }
        //查询合约账户和理财账户
        List<Currency> currencyList = currencyService.list(Wrappers.lambdaQuery(Currency.class)
                .eq(Currency::getUid, uid)
                .in(Currency::getType, ListUtil.of(CurrencyTypeEnum.normal, CurrencyTypeEnum.financial)));
        if (CollUtil.isNotEmpty(currencyList)) {
            for (Currency currency : currencyList) {
                amount = amount.add(TokenCurrencyType.usdt_omni._money(currency.getBalance()));
            }
        }
        return amount;
    }

    @Resource
    private CurrencyTokenMapper currencyTokenMapper;
    @Resource
    private CurrencyMapper currencyMapper;
    @Resource
    private CurrencyLogService currencyLogService;
    @Resource
    private CurrencyTokenLogService currencyTokenLogService;

    @Resource
    CurrencyService currencyService;

    public List<CurrencyToken> getUByUserIds(List<Long> userIds) {
        return this.list(Wrappers.lambdaQuery(CurrencyToken.class)
                .in(CurrencyToken::getUid, userIds)
                .in(CurrencyToken::getToken, ListUtil.of(CurrencyCoinEnum.usdt, CurrencyCoinEnum.usdc)));
    }
}
