package com.tianli.account.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.MoreObjects;
import com.tianli.account.convert.AccountConverter;
import com.tianli.account.entity.AccountBalance;
import com.tianli.account.enums.AccountOperationType;
import com.tianli.account.mapper.AccountBalanceMapper;
import com.tianli.account.service.AccountBalanceOperationLogService;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.account.vo.AccountBalanceMainPageVO;
import com.tianli.account.vo.AccountBalanceSimpleVO;
import com.tianli.account.vo.AccountBalanceVO;
import com.tianli.account.vo.UserAssetsVO;
import com.tianli.chain.entity.CoinBase;
import com.tianli.chain.service.CoinBaseService;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.service.OrderService;
import com.tianli.common.CommonFunction;
import com.tianli.common.blockchain.NetworkType;
import com.tianli.currency.service.CurrencyService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.financial.service.FinancialRecordService;
import com.tianli.financial.service.FinancialService;
import com.tianli.financial.vo.DollarIncomeVO;
import com.tianli.fund.query.FundRecordQuery;
import com.tianli.fund.service.IFundRecordService;
import com.tianli.management.dto.AmountDto;
import com.tianli.tool.ApplicationContextTool;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
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
public class AccountBalanceServiceImpl extends ServiceImpl<AccountBalanceMapper, AccountBalance>
        implements AccountBalanceService {

    private static final Set<String> FIXED_COINS = new HashSet<>();

    static {
        FIXED_COINS.add("usdt");
        FIXED_COINS.add("usdc");
        FIXED_COINS.add("eth");
        FIXED_COINS.add("bnb");
    }


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
        AccountBalanceServiceImpl bean = ApplicationContextTool.getBean(this.getClass());
        Optional.ofNullable(bean).orElseThrow(ErrorCodeEnum.SYSTEM_ERROR :: generalException)
                .unfreeze(uid, type, coin, null, amount, sn, des);
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
     * 获取余额主页面信息
     *
     * @param uid 用户id
     * @return 账户余额主页面VO
     */
    public AccountBalanceMainPageVO accountSummary(Long uid, boolean fixedCoin) {


        DollarIncomeVO income = financialService.income(uid);

        var accountBalanceVOS = accountList(uid);
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
        // 需要显示的币别
        Set<String> coinNames = fixedCoin ? new HashSet<>(FIXED_COINS) : coinBaseService.pushCoinNames();
        // 过滤掉不显示掉币别账户
        accountBalanceVOS = accountBalanceVOS.stream()
                .filter(accountBalanceVO -> coinNames.contains(accountBalanceVO.getCoin())).collect(Collectors.toList());
        existCoinNames.forEach(coinNames::remove);

        for (String coin : coinNames) {
            CoinBase coinBase = validCurrencyToken(coin);
            AccountBalanceVO accountBalanceVO = AccountBalanceVO.getDefault(coinBase);
            accountBalanceVO.setDollarRate(currencyService.getDollarRate(String.valueOf(coin)));
            accountBalanceVO.setWeight(coinBase.getWeight());
            accountBalanceVOS.add(accountBalanceVO);
        }

        // 重新排序
        accountBalanceVOS.sort((a, b) -> {
            if (a.getDollarAssets().compareTo(b.getDollarAssets()) == 0) {
                if (a.getWeight() > b.getWeight()) {
                    return -1;
                }
                return 1;
            } else {
                return b.getDollarAssets().compareTo(a.getDollarAssets());
            }
        });

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

    @Override
    public AccountBalanceMainPageVO accountSummary(Long uid) {
        return accountSummary(uid, false);
    }

    public BigDecimal dollarBalance(Long uid) {
        List<AccountBalanceVO> accountBalanceList = accountList(uid);
        List<AmountDto> amountDtoList = accountBalanceList.stream().map(accountBalanceVO ->
                new AmountDto(accountBalanceVO.getBalance(), accountBalanceVO.getCoin())).collect(Collectors.toList());
        return currencyService.calDollarAmount(amountDtoList);
    }

    public List<AccountBalanceVO> accountList(Long uid) {
        List<AccountBalance> accountBalances = Optional.ofNullable(this.list(uid)).orElse(new ArrayList<>());
        List<AccountBalanceVO> accountBalanceVOS = new ArrayList<>(accountBalances.size());
        accountBalances.forEach(accountBalance -> accountBalanceVOS.add(accountSingleCoin(uid, accountBalance.getCoin())));
        return accountBalanceVOS;
    }

    @Transactional
    public AccountBalanceVO accountSingleCoin(Long uid, String coinName) {
        CoinBase coinBase = validCurrencyToken(coinName);

        AccountBalanceVO accountBalanceVO = accountConverter.toVO(this.getAndInit(uid, coinName));
        BigDecimal dollarRate = currencyService.getDollarRate(accountBalanceVO.getCoin());


        BigDecimal fundHoldAmount = MoreObjects.firstNonNull(fundRecordService.holdSingleCoin(uid, coinName, null), BigDecimal.ZERO);
        BigDecimal financialHoldAmount = MoreObjects.firstNonNull(financialRecordService.holdSingleCoin(uid, coinName), BigDecimal.ZERO);

        BigDecimal assets = accountBalanceVO.getRemain().add(accountBalanceVO.getFreeze()).add(fundHoldAmount).add(financialHoldAmount);

        accountBalanceVO.setAssets(assets);
        accountBalanceVO.setDollarAssets(assets.multiply(dollarRate));
        accountBalanceVO.setDollarRate(dollarRate);
        accountBalanceVO.setHoldAmount(fundHoldAmount.add(financialHoldAmount));
        accountBalanceVO.setDollarFreeze(dollarRate.multiply(accountBalanceVO.getFreeze()));
        accountBalanceVO.setDollarRemain(dollarRate.multiply(accountBalanceVO.getRemain()));
        accountBalanceVO.setDollarBalance(dollarRate.multiply(accountBalanceVO.getBalance()));
        accountBalanceVO.setLogo(coinBase.getLogo());
        accountBalanceVO.setWeight(coinBase.getWeight());
        return accountBalanceVO;

    }

    public UserAssetsVO getUserAssetsVO(Long uid) {
        // 总余额
        BigDecimal dollarBalance = this.dollarBalance(uid);

        BigDecimal fundHoldAmount = fundRecordService.dollarHold(FundRecordQuery.builder().uid(uid).build());

        BigDecimal financialHoldAmount = financialRecordService.dollarHold(uid);

        BigDecimal assets = dollarBalance.add(financialHoldAmount).add(fundHoldAmount);

        BigDecimal purchaseAmount = orderService.uAmount(uid,ChargeType.purchase);

        return UserAssetsVO.builder().uid(uid)
                .assets(assets)
                .financialHoldAmount(financialHoldAmount)
                .fundHoldAmount(fundHoldAmount)
                .purchaseAmount(purchaseAmount)
                .build();
    }

    @Override
    public UserAssetsVO getUserAssetsVO(List<Long> uids) {
        BigDecimal assets = BigDecimal.ZERO;

        if (CollectionUtils.isNotEmpty(uids)) {
            assets = uids.stream().map(id -> this.getUserAssetsVO(id).getAssets())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        return UserAssetsVO.builder().assets(assets).build();
    }

    @Override
    public List<UserAssetsVO> getUserAssetsVOMap(List<Long> uids) {
        if (CollectionUtils.isEmpty(uids)) {
            return Collections.emptyList();
        }

        return uids.stream().map(this::getUserAssetsVO).collect(Collectors.toList());
    }

    /**
     * 获取用户云钱包余额数据
     */
    @SuppressWarnings("unchecked")
    public Map<Long, BigDecimal> getSummaryBalanceAmount(List<Long> uids) {
        if (CollectionUtils.isEmpty(uids)) {
            return MapUtils.EMPTY_SORTED_MAP;
        }
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

    /**
     * 校验币别是否有效 暂时只支持 usdt、usdc、bnb bsc主币、eth eth主币
     *
     * @param tokenName 币别类型
     */
    private CoinBase validCurrencyToken(String tokenName) {
        List<CoinBase> coins = coinBaseService.getPushListCache();
        for (CoinBase coinBase : coins) {
            if (coinBase.getName().equalsIgnoreCase(tokenName)) {
                return coinBase;
            }
        }
        throw ErrorCodeEnum.CURRENCY_NOT_SUPPORT.generalException();
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
    private CoinBaseService coinBaseService;
    @Resource
    private OrderService orderService;


}
