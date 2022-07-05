package com.tianli.robot.task;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.gson.Gson;
import com.tianli.bet.KlineDirectionEnum;
import com.tianli.bet.controller.BetDTO;
import com.tianli.bet.mapper.BetTypeEnum;
import com.tianli.common.async.AsyncService;
import com.tianli.common.init.RequestInit;
import com.tianli.common.init.RequestInitService;
import com.tianli.common.lock.RedisLock;
import com.tianli.currency.CurrencyService;
import com.tianli.currency.CurrencyTypeEnum;
import com.tianli.currency.DiscountCurrencyService;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.currency.mapper.Currency;
import com.tianli.currency.mapper.DiscountCurrency;
import com.tianli.currency_token.mapper.CurrencyCoinEnum;
import com.tianli.loan.entity.LoanCurrency;
import com.tianli.loan.service.ILoanCurrencyService;
import com.tianli.management.ruleconfig.RuleConfigService;
import com.tianli.management.ruleconfig.mapper.BetDuration;
import com.tianli.robot.RobotOrderService;
import com.tianli.robot.RobotResultService;
import com.tianli.robot.RobotService;
import com.tianli.robot.mapper.RobotOrder;
import com.tianli.robot.mapper.RobotResult;
import com.tianli.robot.vo.RobotBetVo;
import com.tianli.tool.WebSocketMsg;
import com.tianli.tool.WebSocketMsgTypeEnum;
import com.tianli.tool.WebSocketUtils;
import com.tianli.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RobotBetTask {

    private static final String LOCK_KEY = "ROBOT:BET:REDIS:LOCK";

    @Resource
    private RedisLock redisLock;

    @Resource
    private RobotOrderService robotOrderService;

    @Resource
    private RuleConfigService ruleConfigService;

    @Resource
    private RobotService robotService;

    @Resource
    private UserService userService;

    @Resource
    private RequestInitService requestInitService;

    @Resource
    private AsyncService asyncService;

    @Resource
    private CurrencyService currencyService;

    @Resource
    RobotResultService robotResultService;

    @Resource
    private DiscountCurrencyService discountCurrencyService;

    @Resource
    ILoanCurrencyService loanCurrencyService;

    @Scheduled(cron = "0/3 * * * * ?")
    public void robotBet() {
        asyncService.async(() -> {
            boolean lock = redisLock._lock(LOCK_KEY, 10L, TimeUnit.MINUTES);
            if (!lock) {
                return;
            }
            try {
                List<RobotOrder> list = robotOrderService.list(Wrappers.lambdaQuery(RobotOrder.class)
                        .eq(RobotOrder::getStatus, Boolean.TRUE)
                        .le(RobotOrder::getNext_bet_time, System.currentTimeMillis() / 1000)
                );
                if (CollectionUtils.isEmpty(list)) {
                    return;
                }

                KlineDirectionEnum res = ThreadLocalRandom.current().nextInt(0, 100) % 2 == 0 ? KlineDirectionEnum.fall : KlineDirectionEnum.rise;
                for (RobotOrder robotOrder : list) {
                    Long uid = robotOrder.getUid();
                    Integer count = robotOrder.getCount();
                    if (Objects.isNull(uid) || count <= 0) {
                        robotService.stop(uid);
                        continue;
                    }

                    Currency currency = currencyService.get(uid, CurrencyTypeEnum.normal);
                    DiscountCurrency discountCurrency = discountCurrencyService.findById(uid);
                    LoanCurrency loanCurrency = loanCurrencyService.findByUid(uid, CurrencyCoinEnum.usdt);
                    BigDecimal remain = TokenCurrencyType.usdt_omni._money(currency.getRemain()).add(TokenCurrencyType.usdt_omni._money(discountCurrency.getBalance())).add(loanCurrency.getBalance());
                    if (remain.compareTo(robotOrder.getAmount()) < 0) {
                        robotService.stop(uid);
                        continue;
                    }

                    requestInitService.init(new RequestInit());
                    requestInitService.setUid(uid);
                    BetDTO betDTO = new BetDTO();
                    betDTO.setAmount(robotOrder.getAmount());
                    betDTO.setBetType(BetTypeEnum.normal);
                    betDTO.setBetSymbol(robotOrder.getCoin());
                    List<BetDuration> betDurations = ruleConfigService.selectAll();
                    betDTO.setBetTime(betDurations.stream().map(BetDuration::getDuration).mapToDouble(Double::doubleValue).min().orElse(0.5));
                    betDTO.setBetDirection(res);
                    try {
                        log.info("执行订单: {}", new Gson().toJson(robotOrder));
                        Long orderId = robotService.autoBet(betDTO, robotOrder);
                        RobotResult robotResult = robotResultService.getOne(Wrappers.lambdaQuery(RobotResult.class).eq(RobotResult::getBet_index, orderId));
                        if (ObjectUtil.isNotNull(robotResult)) {
                            WebSocketUtils.convertAndSend(uid.toString(), WebSocketMsg.getWebSocketMsg(WebSocketMsgTypeEnum.robotBet_refresh, RobotBetVo.getRobotBetVo(orderId, robotResult.getBet_result())));
                        }
                    } catch (Exception e) {
                        log.warn("自动押注机器人订单出现异常 ==> id:{}, uid:{}", robotOrder.getId(), uid, e);
                    }
                }

            } catch (Exception ex) {
                log.error("机器人任务异常! ", ex);
            } finally {
                redisLock.unlock(LOCK_KEY);
            }
        });
    }

    /**
     * 停止机器人下单次数的恢复程序
     */
//    @Scheduled(cron = "10 0 0 * * ? ")
    public void reset() {
        asyncService.async(() -> {
            boolean lock = redisLock._lock(LOCK_KEY + ":reset", 10L, TimeUnit.MINUTES);
            if (!lock) {
                return;
            }
            userService.resetAutoCount();
        });
    }


}