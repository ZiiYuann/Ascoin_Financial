package com.tianli.bet;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.tianli.agent.AgentService;
import com.tianli.agent.mapper.Agent;
import com.tianli.bet.controller.BetAgentVO;
import com.tianli.bet.mapper.Bet;
import com.tianli.bet.mapper.BetMapper;
import com.tianli.bet.mapper.BetTypeEnum;
import com.tianli.common.CommonFunction;
import com.tianli.common.async.AsyncService;
import com.tianli.currency.CurrencyService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.management.ruleconfig.ConfigConstants;
import com.tianli.mconfig.ConfigService;
import com.tianli.rebate.RebateService;
import com.tianli.rebate.mapper.Rebate;
import com.tianli.user.referral.UserReferralService;
import com.tianli.user.referral.mapper.UserReferral;
import com.tianli.user.statistics.UserStatisticsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * 押注表 服务实现类
 * </p>
 *
 * @author hd
 * @since 2020-12-04
 */
@Service
public class BetRebateService extends ServiceImpl<BetMapper, Bet> {

    /**
     * 下注返佣
     *
     * @param uid       用户id
     * @param username  账号
     * @param nick      昵称
     * @param avatar    头像
     * @param betId     押注id
     * @param betType   押注类型
     * @param betAmount 押注金额
     * @return 平台返佣
     */
    @Transactional
    public BigInteger exeRebate(Long uid, String username, String nick, String avatar, long betId, BetTypeEnum betType, BigInteger betAmount) {
        BigInteger platformRebate = BigInteger.ZERO;
        // 直接找邀请人信息
        UserReferral byId = userReferralService.getById(uid);
        if (Objects.nonNull(byId)) {
            LinkedList<Agent> agentChain = agentService.agentChain(byId.getReferral_id());
//            LinkedList<Agent> agentChain = null;
            if (CollectionUtils.isEmpty(agentChain)) {
                String ordinary_member_rebate_rate = Objects.equals(betType, BetTypeEnum.normal) ? configService._get(ConfigConstants.USER_REBATE_RATE_NORMAL) : configService._get(ConfigConstants.USER_REBATE_RATE_STEADY);
                double rate = BetDividendsService.rate(ordinary_member_rebate_rate);
                platformRebate = new BigDecimal(betAmount).multiply(new BigDecimal(String.valueOf(rate))).toBigInteger();
                // 返佣普通用户
                if (platformRebate.compareTo(BigInteger.ZERO) > 0) {
//                    currencyService.increase(byId.getReferral_id(), CurrencyTypeEnum.normal, platformRebate, String.format("bet_%s", betId), CurrencyLogDes.平台返佣.name());
                    rebateService.save(Rebate.builder()
                            .create_time(LocalDateTime.now())
                            .id(CommonFunction.generalId())
                            .uid(uid)
                            .uid_username(username)
                            .uid_nick(nick)
                            .uid_avatar(avatar)
                            .amount(betAmount)
                            .rebate_amount(platformRebate)
                            .bet_id(betId)
                            .bet_type(betType)
                            .rebate_uid(byId.getReferral_id()).build());
                }
            } else {
                BigInteger totalBetAmount = betAmount;
                int size = agentChain.size();
                List<Rebate> rebateList = Lists.newArrayList();
                for (int i = 0; i < size; i++) {
                    Agent e = agentChain.get(i);
                    BigDecimal totalBetAmountBigDecimal = new BigDecimal(totalBetAmount);
                    BigInteger myRebate = totalBetAmountBigDecimal.multiply(new BigDecimal("" + rebate_rate(betType, e))).toBigInteger();
                    if (i == 0) {
                        platformRebate = myRebate;
                    }

                    BigInteger lowRebate = BigInteger.ZERO;
                    if (i + 1 < size) {
                        BigDecimal myRebateBigDecimal = new BigDecimal(myRebate);
                        lowRebate = myRebateBigDecimal.multiply(new BigDecimal("" + rebate_rate(betType, agentChain.get(i + 1)))).toBigInteger();
                    }
                    totalBetAmount = myRebate;
                    BigInteger finalRebate = myRebate.subtract(lowRebate);
                    // 增加余额
//                    currencyService.increase(e.getId(), CurrencyTypeEnum.normal, finalRebate, String.format("bet_%s", betId), CurrencyLogDes.平台返佣.name());
                    // 增加返佣记录
                    if (finalRebate.compareTo(BigInteger.ZERO) > 0)
                        rebateList.add(Rebate.builder()
                                .create_time(LocalDateTime.now())
                                .id(CommonFunction.generalId())
                                .uid(uid)
                                .uid_username(username)
                                .uid_nick(nick)
                                .uid_avatar(avatar)
                                .amount(betAmount)
                                .rebate_amount(finalRebate)
                                .bet_id(betId)
                                .bet_type(betType)
                                .rebate_uid(e.getId()).build());
                    asyncService.asyncSuccessRequest(() -> {
                        // 总返佣
                        userStatisticsService.incrementRebate(e.getId(), finalRebate);
                    });
                }
                if ((rebateList.size()) > 0){
                    if (!rebateService.saveBatch(rebateList)) ErrorCodeEnum.SYSTEM_ERROR.throwException();
                }
            }
        }
        return platformRebate;
    }

    /**
     * 下注结算完成返佣
     *
     * @param uid       用户id
     * @param username  账号
     * @param nick      昵称
     * @param avatar    头像
     * @param betId     押注id
     * @param betType   押注类型
     * @param betAmount 押注金额
     * @param dividendsRate
     * @return 平台返佣
     */
    @Transactional
    public BigInteger exeRebateV2(Long uid, String username, String nick, String avatar, long betId, BetTypeEnum betType, BigInteger betAmount, double dividendsRate) {
        BigInteger platformRebate = BigInteger.ZERO;
        // 直接找邀请人信息
        UserReferral byId = userReferralService.getById(uid);
        if (Objects.nonNull(byId)) {

            // 直接邀请计算抽水
            LinkedList<Agent> agentChain;
            String ordinary_member_rebate_rate = Objects.equals(betType, BetTypeEnum.normal) ? configService._get(ConfigConstants.USER_REBATE_RATE_NORMAL) : configService._get(ConfigConstants.USER_REBATE_RATE_STEADY);
            double rate = BetDividendsService.rate(ordinary_member_rebate_rate);
            platformRebate = new BigDecimal(betAmount).multiply(new BigDecimal(String.valueOf(rate))).multiply(new BigDecimal(String.valueOf(dividendsRate))).toBigInteger();
            // 返佣普通用户
            if (platformRebate.compareTo(BigInteger.ZERO) > 0) {
//                currencyService.increase(byId.getReferral_id(), CurrencyTypeEnum.normal, platformRebate, String.format("bet_%s", betId), CurrencyLogDes.平台返佣.name());
                rebateService.save(Rebate.builder()
                        .create_time(LocalDateTime.now())
                        .id(CommonFunction.generalId())
                        .uid(uid)
                        .uid_username(username)
                        .uid_nick(nick)
                        .uid_avatar(avatar)
                        .amount(betAmount)
                        .rebate_amount(platformRebate)
                        .bet_id(betId)
                        .bet_type(betType)
                        .rebate_uid(byId.getReferral_id()).build());
            }

            BetAgentVO betAgentVO = getAgentList(byId,uid,null);
            agentChain = Optional.ofNullable(betAgentVO).map(e -> betAgentVO.getAgentList()).orElse(null);

            if (!CollectionUtils.isEmpty(agentChain)) {
//                    BigInteger totalBetAmount = betAmount;
                int size = agentChain.size();
                List<Rebate> rebateList = Lists.newArrayList();
                BigInteger preRebate = null;
                BigDecimal totalBetAmountBigDecimal = new BigDecimal(String.valueOf(dividendsRate)).multiply(new BigDecimal(betAmount));
                for (int i = 0; i < size; i++) {
                    Agent e = agentChain.get(i);
                    preRebate = totalBetAmountBigDecimal.multiply(new BigDecimal("" + rebate_rate(betType, e))).toBigInteger();
                    if (i == 0) {
                        platformRebate = platformRebate.add(preRebate);
                    }

                    BigInteger lowRebate = BigInteger.ZERO;
                    if (i + 1 < size) {
//                            BigDecimal myRebateBigDecimal = new BigDecimal(myRebate);
                        lowRebate = totalBetAmountBigDecimal.multiply(new BigDecimal("" + rebate_rate(betType, agentChain.get(i + 1)))).toBigInteger();
                    }
                    if (i + 1 == size && Objects.nonNull(betAgentVO.getUserId())){
                        Double rebateProportion = Objects.equals(betType, BetTypeEnum.normal)?agentChain.get(i).getInvite_normal_rebate_proportion():agentChain.get(i).getInvite_steady_rebate_proportion();
                        if(!rebateProportion.equals(0D))
                        lowRebate = totalBetAmountBigDecimal.multiply(new BigDecimal("" + invite_rebate_rate(betType, agentChain.get(i)))).toBigInteger();
                    }

                    BigInteger finalRebate = preRebate.subtract(lowRebate);
                    if (finalRebate.compareTo(BigInteger.ZERO) <= 0){
                        finalRebate = BigInteger.ZERO;
                        lowRebate = BigInteger.ZERO;
                    }
                    preRebate = lowRebate;
                    // 增加余额
//                    currencyService.increase(e.getId(), CurrencyTypeEnum.normal, finalRebate, String.format("bet_%s", betId), CurrencyLogDes.平台返佣.name());
                    // 增加返佣记录
                    if (finalRebate.compareTo(BigInteger.ZERO) > 0)
                        rebateList.add(Rebate.builder()
                                .create_time(LocalDateTime.now())
                                .id(CommonFunction.generalId())
                                .uid(uid)
                                .uid_username(username)
                                .uid_nick(nick)
                                .uid_avatar(avatar)
                                .amount(betAmount)
                                .rebate_amount(finalRebate)
                                .bet_id(betId)
                                .bet_type(betType)
                                .rebate_uid(e.getId()).build());

                    // 有直接邀请返佣比例
                    if (i + 1 == size && lowRebate.compareTo(BigInteger.ZERO) > 0 && Objects.nonNull(betAgentVO.getUserId())){
//                        currencyService.increase(betAgentVO.getUserId(), CurrencyTypeEnum.normal, lowRebate, String.format("bet_%s", betId), CurrencyLogDes.平台返佣.name());
                        rebateList.add(Rebate.builder()
                                .create_time(LocalDateTime.now())
                                .id(CommonFunction.generalId())
                                .uid(uid)
                                .uid_username(username)
                                .uid_nick(nick)
                                .uid_avatar(avatar)
                                .amount(betAmount)
                                .rebate_amount(lowRebate)
                                .bet_id(betId)
                                .bet_type(betType)
                                .rebate_uid(betAgentVO.getUserId()).build());
                    }
                    BigInteger finalRebate1 = finalRebate;
                    asyncService.asyncSuccessRequest(() -> {
                        // 总返佣
                        userStatisticsService.incrementRebate(e.getId(), finalRebate1);
                    });
                }
                if ((rebateList.size()) > 0) {
                    if (!rebateService.saveBatch(rebateList)) ErrorCodeEnum.SYSTEM_ERROR.throwException();
                }
            }

        }
        return platformRebate;
    }

    /**
     * 获取最低代理直属下级及代理链
     * @param userReferral
     * @param uid
     * @return
     */
    private BetAgentVO getAgentList(UserReferral userReferral, Long uid, Long preUid){
        LinkedList<Agent> agentChain = agentService.agentChain(userReferral.getId());
        if (!CollectionUtils.isEmpty(agentChain)){
            BetAgentVO betAgentVO = new BetAgentVO();
            betAgentVO.setAgentList(agentChain);
            betAgentVO.setUserId(preUid);
            if (agentChain.getLast().getId().equals(uid)){
                agentChain.removeLast();
            }
            return betAgentVO;
        }
        UserReferral referral = userReferralService.getById(userReferral.getReferral_id());
        if (Objects.isNull(referral)) {
            return null;
        }
        preUid = userReferral.getId();
        return getAgentList(referral, uid, preUid.equals(uid)?null:preUid);
    }

    private double rebate_rate(BetTypeEnum betType, Agent agent) {
        double rate = 0.0;
        switch (betType) {
            case normal:
                rate = agent.getNormal_rebate_proportion();
                break;
            case steady:
                rate = agent.getSteady_rebate_proportion();
                break;
        }
        if (rate < 0.0) rate = 0.0;
        if (rate > 1.0) rate = 1.0;
        return rate;
    }

    private double invite_rebate_rate(BetTypeEnum betType, Agent agent) {
        double rate = 0.0;
        switch (betType) {
            case normal:
                rate = agent.getInvite_normal_rebate_proportion();
                break;
            case steady:
                rate = agent.getInvite_steady_rebate_proportion();
                break;
        }
        if (rate < 0.0) rate = 0.0;
        if (rate > 1.0) rate = 1.0;
        return rate;
    }

    /**
     * 执行统计
     *
     * @param uid    下注用户id
     * @param amount 押注金额
     */
    public void exeStatistics(Long uid, BigInteger amount) {
        userStatisticsService.incrementMyAmount(uid, amount);
        UserReferral byId = userReferralService.getById(uid);
        if (Objects.nonNull(byId)) {
            long referral_id = byId.getReferral_id();
            userStatisticsService.incrementTeamAmount(referral_id, amount);
            List<Agent> agentsByUid = agentService.superiorAgentChain(referral_id);
            if (CollectionUtils.isEmpty(agentsByUid)) {
                return;
            }
            userStatisticsService.incrementTeamAmountByIds(agentsByUid.stream().map(Agent::getId).collect(Collectors.toList()), amount);
        }

    }
    public void exeStatisticsV3(Long uid, BigInteger amount) {
        userStatisticsService.incrementMyAmount(uid, amount);
        UserReferral byId = userReferralService.getById(uid);
        if (Objects.nonNull(byId)) {
            long referral_id = byId.getReferral_id();
            // 邀请链
            LinkedList<Long> list = userReferralService.userReferralChain(referral_id);
//            userStatisticsService.incrementTeamAmount(referral_id, amount);
//            List<Agent> agentsByUid = agentService.superiorAgentChain(referral_id);
//            if (CollectionUtils.isEmpty(agentsByUid)) {
//                return;
//            }
            userStatisticsService.incrementTeamAmountByIds(list, amount);
        }

    }

    @Resource
    private AsyncService asyncService;

    @Resource
    private RebateService rebateService;

    @Resource
    private ConfigService configService;

    @Resource
    private UserReferralService userReferralService;

    @Resource
    private AgentService agentService;

    @Resource
    private UserStatisticsService userStatisticsService;

    @Resource
    private CurrencyService currencyService;

}
