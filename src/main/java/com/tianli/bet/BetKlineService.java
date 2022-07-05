package com.tianli.bet;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.gson.Gson;
import com.tianli.bet.mapper.*;
import com.tianli.blocklist.BlackUserListService;
import com.tianli.blocklist.mapper.BlackUserList;
import com.tianli.common.Constants;
import com.tianli.kline.FollowCurrencyService;
import com.tianli.kline.KLineService;
import com.tianli.kline.mapper.FollowCurrency;
import com.tianli.kline.task.Stat;
import com.tianli.management.directioanalconfig.mapper.DirectionalConfig;
import com.tianli.management.directioanalconfig.service.DirectionalConfigService;
import com.tianli.robot.RobotCouponService;
import com.tianli.robot.RobotResultService;
import com.tianli.robot.mapper.RobotResult;
import com.tianli.user.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
public class BetKlineService{

    @Resource
    private KLineService kLineService;

    @Resource
    private DirectionalConfigService directionalConfigService;

    @Resource
    private BlackUserListService blackUserListService;

    @Resource
    private FollowCurrencyService followCurrencyService;

    @Resource
    private BetMapper betMapper;

    @Resource
    private BetUserLineService betUserLineService;

    @Resource
    private UserService userService;

    @Resource
    private RobotResultService robotResultService;

    @Resource
    private RobotCouponService robotCouponService;

    public KLineDirectionResult directionAnalysis(Bet bet, LocalDateTime expectedResultTime){
        log.info("id:{}, robot:{}, 订单解析结算价格", bet.getId(), bet.getOrder_type());
        Double end_exchange_rate;
        Double start_exchange_rate = bet.getStart_exchange_rate();
        // 提前已经知道结果的押注订单;
        if(Objects.nonNull(end_exchange_rate = bet.getProphecy_end_exchange_rate())){
            log.info("id:{}, robot:{}, 是提前知道走势的订单! 最终k值:{}", bet.getId(), bet.getOrder_type(), end_exchange_rate);
            // 异步更新当前用户的单独kline数据, 为了让用户重新登录后可以看到和之前一样的kline走势
            CompletableFuture.runAsync(() -> {
                Gson gson = new Gson();
                boolean toSuccess = true;
                int count = 0;
                    do {
                        count++;
                        BetUserLine byId = betUserLineService.getOne(new LambdaQueryWrapper<BetUserLine>()
                                .eq(BetUserLine::getUid, bet.getUid())
                                .eq(BetUserLine::getSymbol, bet.getBet_symbol()));
                        BetUserLineObj userLine;
                        boolean update;
                        final Double final_start_exchange_rate = bet.getSlip_difference();
                        if (Objects.nonNull(byId)) {
                            String line_json = byId.getLine_json();
                            userLine = gson.fromJson(line_json, BetUserLineObj.class);
                            userLine.update(final_start_exchange_rate, expectedResultTime);
                            update = betUserLineService.update(new LambdaUpdateWrapper<BetUserLine>()
                                    .set(BetUserLine::getVersion, byId.getVersion() + 1)
                                    .set(BetUserLine::getLine_json, gson.toJson(userLine))
                                    .eq(BetUserLine::getId, byId.getId())
                                    .eq(BetUserLine::getVersion, byId.getVersion())
                            );
                        } else {
                            userLine = BetUserLineObj.build();
                            userLine.update(final_start_exchange_rate, expectedResultTime);
                            BetUserLine build = BetUserLine.builder()
                                    .uid(bet.getUid())
                                    .symbol(bet.getBet_symbol())
                                    .line_json(gson.toJson(userLine))
                                    .version(1)
                                    .build();
                            update = betUserLineService.save(build);
                        }

                        if (update) {
                            toSuccess = false;
                        }
                    } while (toSuccess && count < 3);
            }, Constants.COMPLETABLE_FUTURE_EXECUTOR);
            double cpVal = end_exchange_rate - start_exchange_rate;
            return KLineDirectionResult.success(cpVal > 0 ? KlineDirectionEnum.rise : (cpVal < 0 ? KlineDirectionEnum.fall : KlineDirectionEnum.flat), start_exchange_rate, end_exchange_rate);
        }
        String klineType = bet.getBet_symbol();
        /*
         * 币安价格
         */
        Stat stat = kLineService.crawlCurrentBianPrice(klineType);
        if(Objects.isNull(stat)){
            return KLineDirectionResult.fail();
        }
        end_exchange_rate = stat.getClose();

        if("robot".equalsIgnoreCase(bet.getOrder_type())){
            log.info("id:{}, robot:{}, 是机器人订单!", bet.getId(), bet.getOrder_type());
            RobotResult robotResult = robotResultService.getOne(Wrappers.lambdaQuery(RobotResult.class)
                    .eq(RobotResult::getBet_index, bet.getId()));
            log.info("id:{}, robot:{}, 获取机器人res:{}!", bet.getId(), bet.getOrder_type(), new Gson().toJson(robotResult));
            boolean res;
            if (Objects.isNull(robotResult)){
                Double win_rate = 0.8;
                double nextDouble = ThreadLocalRandom.current().nextDouble(0, 1);
                res = nextDouble > win_rate;
            }else{
                res = robotResult.getBet_result() == 0;
            }
            Double low = stat.getLow();
            Double high = stat.getHigh();
            Double betRate = bet.getStart_exchange_rate();
            if(res){
                // SHU
                if (bet.getBet_direction() == KlineDirectionEnum.rise) {
                    // 上升 -> 下降
                    if(betRate > low){
                        if (betRate-0.01 > low)
                            end_exchange_rate = ThreadLocalRandom.current().nextDouble(low, betRate-0.01);
                        else
                            end_exchange_rate = low;

                    }else{
                        end_exchange_rate = betRate - 0.01;
                    }
                } else {
                    // 下降 -> 上升
                    if(betRate < high){
                        end_exchange_rate = ThreadLocalRandom.current().nextDouble(betRate, high);
                    }else{
                        end_exchange_rate = betRate + 0.01;
                    }
                }
            }else{
                // YING
                if (bet.getBet_direction() == KlineDirectionEnum.rise) {
                    // 上升 -> 上升
                    if(betRate >= high){
                        end_exchange_rate = betRate + 0.01;
                    }else{
                        end_exchange_rate = ThreadLocalRandom.current().nextDouble(betRate, high);
                    }
                } else {
                    // 下降 -> 下降
                    if(betRate <= low){
                        end_exchange_rate = betRate - 0.01;
                    }else{
                        if (low < betRate-0.01)
                            end_exchange_rate = ThreadLocalRandom.current().nextDouble(low, betRate-0.01);
                        else
                            end_exchange_rate = low;
                    }
                }
            }
            double cpVal = end_exchange_rate - start_exchange_rate;
            KLineDirectionResult success = KLineDirectionResult.success(cpVal > 0 ? KlineDirectionEnum.rise : (cpVal < 0 ? KlineDirectionEnum.fall : KlineDirectionEnum.flat), start_exchange_rate, end_exchange_rate);
            log.info("id:{}, robot:{}, 是机器人订单! 最终价格:{}, 最终结果:{}", bet.getId(), bet.getOrder_type(), end_exchange_rate, new Gson().toJson(success));
            return success;
        }
        // 查看当前用户配置
        Long uid = bet.getUid();
//        LocalDateTime now = LocalDateTime.now();
//        String bet_symbol = bet.getBet_symbol();
//        String symbol = bet_symbol.replaceAll("usdt", "/usdt").toUpperCase();
//        DirectionalConfig one = directionalConfigService.getOne(Wrappers.lambdaQuery(DirectionalConfig.class)
//                .eq(DirectionalConfig::getUid, uid)
//                .eq(DirectionalConfig::getCurrency_type, symbol)
//                .ge(DirectionalConfig::getStart_time, now)
//                .le(DirectionalConfig::getEnd_time, now)
//                .last(" LIMIT 1")
//        );
        BlackUserList one = blackUserListService.getOne(Wrappers.lambdaQuery(BlackUserList.class)
                .eq(BlackUserList::getUid, uid)
                .last("LIMIT 1"));
        KlineDirectionEnum bet_direction = bet.getBet_direction();
//        if (Objects.nonNull(one)) {
//            // 之前版本的case
//            end_exchange_rate = getResultByUserConfig(start_exchange_rate, bet_direction, stat, one, end_exchange_rate);
//        } else {
//            end_exchange_rate = getResultByGlobalConfig(symbol, start_exchange_rate, bet_direction, stat);
//        }
        if (Objects.nonNull(one)) {
            end_exchange_rate = getLoseEndRate(start_exchange_rate, bet_direction, stat, one);
        }

        double cpVal = end_exchange_rate - start_exchange_rate;
        return KLineDirectionResult.success(cpVal > 0 ? KlineDirectionEnum.rise : (cpVal < 0 ? KlineDirectionEnum.fall : KlineDirectionEnum.flat), start_exchange_rate, end_exchange_rate);
    }

    private Double getLoseEndRate(Double start_exchange_rate, KlineDirectionEnum bet_direction, Stat stat, BlackUserList one) {
        if (one.getControl() == 0){
            switch (bet_direction){
                case rise:
                    Double low = stat.getLow();
                    if (low >= start_exchange_rate){
                        return start_exchange_rate * 0.98;
                    }else {
                        return low;
                    }
                case fall:
                    Double high = stat.getHigh();
                    if (high > start_exchange_rate){
                        return high;
                    }else {
                        return start_exchange_rate * 1.02;
                    }
            }
        }else if (one.getControl() == 1){
            switch (bet_direction){
                case rise:
                    Double high2 = stat.getHigh();
                    if (high2 > start_exchange_rate){
                        return high2;
                    }else {
                        return start_exchange_rate * 1.02;
                    }
                case fall:
                    Double low2 = stat.getLow();
                    if (low2 < start_exchange_rate){
                        return low2;
                    }else {
                        return start_exchange_rate * 0.98;
                    }
            }
        }

        return stat.getClose();
    }

    /**
     * 开始押注汇率在K线数据之间时才做处理(按照规则进行相应的控制)
     *
     * @param symbol 交易对
     * @param start_exchange_rate 押注时的税率
     * @param bet_direction 押注方向
     * @param stat 当前的K线数据
     * @return 处理后的K线收盘价
     */
    private Double getResultByGlobalConfig(String symbol, Double start_exchange_rate, KlineDirectionEnum bet_direction, Stat stat) {
        Double close = stat.getClose();
        double high;
        double low;
        if((low = stat.getLow()) > start_exchange_rate || start_exchange_rate > (high = stat.getHigh())){
            return close;
        }

        // 拿到全局的当前交易对的信息
        FollowCurrency one = followCurrencyService.getOne(new LambdaQueryWrapper<FollowCurrency>().eq(FollowCurrency::getName, symbol));
        double expectedWinningRate;
        if(Objects.isNull(one) || one.getWin_rate() < 0 || (expectedWinningRate = one.getWin_rate()) > 1){
            return close;
        }

        // 判断过往的押注胜率和期望胜率的差值作相应的处理
        double winningProbability = betMapper.selectProportion(symbol.replaceAll("/","").toLowerCase(), BetResultEnum.win);
        ThreadLocalRandom current = ThreadLocalRandom.current();
        if(expectedWinningRate > winningProbability){
            // 尽力把订单处理成win
            if(Objects.equals(bet_direction, KlineDirectionEnum.rise)){
                if(close > start_exchange_rate){
                    return close;
                }else{
                    return current.nextDouble(close, high);
                }
            }else{
                if(close >= start_exchange_rate){
                    return current.nextDouble(low, close);
                }else{
                    return close;
                }
            }
        }else{
            // 尽力把订单处理成lose
            if(Objects.equals(bet_direction, KlineDirectionEnum.fall)){
                if(close > start_exchange_rate){
                    return close;
                }else{
                    return current.nextDouble(close, high);
                }
            }else{
                if(close >= start_exchange_rate){
                    return current.nextDouble(low, close);
                }else{
                    return close;
                }
            }
        }
    }

    private Double getResultByUserConfig(Double start_exchange_rate, KlineDirectionEnum bet_direction, Stat stat, DirectionalConfig one, Double end_exchange_rate) {
        BetResultEnum result = one.getResult();
        if (Objects.equals(BetResultEnum.win, result)) {
            //保证用户赢
            switch (bet_direction) {
                case rise:
                    // 用户压涨, end_exchange_rate >= start_exchange_rate
                    if (end_exchange_rate < start_exchange_rate) {
                        if (stat.getHigh() > start_exchange_rate) {
                            end_exchange_rate = ThreadLocalRandom.current().nextDouble(start_exchange_rate, stat.getHigh());
                        } else {
                            end_exchange_rate = start_exchange_rate + 0.01;
                        }
                    }
                    break;
                case fall:
                    // 用户压跌, end_exchange_rate < start_exchange_rate
                    if (end_exchange_rate >= start_exchange_rate) {
                        if (stat.getLow() < start_exchange_rate) {
                            end_exchange_rate = ThreadLocalRandom.current().nextDouble(stat.getLow(), start_exchange_rate - 0.01);
                        } else {
                            end_exchange_rate = start_exchange_rate - 0.01;
                        }
                    }
                    break;
            }
        } else if (Objects.equals(BetResultEnum.lose, result)) {
            //保证用户输
            switch (bet_direction) {
                case rise:
                    // 用户压涨, end_exchange_rate < start_exchange_rate
                    if (end_exchange_rate >= start_exchange_rate) {
                        if (stat.getLow() < start_exchange_rate) {
                            end_exchange_rate = ThreadLocalRandom.current().nextDouble(stat.getLow(), start_exchange_rate - 0.01);
                        } else {
                            end_exchange_rate = start_exchange_rate - 0.01;
                        }
                    }
                    break;
                case fall:
                    // 用户压跌, end_exchange_rate >= start_exchange_rate
                    if (end_exchange_rate < start_exchange_rate) {
                        if (stat.getHigh() > start_exchange_rate) {
                            end_exchange_rate = ThreadLocalRandom.current().nextDouble(start_exchange_rate, stat.getHigh());
                        } else {
                            end_exchange_rate = start_exchange_rate + 0.01;
                        }
                    }
                    break;
            }
        }
        return end_exchange_rate;
    }
}
