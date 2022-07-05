package com.tianli.bet;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.agent.AgentService;
import com.tianli.agent.mapper.Agent;
import com.tianli.bet.controller.BetDTO;
import com.tianli.bet.controller.BetPageVO;
import com.tianli.bet.mapper.*;
import com.tianli.bet.task.BetOrderTask;
import com.tianli.blocklist.BlackUserListService;
import com.tianli.common.CommonFunction;
import com.tianli.common.Constants;
import com.tianli.common.async.AsyncService;
import com.tianli.common.init.RequestInitService;
import com.tianli.common.init.TspContent;
import com.tianli.common.lock.RedisLock;
import com.tianli.currency.*;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.currency.mapper.Currency;
import com.tianli.currency.mapper.DiscountCurrency;
import com.tianli.currency_token.mapper.CurrencyCoinEnum;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.kline.KLineService;
import com.tianli.kline.task.FastStatBetAmount;
import com.tianli.kline.task.Stat;
import com.tianli.loan.entity.LoanCurrency;
import com.tianli.loan.service.ILoanCurrencyService;
import com.tianli.management.platformfinance.FinanceDailyBreakdownDTO;
import com.tianli.management.platformfinance.FinanceDailyBreakdownDetailsDTO;
import com.tianli.management.platformfinance.FinanceExhibitionDTO;
import com.tianli.management.ruleconfig.ConfigConstants;
import com.tianli.management.ruleconfig.RuleConfigService;
import com.tianli.management.ruleconfig.mapper.BetDuration;
import com.tianli.management.ruleconfig.mapper.BetDurationMapper;
import com.tianli.mconfig.ConfigService;
import com.tianli.robot.RobotCouponService;
import com.tianli.robot.RobotResultService;
import com.tianli.robot.mapper.RobotCoupon;
import com.tianli.robot.mapper.RobotResult;
import com.tianli.tool.BigIntegerTool;
import com.tianli.tool.WebSocketMsg;
import com.tianli.tool.WebSocketMsgTypeEnum;
import com.tianli.tool.WebSocketUtils;
import com.tianli.user.UserService;
import com.tianli.user.mapper.User;
import com.tianli.user.mapper.UserStatus;
import com.tianli.user.referral.UserReferralService;
import com.tianli.user.referral.mapper.UserReferral;
import com.tianli.user.userinfo.UserInfoService;
import com.tianli.user.userinfo.mapper.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 押注表 服务实现类
 * </p>
 *
 * @author hd
 * @since 2020-12-04
 */
@Slf4j
@Service
public class BetService extends ServiceImpl<BetMapper, Bet> {

    @Resource
    private BetService betService;

    @Resource
    RobotCouponService robotCouponService;

    @Resource
    RobotResultService robotResultService;

    @Transactional
    public void settleBetByUid(long uid) {
        List<Bet> list = super.list(new LambdaQueryWrapper<Bet>().isNull(Bet::getComplete_time).eq(Bet::getUid, uid));
        list.forEach(this::settleBet);
    }

    @Transactional
    public Long submit(BetDTO bet) {
        return submit(bet, false);
    }

    public Long submit(BetDTO bet, boolean robot) {
        redisLock.lock("BetController_submit_" + requestInitService.uid(), 1L, TimeUnit.MINUTES);
        // 获取登录人信息
        Long uid = requestInitService.uid();
        User user = userService._get(uid);
        if (Objects.isNull(user) || Objects.equals(user.getStatus(), UserStatus.disable)) {
            ErrorCodeEnum.USER_NOT_EXIST.throwException();
        }
        UserInfo userInfo = userInfoService.getOrSaveById(uid);
        long generalId = CommonFunction.generalId();
        // 冻结下注用户的余额
        BetTypeEnum betType = bet.getBetType();
        BigDecimal betAmount = bet.getAmount();
        BigInteger amount = TokenCurrencyType.usdt_omni.amount(betAmount);
        KlineDirectionEnum betDirection = bet.getBetDirection();
        if (Objects.equals(KlineDirectionEnum.flat, betDirection)) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }

        Double betTime = bet.getBetTime();
        List<BetDuration> betDurations = ruleConfigService.selectAll();
        BetDuration betDuration = betDurations.stream().filter(e -> Objects.equals(betTime, e.getDuration())).findFirst().orElse(null);
        if (Objects.isNull(betDuration)) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }
        //获取 赌场时间最小最大押注金额配置是否生效 的参数,若为true,才校验
        String BET_DURATION_EFFECT_SWITCH = configService.getOrDefault("bet_duration_effect_switch", "false");
        if (Objects.equals(BET_DURATION_EFFECT_SWITCH, "true")) {
            if (TokenCurrencyType.usdt_omni.money(betDuration.getMin_bet_amount()) > bet.getAmount().doubleValue()) {
                ErrorCodeEnum.BET_AMOUNT_TOO_SMALL.throwException();
            }

            /*if (TokenCurrencyType.usdt_omni.money(betDuration.getMax_bet_amount()) < bet.getAmount().doubleValue()) {
                ErrorCodeEnum.BET_AMOUNT_TOO_MUCH.throwException();
            }*/
        }
        BigDecimal real_amount = betAmount.multiply(new BigDecimal("" + betDuration.getExtra_percentage()).movePointLeft(2));
        DiscountCurrency discountCurrency = discountCurrencyService.getById(uid);
        BigInteger weakBalance;
        if (Objects.nonNull(discountCurrency) && Objects.nonNull(discountCurrency.getBalance())) {
            weakBalance = discountCurrency.getBalance();
        } else {
            weakBalance = BigInteger.ZERO;
        }
        LoanCurrency loanCurrency = loanCurrencyService.findByUid(uid, CurrencyCoinEnum.usdt);
        Currency currency = currencyService.get(uid, CurrencyTypeEnum.normal);
        BigInteger loanBalance = TokenCurrencyType.usdt_omni.amount(loanCurrency.getBalance());
        if (amount.compareTo(BigInteger.ZERO) > 0 && (currency.getRemain().add(weakBalance)).add(loanBalance).compareTo(amount) < 0) {
            ErrorCodeEnum.CREDIT_LACK.throwException();
        }
        return this.createBetData(robot, TokenCurrencyType.usdt_omni.amount(real_amount), uid, user, userInfo, generalId, betType, amount, weakBalance, loanBalance, betDirection, betTime, bet.getBetSymbol());
    }

    /**
     * 下注结算
     */
    @Transactional
    public void settleBet(Long id) {
        Bet bet = betService.getById(id);
        settleBet(bet);
    }

    @Transactional
    public void settleBet(Bet bet) {
        System.out.println("订单:[" + bet.getId() + "]执行开始计算结果");
        if (!Objects.equals(bet.getResult(), BetResultEnum.wait)) {
            return;
        }
        Double bet_time = bet.getBet_time();
        LocalDateTime createTime = bet.getCreate_time();
        double second = bet_time * 60;
        LocalDateTime expectedResultTime = createTime.plus((long) second, ChronoUnit.SECONDS);
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(expectedResultTime)) {
            System.out.println("订单:[" + bet.getId() + "]执行中: now.isBefore(expectedResultTime)");
            CompletableFuture.runAsync(() -> {
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                } catch (InterruptedException ignored) {
                }
                betService.settleBet(bet);
            }, Constants.COMPLETABLE_FUTURE_EXECUTOR);
            return;
        }

        //统计结果
        KLineDirectionResult kLineDirectionResult = betKlineService.directionAnalysis(bet, expectedResultTime);
        System.out.println("订单:[" + bet.getId() + "]执行计算结果" + kLineDirectionResult.getResult());
        if (!kLineDirectionResult.getAnalyzable()) {
            CompletableFuture.runAsync(() -> betService.settleBet(bet), Constants.COMPLETABLE_FUTURE_EXECUTOR);
            return;
        }
        KlineDirectionEnum result = kLineDirectionResult.getResult();
        //获取押注结果
        BetResultEnum betResult = BetResultEnum.result(result, bet.getBet_direction());
        BetDividendsService exeService = bet.getBet_type().getExeService();
        int update = betMapper.update(null, new LambdaUpdateWrapper<Bet>().set(Bet::getResult, betResult).eq(Bet::getId, bet.getId()).eq(Bet::getResult, BetResultEnum.wait));
        if (update <= 0) ErrorCodeEnum.SYSTEM_BUSY.throwException();
        bet.setResult(betResult);
        BigInteger platformRebate = BigInteger.ZERO;

        Bet updateBet;
        if (Objects.equals(BetResultEnum.win, betResult)) {
            //押注赢钱时手续费比例
            double pumpRate = exeService.getSysRebateRate();
            updateBet = exeService.winCaseExe(bet, expectedResultTime, result, betResult, pumpRate, kLineDirectionResult.getStartExchangeRate(), kLineDirectionResult.getEndExchangeRate());
//            exeAgentRebate(exeService,updateBet, bet.getBet_type());
//            platformRebate = betRebateService.exeRebateV2(bet.getUid(), bet.getUid_username(), bet.getUid_nick(), bet.getUid_avatar(), bet.getId(), bet.getBet_type(), bet.getAmount(), pumpRate);

        } else {
            updateBet = exeService.loseCaseExe(bet, expectedResultTime, result, betResult, kLineDirectionResult.getStartExchangeRate(), kLineDirectionResult.getEndExchangeRate());
        }
        updateBet.setPlatform_rebate(updateBet.getPlatform_profit());
        //计算代理商分红
//        exeService.calculationAgentDividends(bet, updateBet);
        // 计算代理商分红 V3
        try {
            System.out.println("订单:[" + bet.getId() + "]执行计算分红");
            exeService.calculationAgentDividendsV3(bet, updateBet);
        } catch (Exception e) {
            System.out.println("订单:[" + bet.getId() + "]执行计算分红 异常");
            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(1_000);
                } catch (InterruptedException ignored) {
                }
                betService.settleBet(bet);
            }, Constants.COMPLETABLE_FUTURE_EXECUTOR);
            e.printStackTrace();
            ErrorCodeEnum.throwException("计算代理商分红V3异常");
        }
        if (!super.updateById(updateBet)) {
            CompletableFuture.runAsync(() -> betService.settleBet(bet), Constants.COMPLETABLE_FUTURE_EXECUTOR);
            ErrorCodeEnum.throwException("押注结算时, 更新用户金额失败");
        }
        Long uid = bet.getUid();
        Long id = bet.getId();
        System.out.println("订单:[" + id + "]执行计算分红成功!!!");
        Bet newBetInfo = betService.getById(id);
        BetPageVO resVo = BetPageVO.trans(newBetInfo);
        WebSocketUtils.convertAndSend(uid.toString(), WebSocketMsg.getWebSocketMsg(WebSocketMsgTypeEnum.order_settlement, resVo));
        RobotResult robotResult = robotResultService.getOne(Wrappers.lambdaQuery(RobotResult.class).eq(RobotResult::getBet_index, bet.getId()));
        if (ObjectUtil.isNotNull(robotResult)) {
            robotCouponService.incrementUsedCount(robotResult.getRobot_code());
            log.info("---当前激活码id:{},机器人已执行次数进行-1---", robotResult.getRobot_code());
            RobotCoupon robotCoupon = robotCouponService.getById(robotResult.getRobot_code());
            log.info("---当前激活码id:{},已使用次数:{}---", robotResult.getRobot_code(), robotCoupon.getUsed_count());
        }
        asyncPushRedis(id, uid);
    }

    /**
     * 延迟添加到弹框消息
     */
    private void asyncPushRedis(Long id, Long uid) {
        betOrderTask.offerScheduleRedis(uid, id);
    }

    /**
     * 代理商抽水
     * 上级抽水逻辑
     * dividendsRate 为平台抽佣比例
     * updateBet.getFee 为平台总抽水
     * 需要计算:
     * 平台给上级的抽水, 上上级的抽水
     * 校验上级链中是否有超级管理员
     */
    public void exeAgentRebate(BetDividendsService exeService, Bet updateBet, BetTypeEnum betType) {
        // 系统抽水
        BigInteger sysRebate = updateBet.getFee();
        //平台抽水比例
        double platformRebateRate = exeService.getPlatformRebateRate();
        // 上级代理商+上上级代理商抽水比例 A + B : v = 1 - 平台抽水比例 (占总抽水)
        double agentRebateRate = 1 - platformRebateRate;
        //上级代理商id
        UserReferral byId = userReferralService.getById(updateBet.getUid());
        if (Objects.isNull(byId) || Objects.isNull(byId.getReferral_id())) return;
        Long referralId = byId.getReferral_id();
        Agent agent = agentService.getOne(new LambdaQueryWrapper<Agent>().eq(Agent::getId, referralId));
        //1.没有代理商
        if (Objects.isNull(agent)) return;
        //agent rebateRate
        //上上级代理商id
        Long seniorId = agent.getSenior_id();
        /** 获取上上级代理商给 上级代理商设置的抽水比例 getRebateRate  */

        double rebateRate;
        if (BetTypeEnum.normal.equals(betType)) {
            rebateRate = agent.getNormal_rebate_proportion();
        } else {
            rebateRate = agent.getSteady_rebate_proportion();
        }
        Agent supAgent = agentService.getById(seniorId);
        if (Objects.nonNull(supAgent)) {
            double agentRate = agentRebateRate * (1 - rebateRate);
            BigInteger agentRebate = BigIntegerTool.multiDouble(sysRebate, agentRate);

            //更新上级currency/currencyLog表
            /** sn  */
            currencyService.increase(referralId, CurrencyTypeEnum.normal, agentRebate, "rake_" + updateBet.getId(), CurrencyLogDes.抽水.name());
            double seniorAgentRate = agentRebateRate * rebateRate;
            BigInteger seniorAgentRebate = BigIntegerTool.multiDouble(sysRebate, seniorAgentRate);
            //更新上上级currency/currencyLog表
            currencyService.increase(seniorId, CurrencyTypeEnum.normal, seniorAgentRebate, "rake_" + updateBet.getId(), CurrencyLogDes.抽水.name());

        } else {
            //2.该用户只有上级代理商，没有上上级
            rebateRate = 1;
            double agentRate = agentRebateRate * rebateRate;
            BigInteger agentRebate = BigIntegerTool.multiDouble(sysRebate, agentRate);
            currencyService.increase(referralId, CurrencyTypeEnum.normal, agentRebate, "rake_" + updateBet.getId(), CurrencyLogDes.抽水.name());
        }

        //判断代理商链中是否有超级代理商
        LinkedList<Agent> agentsList = agentService.agentChain(referralId);

        //超级代理商抽水比例
        double supAgentRebateRate = -1;
        for (Agent a : agentsList) {
            if (a.getSuper_agent()) {
                if (supAgentRebateRate == -1) {
                    String s = configService.get(ConfigConstants.SUPER_AGENT_RAKE_RATE);
                    supAgentRebateRate = Double.parseDouble(s);
                }
                BigInteger supAgentRebate = BigIntegerTool.multiDouble(sysRebate, supAgentRebateRate);
                currencyService.increase(a.getId(), CurrencyTypeEnum.normal, supAgentRebate, "rake_" + updateBet.getId(), CurrencyLogDes.抽水.name());
            }
        }
    }

    private static ExecutorService executorService = Executors.newFixedThreadPool(1);

    private Long createBetData(boolean robot, BigInteger real_amount, Long uid, User user, UserInfo userInfo, long generalId,
                               BetTypeEnum betType, BigInteger amount, BigInteger weakBalance, BigInteger loanBalance,
                               KlineDirectionEnum betDirection, Double betTime, String klineType) {

        //定义押注时实际使用的贷款余额
        BigInteger discountUseAmount;
        //定义押注时实际使用的优惠金额
        BigInteger loanUseAmount = BigInteger.ZERO;
        //定义押注时实际使用的普通余额
        BigInteger balance = BigInteger.ZERO;
        //扣减优惠
        discountUseAmount = discountCurrencyService.betAmountReduce(uid, amount, CurrencyTokenEnum.usdt_omni, generalId, "押注");
        if (amount.subtract(discountUseAmount).compareTo(BigInteger.ZERO) > 0) {
            //扣减贷款
            loanUseAmount = loanCurrencyService.betAmountReduce(uid, amount.subtract(discountUseAmount), CurrencyCoinEnum.usdt, generalId, "押注");
        }
        if (amount.compareTo(discountUseAmount.add(loanUseAmount)) > 0) {
            //扣减余额
            balance = amount.subtract(loanUseAmount.add(discountUseAmount));
            currencyService.withdraw(uid, CurrencyTypeEnum.normal, balance, String.format("bet_%s", generalId), CurrencyLogDes.交易.name());
        }
        if (balance.compareTo(BigInteger.ZERO) <= 0) {
            // 1. 扣减余额 (真实余额: 0)
            currencyService.withdraw(uid, CurrencyTypeEnum.normal, BigInteger.ZERO, String.format("bet_%s", generalId), CurrencyLogDes.交易.name());
        }
        /*if (weakBalance.compareTo(amount) >= 0) {
            discountCurrencyService.reduce(uid, amount, CurrencyTokenEnum.usdt_omni, generalId, "押注");
            // 1. 扣减余额 (真实余额: 0)
            currencyService.withdraw(uid, CurrencyTypeEnum.normal, BigInteger.ZERO, String.format("bet_%s", generalId), CurrencyLogDes.交易.name());
            //优惠余额>押注金额,全部使用优惠余额
            discountAmount = amount;
            loanAmount = BigInteger.ZERO;
        } else if (weakBalance.compareTo(BigInteger.ZERO) <= 0 && loanBalance.compareTo(BigInteger.ZERO) <= 0) {
            // 1. 扣减余额 (真实余额)
            currencyService.withdraw(uid, CurrencyTypeEnum.normal, amount, String.format("bet_%s", generalId), CurrencyLogDes.交易.name());
            //押注时无优惠金额可用
            discountAmount = BigInteger.ZERO;
            loanAmount = BigInteger.ZERO;
        } else if (weakBalance.compareTo(BigInteger.ZERO) <= 0 && loanBalance.compareTo(BigInteger.ZERO) >= 0) {
            //扣减贷款
            loanCurrencyService.reduce(uid, amount, CurrencyCoinEnum.usdt, generalId, "押注");
            // 1. 扣减余额 (真实余额: 0)
            currencyService.withdraw(uid, CurrencyTypeEnum.normal, BigInteger.ZERO, String.format("bet_%s", generalId), CurrencyLogDes.交易.name());
            loanAmount = amount;
            discountAmount = BigInteger.ZERO;
        } else if (weakBalance.add(loanBalance).compareTo(BigInteger.ZERO) >= 0) {
            //优惠余额+贷款余额足以支付
            discountCurrencyService.reduce(uid, weakBalance, CurrencyTokenEnum.usdt_omni, generalId, "押注");
            discountAmount = weakBalance;
            loanCurrencyService.reduce(uid, amount.subtract(weakBalance), CurrencyCoinEnum.usdt, generalId, "押注");
            loanAmount = amount.subtract(weakBalance);
        } else {
            discountCurrencyService.reduce(uid, weakBalance, CurrencyTokenEnum.usdt_omni, generalId, "押注");
            BigInteger finalAmount = amount.subtract(weakBalance);
            // 1. 扣减余额 (真实余额: 0)
            currencyService.withdraw(uid, CurrencyTypeEnum.normal, finalAmount, String.format("bet_%s", generalId), CurrencyLogDes.交易.name());
            //扣完优惠金额,不足的部分再用余额
            discountAmount = weakBalance;
        }*/
        Stat stat = null;
        //获取当前的价格
        int crawlCount = 0;
        while (Objects.isNull(stat) && crawlCount++ < 3) {
            //  获取币安kline价格
            stat = kLineService.crawlCurrentBianPrice(klineType);
        }
        if (Objects.isNull(stat)) {
            ErrorCodeEnum.SYSTEM_BUSY.throwException();
        }
        Double currentPrice = stat.getClose();
        LocalDateTime now = LocalDateTime.now();
        Bet createBet = Bet.builder()
                .id(generalId)
                .create_time(now)
                .uid(uid)
                .uid_username(user.getUsername())
                .uid_avatar(userInfo.getAvatar())
                .uid_nick(userInfo.getNick())
                .bet_type(betType)
                .bet_time(betTime)
                .amount(amount)
                .real_amount(real_amount)
                .discount_amount(discountUseAmount)
                .loan_amount(loanUseAmount)
                .start_exchange_rate(currentPrice)
                .result(BetResultEnum.wait)
                .bet_direction(betDirection)
//                .platform_rebate(platformRebate)
                .bet_symbol(klineType)
                .order_type(robot ? "robot" : "normal")
                .build();
        maxBetCordon(amount, betDirection, betTime, stat, currentPrice, createBet);

        userCustomTrend(createBet);

        // 3. 新增押注记录
        log.info("主键id{}, 机器人压单{}, 落库操作", generalId, robot);
        if (!super.save(createBet)) ErrorCodeEnum.SYSTEM_ERROR.throwException();
        TspContent.set("betId", createBet.getId());
        asyncExeStat(robot, user, generalId, amount, createBet);
        return createBet.getId();
    }

    public void asyncExeStat(boolean robot, User user, long generalId, BigInteger amount, Bet createBet) {
        // 4. 异步统计团队押注记录
        log.info("主键id{}, 机器人压单{}, 落库操作", generalId, robot);
        executorService.execute(() -> {
            betRebateService.exeStatisticsV3(user.getId(), amount);
            log.info("betRebateService.exeStatisticsV3 --> Success");
        });
        // 5. 异步提交押注金额到Kline统计队列
        executorService.execute(() -> {
            fastStatBetAmount.handleBetStatData(createBet);
            log.info("fastStatBetAmount.handleBetStatData --> Success");
        });
        // 6: 添加到结算队列
        executorService.execute(() -> {
            betOrderTask.offerSchedule(createBet);
            log.info("betOrderTask.offerSchedule --> Success");
        });
    }

    /**
     * 押注超出阈值直接会输掉
     *
     * @param amount       押注金额
     * @param betDirection 押注的方向
     * @param betTime      押注的时长
     * @param stat         当前的K线数据
     * @param currentPrice 当前的价格
     * @param createBet    创建的订单
     */
    private void maxBetCordon(BigInteger amount, KlineDirectionEnum betDirection, Double betTime, Stat stat, Double currentPrice, Bet createBet) {
        String bet_warning_amount = configService.getOrDefault(ConfigConstants.BET_WARNING_AMOUNT, "1000000000000");
        BigInteger warning_amount = null;
        try {
            warning_amount = TokenCurrencyType.usdt_omni.amount(bet_warning_amount);
        } catch (Exception ignored) {
        }
        // 押注金额大于阈值之后则对当前这笔订单进行(必输操作)
        if (Objects.nonNull(warning_amount) && warning_amount.compareTo(amount) <= 0) {
            Double maxPrice = stat.getHigh();
            Double minPrice = stat.getLow();
            Double resPrice = 0.0;
            Double slip_difference = 0.0;
            String kline_random_fluctuation_ratio = configService.getOrDefault("kline_random_fluctuation_ratio", "0.0001");
            Double rate = Double.valueOf(kline_random_fluctuation_ratio);
            switch (betDirection) {
                case rise:
                    // 用户压涨
                    if (minPrice < currentPrice) {
                        resPrice = ThreadLocalRandom.current().nextDouble(minPrice, currentPrice);
                    } else {
                        resPrice = ThreadLocalRandom.current().nextDouble(minPrice * (1 - rate), currentPrice);
                    }
                    slip_difference = ThreadLocalRandom.current().nextDouble(resPrice * 0.9999, resPrice);
                    break;
                case fall:
                    // 用户压跌
                    if (maxPrice > currentPrice) {
                        resPrice = ThreadLocalRandom.current().nextDouble(currentPrice, maxPrice);
                    } else {
                        resPrice = ThreadLocalRandom.current().nextDouble(currentPrice, maxPrice * (1 + rate));
                    }
                    slip_difference = ThreadLocalRandom.current().nextDouble(resPrice, resPrice * 1.0001);
                    break;
            }
            createBet.setProphecy_end_exchange_rate(resPrice);
            createBet.setSlip_difference(slip_difference);
            TspContent.set("prophecy_end_exchange_rate", slip_difference);
            // 提前1s波动
            TspContent.set("triggerTime", System.currentTimeMillis() + (long) (betTime * 60000 - 1000));
        }
    }

    /**
     * 判断是否对用户进行中自定义走势设置
     */
    private void userCustomTrend(Bet bet) {
//        if(Objects.isNull(bet.getProphecy_end_exchange_rate())){
//            // 查看用户定向配置
//            BlackUserList blackUserList = blackUserListService.getOne(Wrappers.lambdaQuery(BlackUserList.class)
//                    .eq(BlackUserList::getUid, bet.getUid())
//                    .last(" LIMIT 1")
//            );
//            if(Objects.nonNull(blackUserList)){
//                // 结果是输还是赢
//                if(Objects.equals(0, blackUserList.getControl())){
//                    // 输
//
//                }else{
//                    // 赢
//
//                }
//            }
//        }
    }

    public Map<String, BigDecimal> sumProfit(long uid, BetTypeEnum betType, String startTime, String endTime, BetResultEnum result) {
        return baseMapper.selectSumProfit(uid, betType, startTime, endTime, result);
    }

    public BigInteger sumAmount(long uid, BetTypeEnum betType, String startTime, String endTime, BetResultEnum result) {
        return baseMapper.selectSumAmount(uid, betType, startTime, endTime, result);
    }

    public List<BetDuration> durationConfig() {
        List<BetDuration> betDurations = betDurationMapper.selectAll();
        return betDurations.stream().sorted(Comparator.comparingInt(BetDuration::getId)).collect(Collectors.toList());
    }


    public Map<String, BigDecimal> betStatistics(String phone, BetResultEnum result, String startTime, String endTime) {
        return baseMapper.betStatistics(phone, result, startTime, endTime);
    }

    public Map<String, BigDecimal> betStatistics(String ip, String equipment, Boolean grc_result, String phone, BetResultEnum result, String startTime, String endTime, Set<Long> userIds) {
        return baseMapper.betStatistics2(ip, equipment, grc_result, null, null, phone, result, startTime, endTime, userIds);
    }

    public Map<String, BigDecimal> betStatistics(String ip, String equipment, Boolean grc_result, Long uid, BetTypeEnum betType, String phone, BetResultEnum result, String startTime, String endTime) {
        return baseMapper.betStatistics2(ip, equipment, grc_result, uid, betType, phone, result, startTime, endTime, null);
    }

    public BigInteger selectSumAmountByPlatform(String startTime, String endTime, BetResultEnum result, String phone) {
        return baseMapper.selectSumAmountByPlatform(startTime, endTime, result, phone);
    }

    public BigInteger selectSumPlatformProfit(String startTime, String endTime, BetResultEnum result, String phone) {
        return baseMapper.selectSumPlatformProfit(startTime, endTime, result, phone);
    }

    public BigInteger selectSumAgentDividends(String startTime, String endTime, BetResultEnum result, String phone) {
        return baseMapper.selectSumAgentDividends(startTime, endTime, result, phone);
    }

    public List<FinanceExhibitionDTO> getDailyAmount(LocalDateTime start, LocalDateTime end) {
        return betMapper.getDailyAmount(start, end);
    }

    public List<FinanceDailyBreakdownDTO> getDailyBreakdown(LocalDateTime start, LocalDateTime end) {
        return betMapper.getDailyBreakdown(start, end);
    }

    public List<FinanceDailyBreakdownDetailsDTO> getDailyBreakdownDetails(LocalDateTime start, LocalDateTime end) {
        return betMapper.getDailyBreakdownDetails(start, end);
    }

    @Resource
    private BetMapper betMapper;

    @Resource
    private AgentService agentService;

    @Resource
    protected BetKlineService betKlineService;

    @Resource
    protected BetDurationMapper betDurationMapper;

    @Resource
    private AsyncService asyncService;

    @Resource
    private KLineService kLineService;

    @Resource
    private BetRebateService betRebateService;

    @Resource
    private FastStatBetAmount fastStatBetAmount;

    @Resource
    private BlackUserListService blackUserListService;

    @Resource
    private CurrencyService currencyService;

    @Resource
    private DiscountCurrencyService discountCurrencyService;

    @Resource
    ILoanCurrencyService loanCurrencyService;

    @Resource
    private RedisLock redisLock;

    @Resource
    private RequestInitService requestInitService;

    @Resource
    private UserService userService;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private RuleConfigService ruleConfigService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private ConfigService configService;

    @Resource
    private UserReferralService userReferralService;

    @Resource
    private BetOrderTask betOrderTask;

    public int betCount(String ip, String equipment, Boolean grc_result, String phone, BetResultEnum result, String startTime, String endTime, Set<Long> userIds) {
        return baseMapper.betCount(null, ip, equipment, grc_result, phone, result, startTime, endTime, userIds);
    }

    public List<BetPO> betList(Long id, String ip, String equipment, Boolean grc_result, String phone, BetResultEnum result, String startTime, String endTime, Integer page, Integer size) {
        return baseMapper.betList(id, ip, equipment, grc_result, phone, result, startTime, endTime, null, page, size);
    }

    public List<BetPO> betList(String ip, String equipment, Boolean grc_result, String phone, BetResultEnum result, String startTime, String endTime, Set<Long> userIds, Integer page, Integer size) {
        return baseMapper.betList(null, ip, equipment, grc_result, phone, result, startTime, endTime, userIds, page, size);
    }
}
