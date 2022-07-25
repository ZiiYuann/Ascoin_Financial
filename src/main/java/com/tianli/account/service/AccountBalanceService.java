package com.tianli.account.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.account.convert.AccountConverter;
import com.tianli.account.entity.AccountBalance;
import com.tianli.account.enums.AccountOperationType;
import com.tianli.account.mapper.AccountBalanceMapper;
import com.tianli.account.vo.AccountBalanceMainPageVO;
import com.tianli.account.vo.AccountBalanceSimpleVO;
import com.tianli.account.vo.AccountBalanceVO;
import com.tianli.charge.enums.ChargeType;
import com.tianli.common.CommonFunction;
import com.tianli.common.async.AsyncService;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.common.blockchain.NetworkType;
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

    /**
     * 冻结金额
     *
     * @param uid    用户id
     * @param amount 金额
     * @param sn     订单号
     */
    @Transactional
    public void freeze(long uid, ChargeType type,CurrencyCoin coin, BigDecimal amount, String sn, String des) {
        freeze(uid, type, coin,null, amount, sn, des);
    }

    /**
     * 扣除冻结金额
     *
     * @param uid    用户id
     * @param amount 金额
     * @param sn     订单号
     */
    @Transactional
    public void reduce(long uid, ChargeType type,CurrencyCoin coin, BigDecimal amount, String sn, String des) {
        reduce(uid, type,coin,null, amount, sn, des);
    }

    /**
     * 增加金额
     *
     * @param uid    用户id
     * @param amount 金额
     * @param sn     订单号
     */
    @Transactional
    public void increase(long uid, ChargeType type,CurrencyCoin coin, BigDecimal amount, String sn, String des) {
        increase(uid, type, coin,null, amount, sn, des);
    }

    @Transactional
    public void increase(long uid, ChargeType type, CurrencyCoin coin, NetworkType networkType, BigDecimal amount, String sn, String des) {
        getAndInit(uid, coin);

        if (accountBalanceMapper.increase(uid, amount,coin) <= 0L) {
            ErrorCodeEnum.CREDIT_LACK.throwException();
        }
        AccountBalance accountBalance = accountBalanceMapper.get(uid, coin);
        accountBalanceOperationLogService.save(accountBalance,type,coin,networkType, AccountOperationType.increase, amount, sn, des);
    }

    @Transactional
    public void reduce(long uid, ChargeType type, CurrencyCoin coin, NetworkType networkType, BigDecimal amount, String sn, String des) {
        getAndInit(uid, coin);

        if (accountBalanceMapper.reduce(uid, amount,coin) <= 0L) {
            ErrorCodeEnum.CREDIT_LACK.throwException();
        }
        AccountBalance accountBalance = accountBalanceMapper.get(uid, coin);
        accountBalanceOperationLogService.save(accountBalance,type,coin,networkType ,AccountOperationType.reduce, amount, sn, des);
    }

    /**
     * 扣除可用金额
     *
     * @param uid    用户id
     * @param amount 金额
     * @param sn     订单号
     */
    @Transactional
    public void withdraw(long uid, ChargeType type, CurrencyCoin coin, NetworkType networkType, BigDecimal amount, String sn, String des) {
        getAndInit(uid, coin);

        if (accountBalanceMapper.withdraw(uid, amount,coin) <= 0L) {
            ErrorCodeEnum.CREDIT_LACK.throwException();
        }
        AccountBalance accountBalance = accountBalanceMapper.get(uid, coin);
        accountBalanceOperationLogService.save(accountBalance,type,coin,networkType, AccountOperationType.withdraw, amount, sn, des);
    }

    @Transactional
    public void freeze(long uid, ChargeType type, CurrencyCoin coin, NetworkType networkType, BigDecimal amount, String sn, String des) {
        getAndInit(uid, coin);

        if (accountBalanceMapper.freeze(uid, amount,coin) <= 0L) {
            ErrorCodeEnum.CREDIT_LACK.throwException();
        }
        AccountBalance accountBalance = accountBalanceMapper.get(uid, coin);
        accountBalanceOperationLogService.save(accountBalance,type,coin,networkType, AccountOperationType.freeze, amount, sn, des);

    }

    /**
     * 解冻金额
     *
     * @param uid    用户id
     * @param amount 金额
     * @param sn     订单号
     */
    @Transactional
    public void unfreeze(long uid, ChargeType type, CurrencyCoin coin, NetworkType networkType, BigDecimal amount, String sn, String des) {
        getAndInit(uid, coin);

        if (accountBalanceMapper.unfreeze(uid, amount,coin) <= 0L) {
            ErrorCodeEnum.CREDIT_LACK.throwException();
        }

        AccountBalance accountBalance = accountBalanceMapper.get(uid, coin);
        accountBalanceOperationLogService.save(accountBalance,type,coin,networkType, AccountOperationType.unfreeze, amount, sn, des);
    }

    public void unfreeze(long uid, ChargeType type, CurrencyCoin coin, BigDecimal amount, String sn, String des) {
        unfreeze(uid,type,coin,null,amount,sn,des);
    }

    /**
     * 获取用户余额数据并且初始化
     */
    public AccountBalance getAndInit(long uid, CurrencyAdaptType token) {
       return get(uid,token.getCurrencyCoin());
    }

    public AccountBalance getAndInit(long uid, CurrencyCoin currencyCoin) {
        validCurrencyToken(currencyCoin);
        AccountBalance accountBalanceBalance = accountBalanceMapper.get(uid, currencyCoin);
        if (accountBalanceBalance == null) {
            accountBalanceBalance = AccountBalance.builder()
                    .id(CommonFunction.generalId())
                    .uid(uid)
                    .coin(currencyCoin)
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
    private void validCurrencyToken(CurrencyCoin token) {
        if (Objects.equals(token, CurrencyCoin.usdt) || Objects.equals(token, CurrencyCoin.usdc)) {
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
        var accountBalanceVOS = accountBalanceVOS(uid);
        BigDecimal totalDollarBalance = accountBalanceVOS.stream()
                .map(AccountBalanceVO::getDollarBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(8, RoundingMode.HALF_DOWN);


        var result = new AccountBalanceMainPageVO();
        result.setTotalAccountBalance(totalDollarBalance);
        result.setAccountBalances(accountBalanceVOS);
        return result;
    }

    public List<AccountBalanceVO> getAccountBalanceList(Long uid) {
        return accountBalanceVOS(uid);
    }

    public List<AccountBalanceVO> accountBalanceVOS(Long uid){
        List<AccountBalance> accountBalances = Optional.ofNullable(this.list(uid)).orElse(new ArrayList<>());

        Map<CurrencyCoin, BigDecimal> currencyDollarRateMap = accountBalances.stream()
                .map(AccountBalance::getCoin).distinct()
                .collect(Collectors.toMap(o -> o, currencyService::getDollarRate));

        List<AccountBalanceVO> accountBalanceVOS = new ArrayList<>(accountBalances.size());
        accountBalances.stream()
                .forEach(accountBalance -> {
                    CurrencyCoin currencyCoin = accountBalance.getCoin();
                    BigDecimal rate = currencyDollarRateMap.getOrDefault(currencyCoin, BigDecimal.ONE);

                    var dollarBalance = Optional.ofNullable(accountBalance.getBalance()).orElse(BigDecimal.ZERO).multiply(rate);
                    var dollarFreeze = Optional.ofNullable(accountBalance.getFreeze()).orElse(BigDecimal.ZERO).multiply(rate);
                    var dollarRemain = Optional.ofNullable(accountBalance.getRemain()).orElse(BigDecimal.ZERO).multiply(rate);

                    AccountBalanceVO accountBalanceVO = accountConverter.toVO(accountBalance);
                    accountBalanceVO.setDollarRate(rate);
                    accountBalanceVO.setDollarBalance(dollarBalance);
                    accountBalanceVO.setDollarFreeze(dollarFreeze);
                    accountBalanceVO.setDollarRemain(dollarRemain);
                    accountBalanceVOS.add(accountBalanceVO);
                });
        return accountBalanceVOS;
    }

    public AccountBalance get(Long uid, CurrencyCoin currencyCoin){
        return accountBalanceMapper.get(uid,currencyCoin);
    }

    public AccountBalance get(Long uid,CurrencyAdaptType currencyAdaptType){
        return accountBalanceMapper.get(uid,currencyAdaptType.getCurrencyCoin());
    }

    public AccountBalanceVO getVO(Long uid, CurrencyCoin currencyCoin){
        AccountBalanceVO accountBalanceVO = accountConverter.toVO(accountBalanceMapper.get(uid, currencyCoin));
        BigDecimal dollarRate = currencyService.getDollarRate(accountBalanceVO.getCoin());

        accountBalanceVO.setDollarRate(dollarRate);
        accountBalanceVO.setDollarBalance(dollarRate.multiply(accountBalanceVO.getBalance()));
        accountBalanceVO.setDollarFreeze(dollarRate.multiply(accountBalanceVO.getFreeze()));
        accountBalanceVO.setDollarRemain(dollarRate.multiply(accountBalanceVO.getRemain()));
        return accountBalanceVO;

    }

    /**
     * 获取用户云钱包余额数据
     */
    public Map<Long,BigDecimal> getSummaryBalanceAmount(List<Long> uids){

        LambdaQueryWrapper<AccountBalance> balanceQuery = new LambdaQueryWrapper<AccountBalance>().in(AccountBalance::getUid, uids);
        List<AccountBalance> accountBalances = accountBalanceMapper.selectList(balanceQuery);


        Map<Long, List<AccountBalance>> balanceMapByUid = accountBalances.stream().collect(Collectors.groupingBy(AccountBalance::getUid));
        EnumMap<CurrencyCoin, BigDecimal> dollarRateMap = currencyService.getDollarRateMap();

        // 云钱包余额map
        return balanceMapByUid.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().stream().map(accountBalance -> {
                    BigDecimal balance = accountBalance.getBalance();
                    BigDecimal rate = dollarRateMap.getOrDefault(accountBalance.getCoin(), BigDecimal.ONE);
                    return balance.multiply(rate);
                }).reduce(BigDecimal.ZERO, BigDecimal::add)
        ));
    }

    public List<AccountBalanceSimpleVO> getTotalSummaryData(){
        // todo
        AccountBalanceSimpleVO accountBalanceSimpleVO = new AccountBalanceSimpleVO();
        accountBalanceSimpleVO.setBalanceAmount(BigDecimal.ONE);
        accountBalanceSimpleVO.setBalanceDollarAmount(BigDecimal.ONE);
        accountBalanceSimpleVO.setCoin(CurrencyCoin.usdt);
        accountBalanceSimpleVO.setDollarRate(BigDecimal.ONE);
        return List.of(accountBalanceSimpleVO);
    }


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

}
