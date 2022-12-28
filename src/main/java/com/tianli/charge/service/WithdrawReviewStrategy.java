package com.tianli.charge.service;

import com.tianli.chain.entity.CoinReviewConfig;
import com.tianli.chain.service.CoinReviewConfigService;
import com.tianli.charge.entity.Order;
import com.tianli.charge.entity.OrderChargeInfo;
import com.tianli.charge.enums.OrderReviewStrategy;
import com.tianli.common.RedisConstants;
import com.tianli.currency.service.CurrencyService;
import com.tianli.exception.ErrorCodeEnum;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2022-12-09
 **/
@Service
public class WithdrawReviewStrategy {

    @Resource
    private CoinReviewConfigService coinReviewConfigService;
    @Resource
    private CurrencyService currencyService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    public OrderReviewStrategy getStrategy(Order order, OrderChargeInfo orderChargeInfo) {
        return this.getStrategy(order, orderChargeInfo, false);
    }

    /**
     * 获取提现审核策略
     *
     * @param order           订单信息
     * @param orderChargeInfo 订单详情信息
     * @param apply           是否申请
     * @return 策略
     */
    public OrderReviewStrategy getStrategy(Order order, OrderChargeInfo orderChargeInfo, boolean apply) {
        BigDecimal withdrawAmountDollar = orderChargeInfo.getFee().multiply(currencyService.huobiUsdtRate(order.getCoin()));
        CoinReviewConfig coinReviewConfig = coinReviewConfigService.reviewConfig();

        // 人工审核人工打币下限
        BigDecimal manualReviewManualTransfer = BigDecimal.valueOf(coinReviewConfig.getManualReviewManualTransfer());
        if (withdrawAmountDollar.compareTo(manualReviewManualTransfer) > 0) {
            return OrderReviewStrategy.MANUAL_REVIEW_MANUAL_TRANSFER;
        }

        BigDecimal autoReviewAutoTransfer = BigDecimal.valueOf(coinReviewConfig.getAutoReviewAutoTransfer());
        // 人工审核自动打币上限
        if (withdrawAmountDollar.compareTo(autoReviewAutoTransfer) > 0) {
            return OrderReviewStrategy.MANUAL_REVIEW_AUTO_TRANSFER;
        }

        if (apply) {
            String key = RedisConstants.USER_WITHDRAW_LIMIT + order.getUid();
            // 当前时间
            String timestamp = System.currentTimeMillis() + "";
            String expireSeconds = coinReviewConfig.getHourLimit() * 60 * 60 + "";
            String timesLimit = coinReviewConfig.getTimesLimit() + "";

            // 使用redis ZSET 实现滑动窗口算法
            String script = // 获取key
                    "local key = KEYS[1] \n" +
                            // 缓存时间 vale1
                            "local expire = tonumber(ARGV[1]) \n" +
                            // 当前时间 value2
                            "local currentMs = tonumber(ARGV[2]) \n" +
                            // 最大次数 value3
                            "local count = tonumber(ARGV[3]) \n" +
                            // 参数 value4
                            "local value = ARGV[4] \n" +
                            // 窗口开始时间
                            "local windowStartMs = currentMs - expire * 1000 \n" +
                            // 获取key的次数
                            "local current = redis.call('ZCOUNT', key, windowStartMs, currentMs) \n" +
                            // 如果key的次数存在且大于预设值直接返回当前key的次数
                            "if current and tonumber(current) >= count then\n" +
                            "    return tostring(current)\n" +
                            "end \n" +
                            // 清除所有过期成员
                            "redis.call('ZREMRANGEBYSCORE', key, 0, windowStartMs) \n" +
                            // 添加当前成员
                            "redis.call('ZADD', key, currentMs, value) \n" +
                            // 设置过期时间
                            "redis.call('EXPIRE', key, expire) \n" +
                            // 返回当前次数
                            "return tostring(current) ";

            DefaultRedisScript<String> redisScript = new DefaultRedisScript<>();
            redisScript.setResultType(String.class);
            redisScript.setScriptText(script);
            //采用字符串序列化器
            RedisSerializer<String> stringRedisSerializer = redisTemplate.getStringSerializer();
            Object[] objects = new Object[]{expireSeconds, timestamp, timesLimit,order.getOrderNo()};
            String result = redisTemplate.execute(redisScript
                    , stringRedisSerializer
                    , stringRedisSerializer
                    , List.of(key), objects);
            if (StringUtils.isBlank(result)) {
                ErrorCodeEnum.SYSTEM_ERROR.throwException();
            }
            if (Integer.parseInt(result) >= Integer.parseInt(timesLimit)) {
                return OrderReviewStrategy.MANUAL_REVIEW_AUTO_TRANSFER;
            }
            return OrderReviewStrategy.AUTO_REVIEW_AUTO_TRANSFER;
        }

        return OrderReviewStrategy.MANUAL_REVIEW_AUTO_TRANSFER;
    }

}


