package com.tianli.account.service.impl;

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
import com.tianli.common.RedisConstants;
import com.tianli.common.blockchain.NetworkType;
import com.tianli.currency.service.CurrencyService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.management.dto.AmountDto;
import com.tianli.product.afinancial.query.FinancialRecordQuery;
import com.tianli.product.afinancial.service.FinancialRecordService;
import com.tianli.product.afinancial.service.FinancialService;
import com.tianli.product.afinancial.vo.DollarIncomeVO;
import com.tianli.product.afund.query.FundRecordQuery;
import com.tianli.product.afund.service.IFundRecordService;
import com.tianli.tool.ApplicationContextTool;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
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

    @Override
    @Transactional
    public void decrease(long uid, ChargeType type, String coin, BigDecimal amount, String sn) {
        decrease(uid, type, coin, amount, sn, null);
    }

    @Override
    @Transactional
    public void decrease(long uid, ChargeType type, String coin, BigDecimal amount, String sn, NetworkType networkType) {
        this.validBlackUser(uid);
        getAndInit(uid, coin);
        if (accountBalanceMapper.decrease(uid, amount, coin) <= 0L) {
            ErrorCodeEnum.CREDIT_LACK.throwException();
        }
        AccountBalance accountBalance = accountBalanceMapper.get(uid, coin);
        accountBalanceOperationLogService.save(accountBalance, type, coin, networkType, amount, sn, AccountOperationType.decrease);
    }

    @Override
    @Transactional
    public void increase(long uid, ChargeType type, String coin, BigDecimal amount, String sn) {
        increase(uid, type, coin, amount, sn, null);
    }

    @Override
    @Transactional
    public void increase(long uid, ChargeType type, String coin, BigDecimal amount, String sn, NetworkType networkType) {
        getAndInit(uid, coin);

        if (accountBalanceMapper.increase(uid, amount, coin) <= 0L) {
            ErrorCodeEnum.CREDIT_LACK.throwException();
        }
        AccountBalance accountBalance = accountBalanceMapper.get(uid, coin);
        accountBalanceOperationLogService.save(accountBalance, type, coin, networkType, amount, sn,AccountOperationType.increase);
    }


    /**
     * 冻结金额
     *
     * @param uid    用户id
     * @param amount 金额
     * @param sn     订单号
     */
    @Override
    @Transactional
    public void freeze(long uid, ChargeType type, String coin, BigDecimal amount, String sn) {
        freeze(uid, type, coin, amount, sn, null);
    }

    @Override
    @Transactional
    public void freeze(long uid, ChargeType type, String coin, BigDecimal amount, String sn, NetworkType networkType) {
        this.validBlackUser(uid);
        getAndInit(uid, coin);

        if (accountBalanceMapper.freeze(uid, amount, coin) <= 0L) {
            ErrorCodeEnum.CREDIT_LACK.throwException();
        }
        AccountBalance accountBalance = accountBalanceMapper.get(uid, coin);
        accountBalanceOperationLogService.save(accountBalance, type, coin, networkType, amount, sn,AccountOperationType.freeze);

    }

    /**
     * 扣除冻结金额
     *
     * @param uid    用户id
     * @param amount 金额
     * @param sn     订单号
     */
    @Override
    @Transactional
    public void reduce(long uid, ChargeType type, String coin, BigDecimal amount, String sn) {
        reduce(uid, type, coin, amount, sn, null);
    }

    @Transactional
    public void pledgeFreeze(long uid, ChargeType type, String coin, BigDecimal amount, String sn) {
        pledgeFreeze(uid, type, coin, amount, sn, null);
    }

    @Transactional
    public void pledgeFreeze(long uid, ChargeType type, String coin, BigDecimal amount, String sn,
                             NetworkType networkType) {
        this.validBlackUser(uid);
        getAndInit(uid, coin);

        if (accountBalanceMapper.pledgeFreeze(uid, amount, coin) <= 0L) {
            ErrorCodeEnum.CREDIT_LACK.throwException();
        }
        AccountBalance accountBalance = accountBalanceMapper.get(uid, coin);
        accountBalanceOperationLogService.save(accountBalance, type, coin, networkType, amount, sn,AccountOperationType.freeze);
    }

    @Transactional
    public void reduce(long uid, ChargeType type, String coin, BigDecimal amount, String sn, NetworkType networkType) {
        this.validBlackUser(uid);
        getAndInit(uid, coin);

        if (accountBalanceMapper.reduce(uid, amount, coin) <= 0L) {
            ErrorCodeEnum.CREDIT_LACK.throwException();
        }
        AccountBalance accountBalance = accountBalanceMapper.get(uid, coin);
        accountBalanceOperationLogService.save(accountBalance, type, coin, networkType, amount, sn,AccountOperationType.reduce);
    }

    /**
     * 解冻金额
     *
     * @param uid    用户id
     * @param amount 金额
     * @param sn     订单号
     */
    @Override
    @Transactional
    public void unfreeze(long uid, ChargeType type, String coin, BigDecimal amount, String sn, NetworkType networkType) {
        getAndInit(uid, coin);

        if (accountBalanceMapper.unfreeze(uid, amount, coin) <= 0L) {
            ErrorCodeEnum.CREDIT_LACK.throwException();
        }

        AccountBalance accountBalance = accountBalanceMapper.get(uid, coin);
        accountBalanceOperationLogService.save(accountBalance, type, coin, networkType, amount, sn,AccountOperationType.unfreeze);
    }

    /**
     * 解冻金额
     *
     * @param uid    用户id
     * @param amount 金额
     * @param sn     订单号
     */
    @Transactional
    public void pledgeUnfreeze(long uid, ChargeType type, String coin, BigDecimal amount, String sn, NetworkType networkType) {
        getAndInit(uid, coin);

        if (accountBalanceMapper.pledgeUnfreeze(uid, amount, coin) <= 0L) {
            ErrorCodeEnum.CREDIT_LACK.throwException();
        }

        AccountBalance accountBalance = accountBalanceMapper.get(uid, coin);
        accountBalanceOperationLogService.save(accountBalance, type, coin, networkType, amount, sn,AccountOperationType.unfreeze);
    }

    @Override
    @Transactional
    public void pledgeReduce(long uid, ChargeType type, String coin, BigDecimal amount, String sn) {
        pledgeReduce(uid, type, coin, amount, sn, null);
    }

    @Transactional
    public void pledgeReduce(long uid, ChargeType type, String coin, BigDecimal amount, String sn, NetworkType networkType) {
        getAndInit(uid, coin);

        if (accountBalanceMapper.pledgeReduce(uid, amount, coin) <= 0L) {
            ErrorCodeEnum.CREDIT_LACK.throwException();
        }

        AccountBalance accountBalance = accountBalanceMapper.get(uid, coin);
        accountBalanceOperationLogService.save(accountBalance, type, coin, networkType, amount, sn,AccountOperationType.reduce);
    }


    @Override
    @Transactional
    public void unfreeze(long uid, ChargeType type, String coin, BigDecimal amount, String sn) {
        this.unfreeze(uid, type, coin, amount, sn, null);
    }

    @Transactional
    public void pledgeUnfreeze(long uid, ChargeType type, String coin, BigDecimal amount, String sn) {
        pledgeUnfreeze(uid, type, coin, amount, sn, null);
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
                    .balance(BigDecimal.ZERO)
                    .freeze(BigDecimal.ZERO)
                    .remain(BigDecimal.ZERO)
                    .pledgeFreeze(BigDecimal.ZERO)
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
     * @param uid     用户id
     * @param version 版本
     * @return 账户余额主页面VO
     */
    public AccountBalanceMainPageVO accountSummary(Long uid, boolean fixedCoin, int version) {


        DollarIncomeVO income = financialService.income(uid);

        var accountBalanceVOS = accountList(uid);
        UserAssetsVO userAssetsVO = this.getAllUserAssetsVO(uid);


        var existCoinNames =
                accountBalanceVOS.stream().map(AccountBalanceVO::getCoin).collect(Collectors.toList());
        // 需要显示的币别
        Set<String> coinNames = fixedCoin ? new HashSet<>(FIXED_COINS) : coinBaseService.pushCoinNames(version);
        // 过滤掉不显示掉币别账户
        accountBalanceVOS = accountBalanceVOS.stream()
                .filter(accountBalanceVO -> coinNames.contains(accountBalanceVO.getCoin())).collect(Collectors.toList());
        existCoinNames.forEach(coinNames::remove);

        for (String coin : coinNames) {
            CoinBase coinBase = getPushBaseCoin(coin);
            if (coinBase == null) {
                continue;
            }
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
        result.setTotalAccountBalance(userAssetsVO.getBalanceAmount());
        result.setTotalDollarHold(income.getHoldFee());
        result.setTotalDollarFreeze(userAssetsVO.getFreezeAmount());
        result.setTotalDollarRemain(userAssetsVO.getRemainAmount());
        result.setTotalDollarPledgeFreeze(userAssetsVO.getPledgeFreezeAmount());
        result.setYesterdayIncomeFee(income.getYesterdayIncomeFee());
        result.setAccrueIncomeFee(income.getAccrueIncomeFee());
        result.setTotalAssets(userAssetsVO.getAssets());
        result.setAccountBalances(accountBalanceVOS);


        return result;
    }

    @Override
    public AccountBalanceMainPageVO accountSummary(Long uid, int version) {
        return accountSummary(uid, false, version);
    }

    public BigDecimal dollarBalance(Long uid) {
        List<AccountBalanceVO> accountBalanceList = accountList(uid);
        List<AmountDto> amountDtoList = accountBalanceList.stream().map(accountBalanceVO ->
                new AmountDto(accountBalanceVO.getBalance(), accountBalanceVO.getCoin())).collect(Collectors.toList());
        return currencyService.calDollarAmount(amountDtoList);
    }

    public List<AccountBalanceVO> accountList(Long uid) {
        List<AccountBalance> accountBalances = Optional.ofNullable(this.list(uid)).orElse(new ArrayList<>());

        Set<String> coinNames = coinBaseService.pushCoinNames();
        accountBalances = accountBalances.stream()
                .filter(accountBalance -> coinNames.contains(accountBalance.getCoin()))
                .collect(Collectors.toList());

        List<AccountBalanceVO> accountBalanceVOS = new ArrayList<>(accountBalances.size());
        accountBalances.forEach(accountBalance -> accountBalanceVOS.add(accountSingleCoin(uid, accountBalance.getCoin())));
        return accountBalanceVOS;
    }

    public AccountBalanceVO accountSingleCoin(Long uid, String coinName) {
        CoinBase coinBase = validCurrencyToken(coinName);

        var bean = Optional.ofNullable(ApplicationContextTool.getBean(AccountBalanceService.class))
                .orElseThrow(ErrorCodeEnum.SYSTEM_ERROR :: generalException);

        AccountBalanceVO accountBalanceVO = accountConverter.toVO(bean.getAndInit(uid, coinName));
        BigDecimal dollarRate = currencyService.getDollarRate(accountBalanceVO.getCoin());


        BigDecimal fundHoldAmount = MoreObjects.firstNonNull(fundRecordService.holdSingleCoin(uid, coinName, null), BigDecimal.ZERO);
        BigDecimal financialHoldAmount = MoreObjects.firstNonNull(financialRecordService
                .holdSingleCoin(FinancialRecordQuery.builder().uid(uid).coin(coinName).build()), BigDecimal.ZERO);

        BigDecimal assets = accountBalanceVO.getRemain().add(accountBalanceVO.getFreeze()).add(fundHoldAmount).add(financialHoldAmount);

        accountBalanceVO.setAssets(assets);
        accountBalanceVO.setDollarAssets(assets.multiply(dollarRate));
        accountBalanceVO.setDollarRate(dollarRate);
        accountBalanceVO.setHoldAmount(fundHoldAmount.add(financialHoldAmount));
        accountBalanceVO.setDollarFreeze(dollarRate.multiply(accountBalanceVO.getFreeze()));
        accountBalanceVO.setDollarRemain(dollarRate.multiply(accountBalanceVO.getRemain()));
        accountBalanceVO.setDollarBalance(dollarRate.multiply(accountBalanceVO.getBalance()));
        accountBalanceVO.setDollarPledgeFreeze(dollarRate.multiply(accountBalanceVO.getPledgeFreeze()));
        accountBalanceVO.setLogo(coinBase.getLogo());
        accountBalanceVO.setWeight(coinBase.getWeight());
        return accountBalanceVO;

    }

    // 用户资产数据，所有币别
    public UserAssetsVO getAllUserAssetsVO(Long uid) {

        // 基金持有
        BigDecimal fundHoldAmount = fundRecordService.dollarHold(FundRecordQuery.builder().uid(uid).build());

        // 理财持有
        BigDecimal financialHoldAmount = financialRecordService.dollarHold(FinancialRecordQuery.builder().uid(uid).build());

        // 申购金额
        BigDecimal purchaseAmount = orderService.uAmount(uid, ChargeType.purchase);

        // 总资产数据
        var accountBalanceVOS = accountList(uid);
        BigDecimal totalBalance = accountBalanceVOS.stream()
                .map(AccountBalanceVO::getDollarBalance)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.DOWN);
        BigDecimal totalRemain = accountBalanceVOS.stream()
                .map(AccountBalanceVO::getDollarRemain)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.DOWN);
        BigDecimal totalFreeze = accountBalanceVOS.stream()
                .map(AccountBalanceVO::getDollarFreeze)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.DOWN);
        BigDecimal totalPledgeFreeze = accountBalanceVOS.stream()
                .map(AccountBalanceVO::getDollarPledgeFreeze)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.DOWN);

        //去掉理财的金额
        BigDecimal assets = totalBalance.add(financialHoldAmount).add(fundHoldAmount);

        return UserAssetsVO.builder().uid(uid)
                .assets(assets)
                .financialHoldAmount(financialHoldAmount)
                .fundHoldAmount(fundHoldAmount)
                .purchaseAmount(purchaseAmount)
                .balanceAmount(totalBalance)
                .remainAmount(totalRemain)
                .freezeAmount(totalFreeze)
                .pledgeFreezeAmount(totalPledgeFreeze)
                .build();
    }

    @Override
    public UserAssetsVO getAllUserAssetsVO(List<Long> uids) {
        BigDecimal assets = BigDecimal.ZERO;
        BigDecimal totalBalance = BigDecimal.ZERO;
        BigDecimal purchaseAmount = BigDecimal.ZERO;

        for (Long uid : uids) {
            UserAssetsVO userAssetsVO = this.getAllUserAssetsVO(uid);
            assets = assets.add(userAssetsVO.getAssets());
            totalBalance = totalBalance.add(userAssetsVO.getBalanceAmount());
            purchaseAmount = purchaseAmount.add(userAssetsVO.getPurchaseAmount());
        }

        return UserAssetsVO.builder()
                .assets(assets)
                .balanceAmount(totalBalance)
                .purchaseAmount(purchaseAmount)
                .build();
    }

    @Override
    public List<UserAssetsVO> getUserAssetsVOMap(List<Long> uids) {
        if (CollectionUtils.isEmpty(uids)) {
            return Collections.emptyList();
        }

        return uids.stream().map(this::getAllUserAssetsVO).collect(Collectors.toList());
    }

    public List<AccountBalanceSimpleVO> accountBalanceSimpleVOs() {
        List<AccountBalanceSimpleVO> accountBalanceSimpleVOS = baseMapper.listAccountBalanceSimpleVO();
        accountBalanceSimpleVOS.forEach(accountBalanceSimpleVO -> {
            BigDecimal rate = currencyService.getDollarRate(accountBalanceSimpleVO.getCoin());
            accountBalanceSimpleVO.setDollarRate(rate);
            accountBalanceSimpleVO.setBalanceDollarAmount(accountBalanceSimpleVO.getBalanceAmount().multiply(rate));
        });
        return accountBalanceSimpleVOS;
    }


    @Override
    @Transactional
    public void c2cTransferIn(long uid, ChargeType type, String coin, BigDecimal amount, String sn, NetworkType networkType){
        getAndInit(uid, coin);
        if (accountBalanceMapper.increase(uid, amount, coin) <= 0L) {
            ErrorCodeEnum.CREDIT_LACK.throwException();
        }
        AccountBalance accountBalance = accountBalanceMapper.get(uid, coin);
        accountBalanceOperationLogService.save(accountBalance, type, coin, networkType, amount, sn,AccountOperationType.increase);
    }

    @Override
    @Transactional
    public void  c2cTransferOut(long uid, ChargeType type, String coin, BigDecimal amount, String sn, NetworkType networkType){
        this.validBlackUser(uid);
        getAndInit(uid, coin);
        if (accountBalanceMapper.decrease(uid, amount, coin) <= 0L) {
            ErrorCodeEnum.CREDIT_LACK.throwException();
        }
        AccountBalance accountBalance = accountBalanceMapper.get(uid, coin);
        accountBalanceOperationLogService.save(accountBalance, type, coin, networkType, amount, sn,AccountOperationType.decrease);
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

    private CoinBase getPushBaseCoin(String tokenName) {
        List<CoinBase> coins = coinBaseService.getPushListCache();
        for (CoinBase coinBase : coins) {
            if (coinBase.getName().equalsIgnoreCase(tokenName)) {
                return coinBase;
            }
        }
        return null;
    }

    private void validBlackUser(Long uid) {
        Boolean member = stringRedisTemplate.opsForSet().isMember(RedisConstants.WITHDRAW_BLACK, uid + "");
        if (Objects.nonNull(member) && Boolean.TRUE.equals(member)) {
            ErrorCodeEnum.WITHDRAW_BLACK.throwException();
        }
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
    @Resource
    private StringRedisTemplate stringRedisTemplate;

}
