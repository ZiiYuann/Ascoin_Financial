package com.tianli.account.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.account.convert.AccountConverter;
import com.tianli.account.entity.AccountBalance;
import com.tianli.account.enums.AccountOperationType;
import com.tianli.account.enums.ProductType;
import com.tianli.account.mapper.AccountBalanceMapper;
import com.tianli.account.vo.AccountBalanceMainPageVO;
import com.tianli.account.vo.AccountBalanceVO;
import com.tianli.address.AddressService;
import com.tianli.common.CommonFunction;
import com.tianli.common.async.AsyncService;
import com.tianli.currency.enums.CurrencyAdaptType;
import com.tianli.currency.service.CurrencyService;
import com.tianli.exception.ErrorCodeEnum;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户余额表 服务实现类
 * </p>
 *
 * @author hd
 * @since 2020-12-04
 */
@Service
public class AccountBalanceService extends ServiceImpl<AccountBalanceMapper, AccountBalance> {

    @Resource
    private AccountBalanceMapper accountBalanceMapper;
    @Resource
    private AccountBalanceOperationLogService accountBalanceOperationLogService;
    @Resource
    private AsyncService asyncService;
    @Resource
    private AccountConverter accountConverter;
    @Resource
    private CurrencyService currencyService;
    @Resource
    private AddressService addressService;

    /**
     * 扣除可用金额
     *
     * @param uid    用户id
     * @param amount 金额
     * @param sn     订单号
     */
    @Transactional
    public void withdraw(long uid, ProductType type, BigDecimal amount, String sn, String des) {
        withdraw(uid, type, CurrencyAdaptType.usdt_omni, amount, sn, des);
    }

    /**
     * 解冻金额
     *
     * @param uid    用户id
     * @param amount 金额
     * @param sn     订单号
     */
    @Transactional
    public void unfreeze(long uid, ProductType type, BigDecimal amount, String sn, String des) {
        unfreeze(uid, type, CurrencyAdaptType.usdt_omni, amount, sn, des);
    }

    /**
     * 冻结金额
     *
     * @param uid    用户id
     * @param amount 金额
     * @param sn     订单号
     */
    @Transactional
    public void freeze(long uid, ProductType type, BigDecimal amount, String sn, String des) {
        freeze(uid, type, CurrencyAdaptType.usdt_omni, amount, sn, des);
    }

    /**
     * 扣除冻结金额
     *
     * @param uid    用户id
     * @param amount 金额
     * @param sn     订单号
     */
    @Transactional
    public void reduce(long uid, ProductType type, BigDecimal amount, String sn, String des) {
        reduce(uid, type, CurrencyAdaptType.usdt_omni, amount, sn, des);
    }

    /**
     * 增加金额
     *
     * @param uid    用户id
     * @param amount 金额
     * @param sn     订单号
     */
    @Transactional
    public void increase(long uid, ProductType type, BigDecimal amount, String sn, String des) {
        increase(uid, type, CurrencyAdaptType.usdt_omni, amount, sn, des);
    }

    @Transactional
    public void increase(long uid, ProductType type, CurrencyAdaptType token, BigDecimal amount, String sn, String des) {
        getAndInit(uid, token);

        if (accountBalanceMapper.increase(uid, amount) <= 0L) {
            ErrorCodeEnum.CREDIT_LACK.throwException();
        }
        AccountBalance accountBalance = getAndInit(uid, token);
        accountBalanceOperationLogService.save(accountBalance, AccountOperationType.increase, amount, sn, des);
    }

    @Transactional
    public void reduce(long uid, ProductType type, CurrencyAdaptType token, BigDecimal amount, String sn, String des) {
        getAndInit(uid, token);

        if (accountBalanceMapper.reduce(uid, amount) <= 0L) {
            ErrorCodeEnum.CREDIT_LACK.throwException();
        }
        AccountBalance accountBalance = getAndInit(uid, token);
        accountBalanceOperationLogService.save(accountBalance, AccountOperationType.reduce, amount, sn, des);
    }

    @Transactional
    public void withdraw(long uid, ProductType type, CurrencyAdaptType token, BigDecimal amount, String sn, String des) {
        getAndInit(uid, token);

        if (accountBalanceMapper.withdraw(uid, amount) <= 0L) {
            ErrorCodeEnum.CREDIT_LACK.throwException();
        }
        AccountBalance accountBalance = getAndInit(uid, token);
        accountBalanceOperationLogService.save(accountBalance, AccountOperationType.withdraw, amount, sn, des);
    }

    @Transactional
    public void freeze(long uid, ProductType type, CurrencyAdaptType token, BigDecimal amount, String sn, String des) {
        getAndInit(uid, token);

        if (accountBalanceMapper.freeze(uid, amount) <= 0L) {
            ErrorCodeEnum.CREDIT_LACK.throwException();
        }
        AccountBalance accountBalance = accountBalanceMapper.get(uid, token);
        accountBalanceOperationLogService.save(accountBalance, AccountOperationType.freeze, amount, sn, des);

    }

    @Transactional
    public void unfreeze(long uid, ProductType type, CurrencyAdaptType token, BigDecimal amount, String sn, String des) {
        getAndInit(uid, token);

        if (accountBalanceMapper.unfreeze(uid, amount) <= 0L) {
            ErrorCodeEnum.CREDIT_LACK.throwException();
        }

        AccountBalance accountBalance = accountBalanceMapper.get(uid, token);
        accountBalanceOperationLogService.save(accountBalance, AccountOperationType.unfreeze, amount, sn, des);
    }

    /**
     * 获取用户余额数据并且初始化
     */
    public AccountBalance getAndInit(long uid, CurrencyAdaptType token) {
        validCurrencyToken(token);
        String address = addressService.getAddressByCurrencyAdaptType(uid, token);
        AccountBalance accountBalanceBalance = accountBalanceMapper.get(uid, token);
        if (accountBalanceBalance == null) {
            accountBalanceBalance = AccountBalance.builder()
                    .id(CommonFunction.generalId())
                    .uid(uid)
                    .address(address)
                    .currencyAdaptType(token)
                    .balance(BigDecimal.ZERO)
                    .freeze(BigDecimal.ZERO)
                    .remain(BigDecimal.ZERO)
                    .build();
            final AccountBalance accountBalanceBalanceFinal = accountBalanceBalance;
            asyncService.async(() -> accountBalanceMapper.insert(accountBalanceBalanceFinal));
        }
        return accountBalanceBalance;
    }


    public List<AccountBalance> list(long uid) {
        return accountBalanceMapper.list(uid);
    }

    /**
     * 校验币别是否有效 暂时只支持 usdt、usdc
     *
     * @param token 币别类型
     * @since 2022.07.08
     */
    private void validCurrencyToken(CurrencyAdaptType token) {
        if (Objects.equals(token, CurrencyAdaptType.usdt_omni)
                || Objects.equals(token, CurrencyAdaptType.usdt_bep20)
                || Objects.equals(token, CurrencyAdaptType.usdt_erc20)
                || Objects.equals(token, CurrencyAdaptType.usdt_trc20)
                || Objects.equals(token, CurrencyAdaptType.usdc_trc20)
                || Objects.equals(token, CurrencyAdaptType.usdc_erc20)
                || Objects.equals(token, CurrencyAdaptType.usdc_bep20)) {
            return;
        }
        ErrorCodeEnum.CURRENCY_NOT_SUPPORT.throwException();
    }

    /**
     * 获取余额主页面信息
     *
     * @param uid 用户id
     * @return 账户余额主页面VO
     */
    public AccountBalanceMainPageVO getAccountBalanceMainPageVO(Long uid) {

        List<AccountBalance> accountBalances = Optional.ofNullable(this.list(uid)).orElse(new ArrayList<>());

        Map<CurrencyAdaptType, BigDecimal> currencyDollarRateMap = accountBalances.stream()
                .map(AccountBalance::getCurrencyAdaptType).distinct()
                .collect(Collectors.toMap(o -> o, currencyService::getDollarRate));

        List<AccountBalanceVO> accountBalanceVOS = new ArrayList<>(accountBalances.size());
        var totalDollarBalance = accountBalances.stream()
                .map(accountBalance -> {
                    CurrencyAdaptType currencyAdaptType = accountBalance.getCurrencyAdaptType();
                    BigDecimal rate = currencyDollarRateMap.getOrDefault(currencyAdaptType, BigDecimal.ONE);

                    var dollarBalance = Optional.ofNullable(accountBalance.getBalance()).orElse(BigDecimal.ZERO).multiply(rate);
                    var dollarFreeze = Optional.ofNullable(accountBalance.getFreeze()).orElse(BigDecimal.ZERO).multiply(rate);
                    var dollarRemain = Optional.ofNullable(accountBalance.getRemain()).orElse(BigDecimal.ZERO).multiply(rate);

                    AccountBalanceVO accountBalanceVO = accountConverter.toVO(accountBalance);
                    accountBalanceVO.setDollarRate(rate);
                    accountBalanceVO.setDollarBalance(dollarBalance);
                    accountBalanceVO.setDollarFreeze(dollarFreeze);
                    accountBalanceVO.setDollarRemain(dollarRemain);
                    accountBalanceVOS.add(accountBalanceVO);

                    return dollarBalance;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(8, RoundingMode.HALF_DOWN);


        var result = new AccountBalanceMainPageVO();
        result.setTotalAccountBalance(totalDollarBalance);
        result.setAccountBalances(accountBalanceVOS);
        return result;
    }

    public AccountBalance get(Long uid,CurrencyAdaptType currencyAdaptType){
        return accountBalanceMapper.get(uid,currencyAdaptType);
    }

}
