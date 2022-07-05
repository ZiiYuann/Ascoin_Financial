package com.tianli.currency_token.order.schedule;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tianli.common.async.AsyncService;
import com.tianli.common.lock.RedisLock;
import com.tianli.currency_token.mapper.TokenOrderType;
import com.tianli.currency_token.order.CurrencyTokenOrderService;
import com.tianli.currency_token.order.TokenDealService;
import com.tianli.currency_token.order.mapper.CurrencyTokenOrder;
import com.tianli.currency_token.order.mapper.CurrencyTokenOrderStatus;
import com.tianli.exchange.enums.PlatformTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class OrderSchedule {

    @Scheduled(fixedDelay = 1000 * 10)
    public void bianPriceSync() {
        asyncService.async(() -> {
            boolean lock = redisLock._lock("OrderSchedule:bianPriceSync", 3L, TimeUnit.MINUTES);
            if (!lock) {
                return;
            }
            try {
                tokenDealService.syncBianPrice();
            } catch (Exception e) {
                log.error("bianPriceSync Exception:", e);
            } finally {
                redisLock.unlock("OrderSchedule:bianPriceSync");
            }
        });
    }

    @Scheduled(fixedDelay = 500)
    public void tradeToken() {
        asyncService.async(() -> {
            boolean lock = redisLock._lock("OrderSchedule:tradeToken", 3L, TimeUnit.MINUTES);
            if (!lock) {
                return;
            }
            try {
                List<CurrencyTokenOrder> createdList = currencyTokenOrderService.list(
                        new LambdaQueryWrapper<CurrencyTokenOrder>()
                                .eq(CurrencyTokenOrder::getStatus, CurrencyTokenOrderStatus.created)
                                .ne(CurrencyTokenOrder::getPlatform_type, PlatformTypeEnum.own)
                );
                for (CurrencyTokenOrder currencyTokenOrder : createdList) {
                    if (currencyTokenOrder.getType().equals(TokenOrderType.limit)) {
                        currencyTokenOrderService.tradeLimit(currencyTokenOrder);
                    }
                }
            } catch (Exception e) {
                log.error("bianPriceSync Exception:", e);
            } finally {
                redisLock.unlock("OrderSchedule:tradeToken");
            }
        });
    }

    @Resource
    private AsyncService asyncService;
    @Resource
    private RedisLock redisLock;
    @Resource
    private TokenDealService tokenDealService;
    @Resource
    private CurrencyTokenOrderService currencyTokenOrderService;

}
