package com.tianli.account.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.MoreObjects;
import com.tianli.account.convert.AccountConverter;
import com.tianli.account.entity.AccountBalance;
import com.tianli.account.enums.AccountOperationType;
import com.tianli.account.mapper.AccountBalanceMapper;
import com.tianli.account.vo.AccountBalanceMainPageVO;
import com.tianli.account.vo.AccountBalanceSimpleVO;
import com.tianli.account.vo.AccountBalanceVO;
import com.tianli.chain.entity.CoinBase;
import com.tianli.chain.service.CoinService;
import com.tianli.charge.enums.ChargeType;
import com.tianli.common.CommonFunction;
import com.tianli.common.blockchain.NetworkType;
import com.tianli.currency.service.CurrencyService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.financial.service.FinancialRecordService;
import com.tianli.financial.service.FinancialService;
import com.tianli.financial.vo.DollarIncomeVO;
import com.tianli.fund.service.IFundRecordService;
import com.tianli.management.dto.AmountDto;
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
    public void freeze(long uid, ChargeType type, String coin, BigDecimal amount, String sn, String des) {
        freeze(uid, type, coin, null, amount, sn, des);
    }

    /**
     * 扣除冻结金额
     *
     * @param uid    用户id
     * @param amount 金额
     * @param sn     订单号
     */
    @Transactional
    public void reduce(long uid, ChargeType type, String coin, BigDecimal amount, String sn, String des) {
        reduce(uid, type, coin, null, amount, sn, des);
    }

    /**
     * 增加金额
     *
     * @param uid    用户id
     * @param amount 金额
     * @param sn     订单号
     */
    @Transactional
    public void increase(long uid, ChargeType type, String coin, BigDecimal amount, String sn, String des) {
        increase(uid, type, coin, null, amount, sn, des);
    }

    @Transactional
    public void increase(long uid, ChargeType type, String coin, NetworkType networkType, BigDecimal amount, String sn, String des) {
        increase(uid, type, coin, networkType, amount, sn, des, AccountOperationType.increase);
    }

    @Transactional
    public void increase(long uid, ChargeType type, String coin, NetworkType networkType, BigDecimal amount, String sn, String des, AccountOperationType accountOperationType) {
        getAndInit(uid, coin);

        if (accountBalanceMapper.increase(uid, amount, coin) <= 0L) {
            ErrorCodeEnum.CREDIT_LACK.throwException();
        }
        AccountBalance accountBalance = accountBalanceMapper.get(uid, coin);
        accountBalanceOperationLogService.save(accountBalance, type, coin, networkType, accountOperationType, amount, sn, des);
    }

    @Transactional
    public void reduce(long uid, ChargeType type, String coin, NetworkType networkType, BigDecimal amount, String sn, String des) {
        getAndInit(uid, coin);

        if (accountBalanceMapper.reduce(uid, amount, coin) <= 0L) {
            ErrorCodeEnum.CREDIT_LACK.throwException();
        }
        AccountBalance accountBalance = accountBalanceMapper.get(uid, coin);
        accountBalanceOperationLogService.save(accountBalance, type, coin, networkType, AccountOperationType.reduce, amount, sn, des);
    }

    @Transactional
    public void decrease(long uid, ChargeType type, String coin, BigDecimal amount, String sn, String des) {
        decrease(uid, type, coin, null, amount, sn, des);
    }

    @Transactional
    public void decrease(long uid, ChargeType type, String coin, NetworkType networkType,
                         BigDecimal amount, String sn, String des, AccountOperationType accountOperationType) {
        getAndInit(uid, coin);
        if (accountBalanceMapper.decrease(uid, amount, coin) <= 0L) {
            ErrorCodeEnum.CREDIT_LACK.throwException();
        }
        AccountBalance accountBalance = accountBalanceMapper.get(uid, coin);
        accountBalanceOperationLogService.save(accountBalance, type, coin, networkType, accountOperationType, amount, sn, des);
    }

    /**
     * 扣除可用金额
     *
     * @param uid    用户id
     * @param amount 金额
     * @param sn     订单号
     */
    @Transactional
    public void decrease(long uid, ChargeType type, String coin, NetworkType networkType, BigDecimal amount, String sn, String des) {
        decrease(uid, type, coin, networkType, amount, sn, des, AccountOperationType.withdraw);
    }

    @Transactional
    public void freeze(long uid, ChargeType type, String coin, NetworkType networkType, BigDecimal amount, String sn, String des) {
        getAndInit(uid, coin);

        if (accountBalanceMapper.freeze(uid, amount, coin) <= 0L) {
            ErrorCodeEnum.CREDIT_LACK.throwException();
        }
        AccountBalance accountBalance = accountBalanceMapper.get(uid, coin);
        accountBalanceOperationLogService.save(accountBalance, type, coin, networkType, AccountOperationType.freeze, amount, sn, des);

    }

    /**
     * 解冻金额
     *
     * @param uid    用户id
     * @param amount 金额
     * @param sn     订单号
     */
    @Transactional
    public void unfreeze(long uid, ChargeType type, String coin, NetworkType networkType, BigDecimal amount, String sn, String des) {
        getAndInit(uid, coin);

        if (accountBalanceMapper.unfreeze(uid, amount, coin) <= 0L) {
            ErrorCodeEnum.CREDIT_LACK.throwException();
        }

        AccountBalance accountBalance = accountBalanceMapper.get(uid, coin);
        accountBalanceOperationLogService.save(accountBalance, type, coin, networkType, AccountOperationType.unfreeze, amount, sn, des);
    }

    public void unfreeze(long uid, ChargeType type, String coin, BigDecimal amount, String sn, String des) {
        unfreeze(uid, type, coin, null, amount, sn, des);
    }

    @Transactional
    public AccountBalance getAndInit(long uid, String coinName) {
        CoinBase coinBase = validCurrencyToken(coinName);
        AccountBalance accountBalanceBalance = accountBalanceMapper.get(uid, coinName);
        if (accountBalanceBalance == null) {
            accountBalanceBalance = AccountBalance.builder()
                    .id(CommonFunction.generalId())
                    .uid(uid)
                    .coin(coinBase.getName())
                    .logo(coinBase.getLogo())
                    .balance(BigDecimal.ZERO)
                    .freeze(BigDecimal.ZERO)
                    .remain(BigDecimal.ZERO)
                    .build();
            final AccountBalance accountBalanceBalanceFinal = accountBalanceBalance;
            accountBalanceMapper.insert(accountBalanceBalanceFinal);
        }
        return accountBalanceBalance;
    }


    public List<AccountBalance> list(long uid) {
        return accountBalanceMapper.list(uid);
    }

    /**
     * 校验币别是否有效 暂时只支持 usdt、usdc、bnb bsc主币、eth eth主币
     *
     * @param tokenName 币别类型
     */
    private CoinBase validCurrencyToken(String tokenName) {
        List<CoinBase> coins = coinService.effectiveCoinsWithCache();
        for (CoinBase coinBase : coins) {
            if (coinBase.getName().equalsIgnoreCase(tokenName)) {
                return coinBase;
            }
        }
        throw ErrorCodeEnum.CURRENCY_NOT_SUPPORT.generalException();
    }

    /**
     * 获取余额主页面信息
     *
     * @param uid 用户id
     * @return 账户余额主页面VO
     */
    public AccountBalanceMainPageVO getAccountBalanceMainPageVO(Long uid) {


        DollarIncomeVO income = financialService.income(uid);

        var accountBalanceVOS = accountBalanceVOS(uid);
        BigDecimal totalDollarBalance = accountBalanceVOS.stream()
                .map(AccountBalanceVO::getDollarBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.DOWN);
        BigDecimal totalDollarRemain = accountBalanceVOS.stream()
                .map(AccountBalanceVO::getDollarRemain)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.DOWN);
        BigDecimal totalDollarFreeze = accountBalanceVOS.stream()
                .map(AccountBalanceVO::getDollarFreeze)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.DOWN);


        var existCoinNames =
                accountBalanceVOS.stream().map(AccountBalanceVO::getCoin).collect(Collectors.toList());
        Set<String> coinNames = coinService.effectiveCoinNames();
        existCoinNames.forEach(coinNames::remove);

        for (String coin : coinNames) {
            CoinBase coinBase = validCurrencyToken(coin);
            AccountBalanceVO accountBalanceVO = AccountBalanceVO.getDefault(coinBase);
            accountBalanceVO.setDollarRate(currencyService.getDollarRate(String.valueOf(coin)));
            accountBalanceVO.setWeight(coinBase.getWeight());
            accountBalanceVOS.add(accountBalanceVO);
        }

        // 重新排序
        accountBalanceVOS.sort(Comparator.comparing(AccountBalanceVO::getDollarBalance).reversed()
                .thenComparing(AccountBalanceVO::getWeight).reversed());

        var result = new AccountBalanceMainPageVO();
        result.setTotalAccountBalance(totalDollarBalance);
        result.setTotalDollarHold(income.getHoldFee());
        result.setTotalDollarFreeze(totalDollarFreeze);
        result.setTotalDollarRemain(totalDollarRemain);
        result.setYesterdayIncomeFee(income.getYesterdayIncomeFee());
        result.setAccrueIncomeFee(income.getAccrueIncomeFee());
        // 总资产 = 可用 + 持有 + 冻结
        result.setTotalAssets(totalDollarRemain.add(income.getHoldFee()).add(totalDollarFreeze));
        result.setAccountBalances(accountBalanceVOS);


        return result;
    }

    public BigDecimal getDollarBalance(Long uid) {
        List<AccountBalanceVO> accountBalanceList = accountBalanceVOS(uid);
        List<AmountDto> amountDtoList = accountBalanceList.stream().map(accountBalanceVO ->
                new AmountDto(accountBalanceVO.getBalance(), accountBalanceVO.getCoin())).collect(Collectors.toList());
        return currencyService.calDollarAmount(amountDtoList);
    }

    public List<AccountBalanceVO> getAccountBalanceList(Long uid) {
        return accountBalanceVOS(uid);
    }

    public List<AccountBalanceVO> accountBalanceVOS(Long uid) {
        List<AccountBalance> accountBalances = Optional.ofNullable(this.list(uid)).orElse(new ArrayList<>());

        Map<String, BigDecimal> currencyDollarRateMap = accountBalances.stream()
                .map(AccountBalance::getCoin)
                .distinct()
                .collect(Collectors.toMap(o -> o, currencyService::getDollarRate));

        List<AccountBalanceVO> accountBalanceVOS = new ArrayList<>(accountBalances.size());
        accountBalances.forEach(accountBalance -> {
            String coinName = accountBalance.getCoin();
            BigDecimal rate = currencyDollarRateMap.getOrDefault(coinName, BigDecimal.ONE);
            CoinBase coinBase = validCurrencyToken(coinName);
            BigDecimal balance = Optional.ofNullable(accountBalance.getBalance()).orElse(BigDecimal.ZERO);
            var dollarBalance = balance.multiply(rate);
            var dollarFreeze = Optional.ofNullable(accountBalance.getFreeze()).orElse(BigDecimal.ZERO).multiply(rate);
            var dollarRemain = Optional.ofNullable(accountBalance.getRemain()).orElse(BigDecimal.ZERO).multiply(rate);


            BigDecimal fundHoldAmount = MoreObjects.firstNonNull(fundRecordService.holdAmount(uid, coinName, null), BigDecimal.ZERO);
            BigDecimal financialHoldAmount = MoreObjects.firstNonNull(financialRecordService.holdAmountByCoin(uid, coinName), BigDecimal.ZERO);

            AccountBalanceVO accountBalanceVO = accountConverter.toVO(accountBalance);
            accountBalanceVO.setAssets(fundHoldAmount.add(financialHoldAmount).add(balance));
            accountBalanceVO.setDollarAssets(accountBalanceVO.getAssets().multiply(rate));
            accountBalanceVO.setDollarRate(rate);
            accountBalanceVO.setDollarBalance(dollarBalance);
            accountBalanceVO.setDollarFreeze(dollarFreeze);
            accountBalanceVO.setDollarRemain(dollarRemain);
            accountBalanceVO.setLogo(coinBase.getLogo());
            accountBalanceVOS.add(accountBalanceVO);
        });
        return accountBalanceVOS;
    }

    public AccountBalanceVO getVO(Long uid, String coinName) {
        AccountBalanceVO accountBalanceVO = accountConverter.toVO(this.getAndInit(uid, coinName));
        BigDecimal dollarRate = currencyService.getDollarRate(accountBalanceVO.getCoin());


        BigDecimal fundHoldAmount = fundRecordService.holdAmount(uid, coinName, null);
        BigDecimal financialHoldAmount = financialRecordService.holdAmountByCoin(uid, coinName);

        BigDecimal assets = accountBalanceVO.getRemain().add(accountBalanceVO.getFreeze()).add(fundHoldAmount).add(financialHoldAmount);

        CoinBase coinBase = validCurrencyToken(coinName);
        accountBalanceVO.setAssets(assets);
        accountBalanceVO.setDollarAssets(assets.multiply(dollarRate));
        accountBalanceVO.setDollarRate(dollarRate);
        accountBalanceVO.setHoldAmount(fundHoldAmount.add(financialHoldAmount));
        accountBalanceVO.setDollarFreeze(dollarRate.multiply(accountBalanceVO.getFreeze()));
        accountBalanceVO.setDollarRemain(dollarRate.multiply(accountBalanceVO.getRemain()));
        accountBalanceVO.setDollarBalance(dollarRate.multiply(accountBalanceVO.getBalance()));
        accountBalanceVO.setLogo(coinBase.getLogo());
        return accountBalanceVO;

    }

    /**
     * 获取用户云钱包余额数据
     */
    public Map<Long, BigDecimal> getSummaryBalanceAmount(List<Long> uids) {

        LambdaQueryWrapper<AccountBalance> balanceQuery = new LambdaQueryWrapper<AccountBalance>().in(AccountBalance::getUid, uids);
        List<AccountBalance> accountBalances = accountBalanceMapper.selectList(balanceQuery);


        Map<Long, List<AccountBalance>> balanceMapByUid = accountBalances.stream().collect(Collectors.groupingBy(AccountBalance::getUid));

        // 云钱包余额map
        return balanceMapByUid.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().stream().map(accountBalance -> {
                    BigDecimal balance = accountBalance.getBalance();
                    BigDecimal rate = currencyService.getDollarRate(accountBalance.getCoin());
                    return balance.multiply(rate);
                }).reduce(BigDecimal.ZERO, BigDecimal::add)
        ));
    }

    public List<AccountBalanceSimpleVO> getTotalSummaryData() {
        List<AccountBalanceSimpleVO> accountBalanceSimpleVOS = baseMapper.listAccountBalanceSimpleVO();
        accountBalanceSimpleVOS.forEach(accountBalanceSimpleVO -> {
            BigDecimal rate = currencyService.getDollarRate(accountBalanceSimpleVO.getCoin());
            accountBalanceSimpleVO.setDollarRate(rate);
            accountBalanceSimpleVO.setBalanceDollarAmount(accountBalanceSimpleVO.getBalanceAmount().multiply(rate));
        });
        return accountBalanceSimpleVOS;
    }


    @Resource
    private AccountBalanceMapper accountBalanceMapper;
    @Resource
    private AccountBalanceOperationLogService accountBalanceOperationLogService;
    @Resource
    private AccountConverter accountConverter;
    @Resource
    private CurrencyService currencyService;
    @Resource
    private FinancialService financialService;
    @Resource
    private IFundRecordService fundRecordService;
    @Resource
    private FinancialRecordService financialRecordService;
    @Resource
    private CoinService coinService;


}
