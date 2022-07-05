package com.tianli.kline.task;

import com.tianli.bet.mapper.Bet;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.kline.KLineConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.ZoneOffset;
import java.util.Objects;

@Slf4j
@Component
public class FastStatBetAmount {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 处理统计押注数据
     * 缓存各个维度到redis
     */
    public void handleBetStatData(Bet bet) {
        try {
            BetStatItem betStatItem = BetStatItem.trans(bet);
            // 加载获取队列统计数据进行计算 HuoBiPeriodEnum
            if (Objects.isNull(betStatItem) || betStatItem.getTime() <= 0 || betStatItem.getAmount() <= 0) {
                return;
            }
            HuoBiPeriodEnum.incrementAllPipelined(redisTemplate, KLineConstants.STAT_DATA_CACHE_KEY, betStatItem.getTime(), betStatItem.getAmount());
        } catch (Exception e) {
            // do nothing
            log.warn("\n 定时收集远程数据任务异常!! ");
        }
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    static class BetStatItem {
        /**
         * 下注的时间戳
         */
        private long time;
        /**
         * 下注的金额
         */
        private double amount;

        public static BetStatItem trans(Bet bet) {
            return BetStatItem.builder()
                    .time(bet.getCreate_time().toInstant(ZoneOffset.of("+8")).toEpochMilli())
                    .amount(TokenCurrencyType.usdt_omni.money(bet.getAmount()))
                    .build();
        }
    }
}
