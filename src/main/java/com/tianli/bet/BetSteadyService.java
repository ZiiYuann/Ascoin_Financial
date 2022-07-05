package com.tianli.bet;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tianli.agent.AgentService;
import com.tianli.agent.mapper.Agent;
import com.tianli.bet.mapper.Bet;
import com.tianli.bet.mapper.BetResultEnum;
import com.tianli.currency.*;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.dividends.DividendsService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.management.ruleconfig.ConfigConstants;
import com.tianli.mconfig.ConfigService;
import com.tianli.user.UserService;
import com.tianli.user.mapper.User;
import com.tianli.user.referral.UserReferralService;
import com.tianli.user.referral.mapper.UserReferral;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * <p>
 * 稳赚场Service
 * </p>
 *
 * @author hd
 * @since 2020-12-04
 */
@Slf4j
@Service
public class BetSteadyService implements BetDividendsService {

    @Resource
    private ConfigService configService;

    @Resource
    private CurrencyService currencyService;

    @Resource
    private DiscountCurrencyService discountCurrencyService;

    @Resource
    private BetDividendsService betNormalService;

    @Resource
    private UserService userService;

    @Resource
    private DividendsService dividendsService;

    @Resource
    private UserReferralService userReferralService;

    @Resource
    private AgentService agentService;

    @Override
    public double getSysRebateRate() {
        String steady_dividends_rate = configService._get(ConfigConstants.BET_RAKE_RATE_STEADY);
        return BetDividendsService.rate(steady_dividends_rate);
    }

    @Override
    public double getPlatformRebateRate() {
        String bet_plf_rebate_rate_win = configService._get(ConfigConstants.PLATFORM_REBATE_RATE_STEADY);
        return BetDividendsService.rate(bet_plf_rebate_rate_win);
    }

    @Override
    public void calculationAgentDividends(Bet dbBet, Bet bet) {
        betNormalService.calculationAgentDividends(dbBet, bet);
    }

    @Override
    public Bet winCaseExe(Bet bet, LocalDateTime expectedResultTime, KlineDirectionEnum result, BetResultEnum betResult, double dividendsRate, double startExchangeRate, double endExchangeRate) {
        Bet update = betNormalService.winCaseExe(bet, expectedResultTime, result, betResult, dividendsRate, startExchangeRate, endExchangeRate);
        // 计算并增加奖励的BF代币
        BigInteger bigInteger = constantReward(bet.getAmount().subtract(bet.getDiscount_amount()), BetResultEnum.win);
        update.setIncome_BF(bigInteger);
        return update;
    }

    @Override
    public Bet loseCaseExe(Bet bet, LocalDateTime expectedResultTime, KlineDirectionEnum result, BetResultEnum betResult, double startExchangeRate, double endExchangeRate) {
        BigInteger betAmount = bet.getAmount();
        // 稳赚场押注失败也会返回押金
//        currencyService.increase(bet.getUid(), CurrencyTypeEnum.normal, betAmount, String.format("bet_%s", bet.getId()), CurrencyLogDes.交易.name());
        return Bet.builder()
                .id(bet.getId())
                .uid(bet.getUid())
                .complete_time(expectedResultTime)
                .final_direction(result)
                .start_exchange_rate(startExchangeRate)
                .end_exchange_rate(endExchangeRate)
                .result(betResult)
                .earn(BigInteger.ZERO)
                .fee(BigInteger.ZERO)
                .profit(BigInteger.ZERO)
                .income(betAmount)
                .build();
    }

    private BigInteger constantReward(BigInteger betAmount, BetResultEnum betResult){
        double betAmountDouble = TokenCurrencyType.usdt_omni.money(betAmount);
        String normal_constant_reward_json = configService.getOrDefault(ConfigConstants.DEFAULT_BF_CONSTANT_REWARD_KEY, ConfigConstants.DEFAULT_BF_CONSTANT_REWARD_JSON);
        List<BFRewardConfig> list = new Gson().fromJson(normal_constant_reward_json, new TypeToken<List<BFRewardConfig>>() {
        }.getType());
        BFRewardConfig bfRewardConfig = list.stream().filter(e -> e.getMin() < betAmountDouble && e.getMax() >= betAmountDouble).findFirst().orElseGet(null);
        if(Objects.isNull(bfRewardConfig)){
            return BigInteger.ZERO;
        }
        if (Objects.equals(betResult, BetResultEnum.win)){
            double win = bfRewardConfig.getWin2();
            return new BigDecimal(betAmountDouble + "").multiply(new BigDecimal("" + win)).movePointRight(18).toBigInteger();
        }else if(Objects.equals(betResult, BetResultEnum.lose)){
            double lose = bfRewardConfig.getLose2();
            return new BigDecimal(betAmountDouble + "").multiply(new BigDecimal("" + lose)).movePointRight(18).toBigInteger();
        }
        return BigInteger.ZERO;
    }

    @Override
    public void calculationAgentDividendsV2(Bet dbBet, Bet bet) {
        betNormalService.calculationAgentDividendsV2(dbBet, bet);
    }

    @Override
    public void calculationAgentDividendsV3(Bet dbBet, Bet bet) {
        Long uid = bet.getUid();
        User user = userService._get(uid);
        if(Objects.isNull(user)){
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }

        BetResultEnum result = bet.getResult();
        // 可能是USDT / BF
        BigDecimal total_dividends;
        if(Objects.equals(result, BetResultEnum.win)){
            total_dividends = new BigDecimal(bet.getFee());
        }else{
            initLoseProfit(bet);
            doIncrease(dbBet, bet, uid);
            return;
        }
        CurrencyTokenEnum token = CurrencyTokenEnum.usdt_omni;
        // 判断是否使用BF抵扣
        if(user.getBF()){
            Map<String, Object> stringObjectMap = transBF(uid, total_dividends, bet, dbBet);
            total_dividends = (BigDecimal)stringObjectMap.get("total_dividends");
            token = (CurrencyTokenEnum)stringObjectMap.get("token");
        }
        bet.setProfit_token(token);

        // 更新押注用户余额信息
        currencyService.get(uid, CurrencyTypeEnum.normal);

        // 固定奖励 BF
        if (bet.getIncome_BF().compareTo(BigInteger.ZERO) > 0) {
            currencyService.increase(uid, CurrencyTypeEnum.normal, CurrencyTokenEnum.BF_bep20, bet.getIncome_BF(), String.format("bet_%s", bet.getId()), CurrencyLogDes.押注奖励.name());
        }
        if(Objects.equals(result, BetResultEnum.win)){
            // usdt 赢得时候会把钱加回去
//            currencyService.increase(uid, CurrencyTypeEnum.normal, CurrencyTokenEnum.usdt_omni, bet.getIncome(), String.format("bet_%s", bet.getId()), CurrencyLogDes.交易.name());
            doIncrease(dbBet, bet, uid);

        }

        // 1. 平台收益
        String pf_profit_rate = configService.getOrDefault(ConfigConstants.PLATFORM_REBATE_RATE_STEADY, "0.2");
        BigDecimal pfFee = total_dividends.multiply(new BigDecimal(pf_profit_rate));
        // 1.1 平台抽水
        bet.setPf_profit(pfFee.toBigInteger());
        // 剩下的钱
        total_dividends = total_dividends.subtract(pfFee);


        // 2. 隐形的代理商收益
        UserReferral userReferral = userReferralService.getById(uid);
        LinkedList<Long> list = null;
        Agent agent;
        bet.setAgent_profit(BigInteger.ZERO);
        if(Objects.nonNull(userReferral)){
            list = userReferralService.userReferralChain(userReferral.getReferral_id());
            Long last = list.getLast();
            agent = agentService.getById(last);
            if(Objects.nonNull(agent)){
                total_dividends = doDividends(agent, dbBet, bet, total_dividends,  token);
            }
        }

        // 3. 邀请链收益 80% + 50%
        if(!CollectionUtils.isEmpty(list)){
            doRebates(list, total_dividends, token, bet, dbBet, uid);
        }

        // 4. 设置部分属性
        if(Objects.isNull(bet.getSurplus_profit())){
            bet.setSurplus_profit(total_dividends.toBigInteger());
        }
        bet.setPlatform_profit(bet.getPf_profit().add(bet.getSurplus_profit()));
        if(Objects.isNull(bet.getAgent_profit())){
            bet.setAgent_profit(BigInteger.ZERO);
        }
        bet.setAgent_dividends(bet.getAgent_profit());
        if(Objects.isNull(bet.getChain_profit())){
            bet.setChain_profit(BigInteger.ZERO);
        }
        bet.setPlatform_rebate(bet.getChain_profit());
    }

    private void doIncrease(Bet dbBet, Bet bet, Long uid) {
        BigInteger discount_amount = dbBet.getDiscount_amount();
        if(Objects.nonNull(discount_amount) && discount_amount.compareTo(BigInteger.ZERO) > 0){
            // 返回押注的真是金额
            currencyService.increase(uid, CurrencyTypeEnum.normal, CurrencyTokenEnum.usdt_omni, bet.getIncome().subtract(discount_amount), String.format("bet_%s", bet.getId()), CurrencyLogDes.交易.name());
            // 返回优惠金额
            discountCurrencyService.increase(uid, discount_amount, CurrencyTokenEnum.usdt_omni, bet.getId(), "押注结算");
        }else{
            // usdt 赢得时候会把钱加回去
            currencyService.increase(uid, CurrencyTypeEnum.normal, CurrencyTokenEnum.usdt_omni, bet.getIncome(), String.format("bet_%s", bet.getId()), CurrencyLogDes.交易.name());
        }
    }

    private void initLoseProfit(Bet bet) {
        bet.setProfit_token(CurrencyTokenEnum.usdt_omni);
        bet.setPf_profit(BigInteger.ZERO);
        bet.setAgent_profit(BigInteger.ZERO);
        bet.setChain_profit(BigInteger.ZERO);
        bet.setSurplus_profit(BigInteger.ZERO);
        bet.setPlatform_profit(BigInteger.ZERO);
        bet.setAgent_dividends(BigInteger.ZERO);
        bet.setPlatform_rebate(BigInteger.ZERO);
    }

    @Override
    public Map<String, Object> transBF(Long uid, BigDecimal total_dividends, Bet bet, Bet dbBet) {
        return betNormalService.transBF(uid, total_dividends, bet, dbBet);
    }

    @Override
    public void doRebates(LinkedList<Long> list, BigDecimal total_dividends, CurrencyTokenEnum token, Bet bet, Bet dbBet, Long uid) {
        betNormalService.doRebates(list, total_dividends, token, bet, dbBet, uid);
    }

    @Override
    public BigDecimal doDividends(Agent agent, Bet dbBet, Bet bet, BigDecimal total_dividends, CurrencyTokenEnum token) {
        return betNormalService.doDividends(agent, dbBet, bet, total_dividends, token);
    }

}
