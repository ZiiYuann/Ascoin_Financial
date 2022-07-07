package com.tianli.account.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.account.entity.AccountSummary;
import com.tianli.common.CommonFunction;
import com.tianli.common.async.AsyncService;
import com.tianli.account.enums.ProductType;
import com.tianli.currency.CurrencyTokenEnum;
import com.tianli.currency.log.CurrencyLogType;
import com.tianli.account.mapper.AccountSummaryMapper;
import com.tianli.exception.ErrorCodeEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 用户余额表 服务实现类
 * </p>
 *
 * @author hd
 * @since 2020-12-04
 */
@Service
public class AccountSummaryService extends ServiceImpl<AccountSummaryMapper, AccountSummary> {

    @Resource
    private AccountSummaryMapper accountMapper;
    @Resource
    private AccountBalanceOperationLogService currencyLogService;
    @Resource
    private AsyncService asyncService;

    /**
     * 扣除可用金额
     *
     * @param uid    用户id
     * @param amount 金额
     * @param sn     订单号
     */
    @Transactional
    public void withdraw(long uid, ProductType type, BigInteger amount, String sn, String des) {
        withdraw(uid, type, CurrencyTokenEnum.usdt_omni, amount, sn, des);
    }

    /**
     * 解冻金额
     *
     * @param uid    用户id
     * @param amount 金额
     * @param sn     订单号
     */
    @Transactional
    public void unfreeze(long uid, ProductType type, BigInteger amount, String sn, String des) {
        unfreeze(uid, type, CurrencyTokenEnum.usdt_omni, amount, sn, des);
    }

    /**
     * 冻结金额
     *
     * @param uid    用户id
     * @param amount 金额
     * @param sn     订单号
     */
    @Transactional
    public void freeze(long uid, ProductType type, BigInteger amount, String sn, String des) {
        freeze(uid, type, CurrencyTokenEnum.usdt_omni, amount, sn, des);
    }

    /**
     * 扣除冻结金额
     *
     * @param uid    用户id
     * @param amount 金额
     * @param sn     订单号
     */
    @Transactional
    public void reduce(long uid, ProductType type, BigInteger amount, String sn, String des) {
        reduce(uid, type, CurrencyTokenEnum.usdt_omni, amount, sn, des);
    }

    /**
     * 增加金额
     *
     * @param uid    用户id
     * @param amount 金额
     * @param sn     订单号
     */
    @Transactional
    public void increase(long uid, ProductType type, BigInteger amount, String sn, String des) {
        increase(uid, type, CurrencyTokenEnum.usdt_omni, amount, sn, des);
    }

    /**
     * 获取用户余额数据并且初始化
     */
    public AccountSummary getAndInit(long uid, ProductType type) {
        AccountSummary accountSummaryBalance = accountMapper.get(uid, type);
        if (accountSummaryBalance == null) {
            accountSummaryBalance = AccountSummary.builder()
                    .id(CommonFunction.generalId())
                    .uid(uid)
                    .type(type)
                    .balance(BigInteger.ZERO)
                    .freeze(BigInteger.ZERO)
                    .remain(BigInteger.ZERO)
                    .build();
            final AccountSummary accountSummaryBalanceFinal = accountSummaryBalance;
            asyncService.async(() -> accountMapper.insert(accountSummaryBalanceFinal));
        }
        return accountSummaryBalance;
    }


    public List<AccountSummary> list(long uid) {
        return accountMapper.list(uid);
    }

    @Transactional
    public void increase(long uid, ProductType type, CurrencyTokenEnum token, BigInteger amount, String sn, String des) {
        getAndInit(uid, type);
        boolean result = false;
        if (Objects.equals(token, CurrencyTokenEnum.usdt_omni)
                || Objects.equals(token, CurrencyTokenEnum.usdt_bep20)
                || Objects.equals(token, CurrencyTokenEnum.usdt_erc20)
                || Objects.equals(token, CurrencyTokenEnum.usdt_trc20)
                || Objects.equals(token, CurrencyTokenEnum.usdc_trc20)
                || Objects.equals(token, CurrencyTokenEnum.usdc_erc20)
                || Objects.equals(token, CurrencyTokenEnum.usdc_bep20)) {
            long num = accountMapper.increase(uid, type, amount);
            AccountSummary accountSummaryBalance = getAndInit(uid, type);
            currencyLogService.save(uid, type, token, CurrencyLogType.increase, amount, sn, des, accountSummaryBalance.getBalance(), accountSummaryBalance.getFreeze(), accountSummaryBalance.getRemain());
            result = num > 0L;
        } else if (Objects.equals(token, CurrencyTokenEnum.BF_bep20)) {
            long num = accountMapper.increaseBF(uid, type, amount);
            AccountSummary accountSummaryBalance = accountMapper.get(uid, type);
            currencyLogService.save(uid, type, CurrencyTokenEnum.BF_bep20, CurrencyLogType.increase, amount, sn, des, accountSummaryBalance.getBalanceBF(), accountSummaryBalance.getFreezeBF(), accountSummaryBalance.getRemainBF());
            result = num > 0L;
        }

        if (!result) {
            ErrorCodeEnum.CREDIT_LACK.throwException();
        }

    }

    @Transactional
    public void reduce(long uid, ProductType type, CurrencyTokenEnum token, BigInteger amount, String sn, String des) {
        getAndInit(uid, type);
        boolean result = false;
        if (Objects.equals(token, CurrencyTokenEnum.usdt_omni)
                || Objects.equals(token, CurrencyTokenEnum.usdt_bep20)
                || Objects.equals(token, CurrencyTokenEnum.usdt_erc20)
                || Objects.equals(token, CurrencyTokenEnum.usdt_trc20)
                || Objects.equals(token, CurrencyTokenEnum.usdc_trc20)
                || Objects.equals(token, CurrencyTokenEnum.usdc_erc20)
                || Objects.equals(token, CurrencyTokenEnum.usdc_bep20)) {
            long num = accountMapper.reduce(uid, type, amount);
            AccountSummary accountSummaryBalance = getAndInit(uid, type);
            currencyLogService.save(uid, type, token, CurrencyLogType.reduce, amount, sn, des, accountSummaryBalance.getBalance(), accountSummaryBalance.getFreeze(), accountSummaryBalance.getRemain());
            result =  num > 0L;
        } else if (Objects.equals(token, CurrencyTokenEnum.BF_bep20)) {
            long num = accountMapper.reduceBF(uid, type, amount);
            AccountSummary accountSummaryBalance = getAndInit(uid, type);
            currencyLogService.save(uid, type, CurrencyTokenEnum.BF_bep20, CurrencyLogType.reduce, amount, sn, des, accountSummaryBalance.getBalanceBF(), accountSummaryBalance.getFreezeBF(), accountSummaryBalance.getRemainBF());
            result=  num > 0L;
        }
        if( !result ){
            ErrorCodeEnum.CREDIT_LACK.throwException();
        }
    }

    @Transactional
    public void withdraw(long uid, ProductType type, CurrencyTokenEnum token, BigInteger amount, String sn, String des) {
        getAndInit(uid, type);
        boolean result = false;
        if (Objects.equals(token, CurrencyTokenEnum.usdt_omni)
                || Objects.equals(token, CurrencyTokenEnum.usdt_bep20)
                || Objects.equals(token, CurrencyTokenEnum.usdt_erc20)
                || Objects.equals(token, CurrencyTokenEnum.usdt_trc20)
                || Objects.equals(token, CurrencyTokenEnum.usdc_trc20)
                || Objects.equals(token, CurrencyTokenEnum.usdc_erc20)
                || Objects.equals(token, CurrencyTokenEnum.usdc_bep20)) {
            long num = accountMapper.withdraw(uid, type, amount);
            AccountSummary accountSummaryBalance = getAndInit(uid, type);
            currencyLogService.save(uid, type, token, CurrencyLogType.withdraw, amount, sn, des, accountSummaryBalance.getBalance(), accountSummaryBalance.getFreeze(), accountSummaryBalance.getRemain());
            result =  num > 0L;
        } else if (Objects.equals(token, CurrencyTokenEnum.BF_bep20)) {
            long num = accountMapper.withdrawBF(uid, type, amount);
            AccountSummary accountSummaryBalance = getAndInit(uid, type);
            currencyLogService.save(uid, type, CurrencyTokenEnum.BF_bep20, CurrencyLogType.withdraw, amount, sn, des, accountSummaryBalance.getBalanceBF(), accountSummaryBalance.getFreezeBF(), accountSummaryBalance.getRemainBF());
            result =  num > 0L;
        }
        if(!result){
            ErrorCodeEnum.CREDIT_LACK.throwException();
        }
    }

    @Transactional
    public void freeze(long uid, ProductType type, CurrencyTokenEnum token, BigInteger amount, String sn, String des) {
        getAndInit(uid, type);
        boolean result = false;
        if (Objects.equals(token, CurrencyTokenEnum.usdt_omni)
                || Objects.equals(token, CurrencyTokenEnum.usdt_bep20)
                || Objects.equals(token, CurrencyTokenEnum.usdt_erc20)
                || Objects.equals(token, CurrencyTokenEnum.usdt_trc20)
                || Objects.equals(token, CurrencyTokenEnum.usdc_trc20)
                || Objects.equals(token, CurrencyTokenEnum.usdc_erc20)
                || Objects.equals(token, CurrencyTokenEnum.usdc_bep20)) {
            long num = accountMapper.freeze(uid, type, amount);
            AccountSummary accountSummaryBalance = accountMapper.get(uid, type);
            currencyLogService.save(uid, type, token, CurrencyLogType.freeze, amount, sn, des, accountSummaryBalance.getBalance(), accountSummaryBalance.getFreeze(), accountSummaryBalance.getRemain());
            result = num > 0L;
        } else if (Objects.equals(token, CurrencyTokenEnum.BF_bep20)) {
            long num = accountMapper.freezeBF(uid, type, amount);
            AccountSummary accountSummaryBalance = accountMapper.get(uid, type);
            currencyLogService.save(uid, type, CurrencyTokenEnum.BF_bep20, CurrencyLogType.freeze, amount, sn, des, accountSummaryBalance.getBalanceBF(), accountSummaryBalance.getFreezeBF(), accountSummaryBalance.getRemainBF());
            result = num > 0L;
        }
        if(!result){
            ErrorCodeEnum.CREDIT_LACK.throwException();
        }
    }

    @Transactional
    public void unfreeze(long uid, ProductType type, CurrencyTokenEnum token, BigInteger amount, String sn, String des) {
        getAndInit(uid, type);
        boolean result = false;
        if (Objects.equals(token, CurrencyTokenEnum.usdt_omni)
                || Objects.equals(token, CurrencyTokenEnum.usdt_bep20)
                || Objects.equals(token, CurrencyTokenEnum.usdt_erc20)
                || Objects.equals(token, CurrencyTokenEnum.usdt_trc20)
                || Objects.equals(token, CurrencyTokenEnum.usdc_trc20)
                || Objects.equals(token, CurrencyTokenEnum.usdc_erc20)
                || Objects.equals(token, CurrencyTokenEnum.usdc_bep20)) {
            long num = accountMapper.unfreeze(uid, type, amount);
            AccountSummary accountSummaryBalance = accountMapper.get(uid, type);
            currencyLogService.save(uid, type, token, CurrencyLogType.unfreeze, amount, sn, des, accountSummaryBalance.getBalance(), accountSummaryBalance.getFreeze(), accountSummaryBalance.getRemain());
            result = num > 0L;
        } else if (Objects.equals(token, CurrencyTokenEnum.BF_bep20)) {
            long num = accountMapper.unfreezeBF(uid, type, amount);
            AccountSummary accountSummaryBalance = accountMapper.get(uid, type);
            currencyLogService.save(uid, type, CurrencyTokenEnum.BF_bep20, CurrencyLogType.unfreeze, amount, sn, des, accountSummaryBalance.getBalanceBF(), accountSummaryBalance.getFreezeBF(), accountSummaryBalance.getRemainBF());
            result =  num > 0L;
        }

        if(!result){
            ErrorCodeEnum.CREDIT_LACK.throwException();
        }
    }

}
