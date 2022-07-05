package com.tianli.bet;

import cn.hutool.core.util.ObjectUtil;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tianli.agent.AgentService;
import com.tianli.agent.mapper.Agent;
import com.tianli.bet.mapper.Bet;
import com.tianli.bet.mapper.BetResultEnum;
import com.tianli.common.CommonFunction;
import com.tianli.currency.*;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.currency.mapper.Currency;
import com.tianli.currency_token.mapper.CurrencyCoinEnum;
import com.tianli.dividends.DividendsService;
import com.tianli.dividends.mapper.Dividends;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.loan.service.ILoanCurrencyService;
import com.tianli.management.ruleconfig.ConfigConstants;
import com.tianli.mconfig.ConfigService;
import com.tianli.rebate.RebateService;
import com.tianli.rebate.mapper.Rebate;
import com.tianli.tool.BigIntegerTool;
import com.tianli.user.UserService;
import com.tianli.user.mapper.User;
import com.tianli.user.mapper.UserIdentity;
import com.tianli.user.referral.UserReferralService;
import com.tianli.user.referral.mapper.UserReferral;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.*;

/**
 * <p>
 * 普通场Service
 * </p>
 *
 * @author hd
 * @since 2020-12-04
 */
@Slf4j
@Service
public class BetNormalService implements BetDividendsService {

    @Resource
    private ConfigService configService;

    @Resource
    private AgentService agentService;

    @Resource
    private UserReferralService userReferralService;

    @Resource
    private UserService userService;

    @Resource
    private CurrencyService currencyService;

    @Resource
    private DiscountCurrencyService discountCurrencyService;

    @Resource
    ILoanCurrencyService loanCurrencyService;

    @Resource
    private RebateService rebateService;

    @Resource
    private DividendsService dividendsService;

    @Override
    public double getSysRebateRate() {
        String steady_dividends_rate = configService._get(ConfigConstants.BET_RAKE_RATE_NORMAL);
        return BetDividendsService.rate(steady_dividends_rate);
    }

    @Override
    public double getPlatformRebateRate() {
        String platform_rebate_rate_normal = configService._get(ConfigConstants.PLATFORM_REBATE_RATE_NORMAL);
        return BetDividendsService.rate(platform_rebate_rate_normal);
    }

    @Override
    public void calculationAgentDividends(Bet dbBet, Bet bet) {
        Long uid = bet.getUid();
        UserReferral byId = userReferralService.getById(uid);
        if (Objects.isNull(byId)) {
            bet.setAgent_dividends(BigInteger.ZERO);
            return;
        }
        List<Agent> agentList = agentService.agentChain(byId.getReferral_id());
        if (CollectionUtils.isEmpty(agentList)) {
            bet.setAgent_dividends(BigInteger.ZERO);
            return;
        }
        Agent seniorAgent = agentList.get(0);
        if (!Objects.equals(seniorAgent.getIdentity(), UserIdentity.senior_agent)) {
            log.warn(String.format("用户[%s]的上级代理链有问题", uid));
        }
        int size = agentList.size();
        // 上上级给上级的钱
        BigInteger all_profit = bet.getPlatform_profit();
        List<Dividends> dividendsList = Lists.newArrayListWithCapacity(size);
        for (int i = 0; i < size; i++) {
            Agent agent = agentList.get(i);
            if (i == 0) {
                Double real_dividends = agent.getReal_dividends();
                BigInteger agentDividends = BigIntegerTool.multiDouble(all_profit, real_dividends);
                bet.setAgent_dividends(agentDividends);
            }
            double lowRealDividends = 0.0;
            if (i + 1 < size) {
                lowRealDividends = agentList.get(i + 1).getReal_dividends();
            }
            BigInteger myAllProfit = BigIntegerTool.multiDouble(all_profit, agent.getReal_dividends());
            BigInteger lowProfit = BigIntegerTool.multiDouble(myAllProfit, lowRealDividends);
            Dividends dividends = Dividends.builder()
                    .id(CommonFunction.generalId())
                    .create_time(bet.getComplete_time())
                    .bet_id(dbBet.getId())
                    .bet_create_time(dbBet.getCreate_time())
                    .bet_type(dbBet.getBet_type())
                    .bet_time(dbBet.getBet_time())
                    .bet_direction(dbBet.getBet_direction())
                    .amount(dbBet.getAmount())
                    .final_direction(bet.getFinal_direction())
                    .result(bet.getResult())
                    .uid(dbBet.getUid())
                    .uid_username(dbBet.getUid_username())
                    .uid_nick(dbBet.getUid_nick())
                    .uid_avatar(dbBet.getUid_avatar())
                    .dividends_uid(agent.getId())
                    .all_profit(all_profit)
                    .senior_profit(myAllProfit)
                    .my_profit(myAllProfit.subtract(lowProfit))
                    .low_profit(lowProfit)
                    .build();
            all_profit = myAllProfit;
            currencyService.get(dividends.getDividends_uid(), CurrencyTypeEnum.settlement);
            currencyService.increase(dividends.getDividends_uid(), CurrencyTypeEnum.settlement, myAllProfit, String.format("bet_%s", dividends.getBet_id()), CurrencyLogDes.交易.name());
            agentService.increaseProfit(dividends.getDividends_uid(), myAllProfit);
            dividendsList.add(dividends);
        }
        dividendsService.saveBatch(dividendsList);
    }

    @Override
    public Bet winCaseExe(Bet bet, LocalDateTime expectedResultTime, KlineDirectionEnum result, BetResultEnum betResult, double rebateRate, double startExchangeRate, double endExchangeRate) {
        BigInteger betAmount = bet.getReal_amount();
        BigInteger fee = BigIntegerTool.multiDouble(betAmount, rebateRate);
        BigInteger profit = betAmount.subtract(fee);
        Bet build = Bet.builder()
                .id(bet.getId())
                .uid(bet.getUid())
                .complete_time(expectedResultTime)
                .final_direction(result)
                .end_exchange_rate(endExchangeRate)
                .result(BetResultEnum.win)
                .earn(betAmount)
                .fee(fee)
                .profit(profit)
                .income(bet.getAmount().add(profit))
                .build();
        // 计算并增加BF代币
//        BigInteger bigInteger = constantReward(betAmount.subtract(bet.getDiscount_amount()), BetResultEnum.win);
        build.setIncome_BF(BigInteger.ZERO);
        return build;
    }

    @Override
    public Bet loseCaseExe(Bet bet, LocalDateTime expectedResultTime, KlineDirectionEnum result, BetResultEnum betResult, double startExchangeRate, double endExchangeRate) {
        BigInteger betAmount = bet.getReal_amount();
        Bet build = Bet.builder()
                .id(bet.getId())
                .uid(bet.getUid())
                .complete_time(expectedResultTime)
                .final_direction(result)
                .start_exchange_rate(startExchangeRate)
                .end_exchange_rate(endExchangeRate)
                .result(BetResultEnum.lose)
                .earn(betAmount.negate())
                .fee(BigInteger.ZERO)
                .profit(betAmount.negate())
                .income(bet.getAmount().subtract(betAmount))
                .build();
        // 计算并增加BF代币
//        BigInteger bigInteger = constantReward(betAmount.subtract(bet.getDiscount_amount()), BetResultEnum.lose);
        build.setIncome_BF(BigInteger.ZERO);
        return build;
    }

    private BigInteger constantReward(BigInteger betAmount, BetResultEnum betResult) {
        double betAmountDouble = TokenCurrencyType.usdt_omni.money(betAmount);
        String normal_constant_reward_json = configService.getOrDefault(ConfigConstants.DEFAULT_BF_CONSTANT_REWARD_KEY, ConfigConstants.DEFAULT_BF_CONSTANT_REWARD_JSON);
        List<BFRewardConfig> list = new Gson().fromJson(normal_constant_reward_json, new TypeToken<List<BFRewardConfig>>() {
        }.getType());
        BFRewardConfig bfRewardConfig = list.stream().filter(e -> e.getMin() <= betAmountDouble && e.getMax() >= betAmountDouble).findFirst().orElse(null);
        if (Objects.isNull(bfRewardConfig)) {
            return BigInteger.ZERO;
        }
        if (Objects.equals(betResult, BetResultEnum.win)) {
            double win = bfRewardConfig.getWin1();
            return new BigDecimal(betAmountDouble + "").multiply(new BigDecimal("" + win)).movePointRight(18).toBigInteger();
        } else if (Objects.equals(betResult, BetResultEnum.lose)) {
            double lose = bfRewardConfig.getLose1();
            return new BigDecimal(betAmountDouble + "").multiply(new BigDecimal("" + lose)).movePointRight(18).toBigInteger();
        }
        return BigInteger.ZERO;
    }

    @Override
    public void calculationAgentDividendsV2(Bet dbBet, Bet bet) {
        Long uid = bet.getUid();
        UserReferral byId = userReferralService.getById(uid);
        if (Objects.isNull(byId)) {
            bet.setAgent_dividends(BigInteger.ZERO);
            return;
        }
        List<Agent> agentList = agentService.agentChain(byId.getReferral_id());
        if (CollectionUtils.isEmpty(agentList)) {
            bet.setAgent_dividends(BigInteger.ZERO);
            return;
        }
        Agent seniorAgent = agentList.get(0);
        if (!Objects.equals(seniorAgent.getIdentity(), UserIdentity.senior_agent)) {
            log.warn(String.format("用户[%s]的上级代理链有问题", uid));
        }
        int size = agentList.size();
        // 上上级给上级的钱
        BigInteger all_profit = dbBet.getAmount();
        if (dbBet.getResult().equals(BetResultEnum.win)) {
            all_profit = all_profit.negate();
        }
        List<Dividends> dividendsList = Lists.newArrayListWithCapacity(size);
        for (int i = 0; i < size; i++) {
            Agent agent = agentList.get(i);
            if (i == 0) {
                Double real_dividends = agent.getReal_dividends();
                BigInteger agentDividends = BigIntegerTool.multiDouble(all_profit, real_dividends);
                bet.setAgent_dividends(agentDividends);
            }
            double lowRealDividends = 0.0;
            if (i + 1 < size) {
                lowRealDividends = agentList.get(i + 1).getReal_dividends();
            }
            BigInteger myAllProfit = BigIntegerTool.multiDouble(all_profit, agent.getReal_dividends());
            BigInteger lowProfit = BigIntegerTool.multiDouble(all_profit, lowRealDividends);
            Dividends dividends = Dividends.builder()
                    .id(CommonFunction.generalId())
                    .create_time(bet.getComplete_time())
                    .bet_id(dbBet.getId())
                    .bet_create_time(dbBet.getCreate_time())
                    .bet_type(dbBet.getBet_type())
                    .bet_time(dbBet.getBet_time())
                    .bet_direction(dbBet.getBet_direction())
                    .amount(dbBet.getAmount())
                    .final_direction(bet.getFinal_direction())
                    .result(bet.getResult())
                    .uid(dbBet.getUid())
                    .uid_username(dbBet.getUid_username())
                    .uid_nick(dbBet.getUid_nick())
                    .uid_avatar(dbBet.getUid_avatar())
                    .dividends_uid(agent.getId())
                    .all_profit(all_profit)
                    .senior_profit(myAllProfit)
                    .my_profit(myAllProfit.subtract(lowProfit))
                    .low_profit(lowProfit)
                    .build();
            currencyService.get(dividends.getDividends_uid(), CurrencyTypeEnum.settlement);
            currencyService.increase(dividends.getDividends_uid(), CurrencyTypeEnum.settlement, myAllProfit, String.format("bet_%s", dividends.getBet_id()), CurrencyLogDes.交易.name());
            agentService.increaseProfit(dividends.getDividends_uid(), myAllProfit);
            dividendsList.add(dividends);
        }
        dividendsService.saveBatch(dividendsList);
    }

    @Override
    public void calculationAgentDividendsV3(Bet dbBet, Bet bet) {
        Long uid = bet.getUid();
        User user = userService._get(uid);
        if (Objects.isNull(user)) {
            ErrorCodeEnum.OBJECT_NOT_FOUND.throwException();
        }

        BetResultEnum result = bet.getResult();
        // 可能是USDT / BF
        BigDecimal total_dividends;
        if (Objects.equals(result, BetResultEnum.win)) {
            total_dividends = new BigDecimal(bet.getFee());
        } else {
            total_dividends = new BigDecimal(dbBet.getAmount());
        }
        CurrencyTokenEnum token = CurrencyTokenEnum.usdt_omni;
        // 判断是否使用BF抵扣
        if (user.getBF() && Objects.equals(result, BetResultEnum.win)) {
            Map<String, Object> stringObjectMap = transBF(uid, total_dividends, bet, dbBet);
            total_dividends = (BigDecimal) stringObjectMap.get("total_dividends");
            token = (CurrencyTokenEnum) stringObjectMap.get("token");
        }
        bet.setProfit_token(token);

        // 更新押注用户余额信息
        currencyService.get(uid, CurrencyTypeEnum.normal);
        // 固定奖励 BF

        if (bet.getIncome_BF().compareTo(BigInteger.ZERO) > 0) {
            currencyService.increase(uid, CurrencyTypeEnum.normal, CurrencyTokenEnum.BF_bep20, bet.getIncome_BF(), String.format("bet_%s", bet.getId()), CurrencyLogDes.押注奖励.name());
        }

        BigInteger discount_amount = ObjectUtil.isNull(dbBet.getDiscount_amount()) ? BigInteger.ZERO : dbBet.getDiscount_amount();
        BigInteger loan_amount = ObjectUtil.isNull(dbBet.getLoan_amount()) ? BigInteger.ZERO : dbBet.getLoan_amount();
        if (Objects.equals(result, BetResultEnum.win)) {
            BigInteger income = bet.getIncome();
            //实际要返回的普通金额
            BigInteger amount = income.subtract(discount_amount).subtract(loan_amount);
            if (amount.compareTo(BigInteger.ZERO) > 0) {
                // usdt 赢得时候会把钱加回去
                currencyService.increase(uid, CurrencyTypeEnum.normal, CurrencyTokenEnum.usdt_omni, amount, String.format("bet_%s", bet.getId()), CurrencyLogDes.交易.name());
            }
            //返回优惠金额
            if (discount_amount.compareTo(BigInteger.ZERO) > 0) {
                // 返回优惠金额
                discountCurrencyService.increase(uid, discount_amount, CurrencyTokenEnum.usdt_omni, bet.getId(), CurrencyLogDes.押注结算.name());
            }
            //返回贷款金额
            if (loan_amount.compareTo(BigInteger.ZERO) > 0) {
                loanCurrencyService.increase(uid, loan_amount, CurrencyCoinEnum.usdt, bet.getId(), CurrencyLogDes.押注结算.name());
            }
            /*if (Objects.nonNull(discount_amount) && discount_amount.compareTo(BigInteger.ZERO) > 0) {
                // 返回押注的真是金额
                currencyService.increase(uid, CurrencyTypeEnum.normal, CurrencyTokenEnum.usdt_omni, bet.getIncome().subtract(discount_amount), String.format("bet_%s", bet.getId()), CurrencyLogDes.交易.name());
                // 返回优惠金额
                discountCurrencyService.increase(uid, discount_amount, CurrencyTokenEnum.usdt_omni, bet.getId(), "押注结算");
            } else {
                // usdt 赢得时候会把钱加回去
                currencyService.increase(uid, CurrencyTypeEnum.normal, CurrencyTokenEnum.usdt_omni, bet.getIncome(), String.format("bet_%s", bet.getId()), CurrencyLogDes.交易.name());
            }*/
        } else {
            //总下注金额
            BigInteger amount = dbBet.getAmount();
            //实际下注金额
            BigInteger realAmount = dbBet.getReal_amount();
            //需要返还的金额
            BigInteger returnAmount = amount.subtract(realAmount);
            BigInteger ordinaryAmount = amount.subtract(discount_amount).subtract(loan_amount);
            //
            if (returnAmount.compareTo(BigInteger.ZERO) > 0) {
                //优先返回普通的余额
                //普通余额 = amount - discount_amount - loan_amount
                BigInteger returnOrdinaryAmount = currencyService.betIncrease(uid, CurrencyTypeEnum.normal, CurrencyTokenEnum.usdt_omni, returnAmount, ordinaryAmount, String.format("bet_%s", bet.getId()), CurrencyLogDes.交易.name());
                returnAmount = returnAmount.subtract(returnOrdinaryAmount);
                if (returnAmount.compareTo(BigInteger.ZERO) > 0) {
                    BigInteger returnLoanAmount = loanCurrencyService.betIncrease(uid, bet.getId(), CurrencyCoinEnum.usdt, returnAmount, loan_amount, CurrencyLogDes.押注结算.name());
                    returnAmount = returnAmount.subtract(returnLoanAmount);
                }
                if (returnAmount.compareTo(BigInteger.ZERO) > 0) {
                    discountCurrencyService.betIncrease(uid, bet.getId(), CurrencyTokenEnum.usdt_omni, returnAmount, discount_amount, CurrencyLogDes.押注结算.name());
                }
            }
            /*if (Objects.nonNull(discount_amount) && discount_amount.compareTo(BigInteger.ZERO) > 0) {
                if (discount_amount.compareTo(dbBet.getReal_amount()) > 0) {
                    // 返回押注的真实金额
                    currencyService.increase(uid, CurrencyTypeEnum.normal, CurrencyTokenEnum.usdt_omni, dbBet.getAmount().subtract(discount_amount), String.format("bet_%s", bet.getId()), CurrencyLogDes.交易.name());
                    // 返回优惠金额 - 损失金额
                    discountCurrencyService.increase(uid, discount_amount.subtract(dbBet.getReal_amount()), CurrencyTokenEnum.usdt_omni, bet.getId(), "押注结算");
                } else {
                    // 返回押注的全部金额 - 损失金额
                    currencyService.increase(uid, CurrencyTypeEnum.normal, CurrencyTokenEnum.usdt_omni, dbBet.getAmount().subtract(dbBet.getReal_amount()), String.format("bet_%s", bet.getId()), CurrencyLogDes.交易.name());
                }
            } else {
                // usdt 赢得时候会把钱加回去
                currencyService.increase(uid, CurrencyTypeEnum.normal, CurrencyTokenEnum.usdt_omni, dbBet.getAmount().subtract(dbBet.getReal_amount()), String.format("bet_%s", bet.getId()), CurrencyLogDes.交易.name());
            }*/
        }

        // 1. 计算平台收益
        String pf_profit_rate = configService.getOrDefault(ConfigConstants.PLATFORM_REBATE_RATE_NORMAL, "0.2");
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
        if (Objects.nonNull(userReferral)) {
            list = userReferralService.userReferralChain(userReferral.getReferral_id());
            Long last = list.getLast();
            agent = agentService.getById(last);
            if (Objects.nonNull(agent)) {
                total_dividends = doDividends(agent, dbBet, bet, total_dividends, token);
            }
        }

        // 3. 邀请链收益 80% + 50%
        if (!CollectionUtils.isEmpty(list)) {
            doRebates(list, total_dividends, token, bet, dbBet, uid);
        }

        // 4. 设置部分属性
        if (Objects.isNull(bet.getSurplus_profit())) {
            bet.setSurplus_profit(total_dividends.toBigInteger());
        }
        bet.setPlatform_profit(bet.getPf_profit().add(bet.getSurplus_profit()));
        if (Objects.isNull(bet.getAgent_profit())) {
            bet.setAgent_profit(BigInteger.ZERO);
        }
        bet.setAgent_dividends(bet.getAgent_profit());
        if (Objects.isNull(bet.getChain_profit())) {
            bet.setChain_profit(BigInteger.ZERO);
        }
        bet.setPlatform_rebate(bet.getChain_profit());
    }


    public Map<String, Object> transBF(Long uid, BigDecimal total_dividends, Bet bet, Bet dbBet) {
        Map<String, Object> map = new HashMap<>();
        map.put("total_dividends", total_dividends);
        map.put("token", CurrencyTokenEnum.usdt_omni);
        Currency currency = currencyService.get(uid, CurrencyTypeEnum.normal);
        String BF_usdt_rate = configService.getOrDefault(ConfigConstants.BF_USDT_RATE, "1");
        BigDecimal remainBF = new BigDecimal(currency.getRemain_BF());
        BigDecimal needBF = total_dividends.divide(new BigDecimal(BF_usdt_rate)).movePointRight(10);
        String usdt_BF_discount_rate = configService.getOrDefault(ConfigConstants.USDT_BF_DISCOUNT_RATE, "0.75");
        BigDecimal discountAmount = needBF.multiply(BigDecimal.ONE.subtract(new BigDecimal(usdt_BF_discount_rate)));
        if (remainBF.compareTo(discountAmount) >= 0) {
            // 使用BF顶替usdt进行分红, 更新bet数据
            boolean withdraw = true;
            try {
                currencyService.withdraw(uid, CurrencyTypeEnum.normal, CurrencyTokenEnum.BF_bep20, discountAmount.toBigInteger(), String.format("bet_%s", bet.getId()), CurrencyLogDes.交易.name());
            } catch (Exception e) {
                withdraw = false;
            }
            if (withdraw) {
                bet.setIncome(dbBet.getAmount().add(bet.getEarn()));
                bet.setBase_BF(needBF.toBigInteger());
                bet.setFinal_BF(discountAmount.toBigInteger());
                map.put("total_dividends", discountAmount);
                map.put("token", CurrencyTokenEnum.BF_bep20);
            }
        }
        return map;
    }

    public void doRebates(LinkedList<Long> list, BigDecimal total_dividends, CurrencyTokenEnum token, Bet bet, Bet dbBet, Long uid) {
        int size = list.size();
        BigDecimal firstRate = new BigDecimal(configService.getOrDefault(ConfigConstants.PROPORTION_OF_FIRST_REBATE, "0.8"));
        BigDecimal secondRate = new BigDecimal(configService.getOrDefault(ConfigConstants.PROPORTION_OF_SECOND_REBATE, "0.5"));
        ArrayList<Rebate> rebateList = Lists.newArrayListWithCapacity(size);
        BigDecimal total_chain_dividends = BigDecimal.ZERO;
        for (int i = 0; i < size; i++) {
            Long awardUid = list.get(i);
            BigDecimal awardNum = total_dividends.multiply(i == 0 ? firstRate : secondRate);
            currencyService.get(awardUid, CurrencyTypeEnum.normal);
//            currencyService.increase(awardUid, CurrencyTypeEnum.normal, token, awardNum.toBigInteger(), String.format("bet_%s", bet.getId()), CurrencyLogDes.平台返佣.name());
            rebateList.add(Rebate.builder()
                    .create_time(LocalDateTime.now())
                    .id(CommonFunction.generalId())
                    .uid(uid)
                    .uid_username(dbBet.getUid_username())
                    .uid_nick(dbBet.getUid_nick())
                    .uid_avatar(dbBet.getUid_avatar())
                    .amount(dbBet.getAmount())
                    .rebate_amount(awardNum.toBigInteger())
                    .token(token)
                    .bet_id(dbBet.getId())
                    .bet_type(dbBet.getBet_type())
                    .bet_type(dbBet.getBet_type())
                    .rebate_uid(awardUid)
                    .build());
            total_chain_dividends = total_chain_dividends.add(awardNum);
            total_dividends = total_dividends.subtract(awardNum);
        }
        // 4. 剩余的返回给平台
        bet.setChain_profit(total_chain_dividends.toBigInteger());
        bet.setSurplus_profit(total_dividends.toBigInteger());
        rebateService.saveBatch(rebateList);
    }

    public BigDecimal doDividends(Agent agent, Bet dbBet, Bet bet, BigDecimal total_dividends, CurrencyTokenEnum token) {
        List<Dividends> dividendsList = new ArrayList<>();
        BigDecimal res = total_dividends;
        if (Objects.nonNull(agent)) {
            bet.setAgent_id(agent.getId());
            bet.setAgent_username(agent.getUsername());
            // 隐形的代理商收益
            Double real_dividends = agent.getReal_dividends();
            BigDecimal dlsFee = total_dividends.multiply(new BigDecimal("" + real_dividends));
            bet.setAgent_profit(dlsFee.toBigInteger());
            // 保存代理商金额
            agentService.increaseProfit(agent.getId(), token, dlsFee.toBigInteger());
            currencyService.get(agent.getId(), CurrencyTypeEnum.settlement);
//            currencyService.increase(agent.getId(), CurrencyTypeEnum.settlement, token, dlsFee.toBigInteger(), String.format("bet_%s", bet.getId()), CurrencyLogDes.平台返佣.name());
            bet.setAgent_id(agent.getId());
            bet.setAgent_username(agent.getUsername());
            Dividends dividends2 = Dividends.builder()
                    .id(CommonFunction.generalId())
                    .create_time(bet.getComplete_time())
                    .bet_id(dbBet.getId())
                    .bet_create_time(dbBet.getCreate_time())
                    .bet_type(dbBet.getBet_type())
                    .bet_time(dbBet.getBet_time())
                    .bet_direction(dbBet.getBet_direction())
                    .amount(dbBet.getAmount())
                    .final_direction(bet.getFinal_direction())
                    .result(bet.getResult())
                    .uid(dbBet.getUid())
                    .uid_username(dbBet.getUid_username())
                    .uid_nick(dbBet.getUid_nick())
                    .uid_avatar(dbBet.getUid_avatar())
                    .dividends_uid(agent.getId())
                    .all_profit(total_dividends.toBigInteger())
                    .senior_profit(BigInteger.ZERO)
                    .my_profit(dlsFee.toBigInteger())
                    .low_profit(BigInteger.ZERO)
                    .profit_token(token)
                    .build();
            dividendsList.add(dividends2);
            res = total_dividends.subtract(dlsFee);
        }
        dividendsService.saveBatch(dividendsList);
        return res;
    }
}
