package com.tianli.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.chain.enums.TransactionStatus;
import com.tianli.chain.service.contract.ContractAdapter;
import com.tianli.charge.entity.Order;
import com.tianli.charge.entity.OrderChargeInfo;
import com.tianli.charge.entity.OrderReview;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.service.OrderChargeInfoService;
import com.tianli.charge.service.OrderReviewService;
import com.tianli.charge.service.OrderService;
import com.tianli.common.RedisConstants;
import com.tianli.common.RedisLockConstants;
import com.tianli.common.webhook.WebHookService;
import com.tianli.common.webhook.WebHookToken;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.mconfig.ConfigService;
import com.tianli.tool.crypto.Crypto;
import com.tianli.tool.time.TimeTool;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.crypto.util.DigestFactory;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.tianli.common.ConfigConstants.SYSTEM_URL_PATH_PREFIX;

/**
 * @author chenb
 * @apiNote
 * @since 2022-09-29
 **/
@Component
public class WithdrawOrderTask {

    @Resource
    private OrderService orderService;
    @Resource
    private WebHookService webHookService;
    @Resource
    private OrderChargeInfoService orderChargeInfoService;
    @Resource
    private ContractAdapter contractAdapter;
    @Resource
    private OrderReviewService orderReviewService;
    @Resource
    private ConfigService configService;
    @Resource
    private RedissonClient redissonClient;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Scheduled(cron = "0 0/15 * * * ?")
    public void withdrawTask() {
        RLock lock = redissonClient.getLock(RedisLockConstants.ORDER_WITHDRAW);
        try {
            if (lock.tryLock(3, TimeUnit.SECONDS)) {
                LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<Order>()
                        .eq(Order::getType, ChargeType.withdraw)
                        .eq(Order::getStatus, ChargeStatus.chaining);

                List<Order> orders = orderService.list(queryWrapper);
                orders.forEach(this::operation);
            }
        } catch (InterruptedException e) {
            // Restore interrupted state...
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public void operation(Order order) {

        Long relatedId = order.getRelatedId();
        OrderChargeInfo orderChargeInfo = orderChargeInfoService.getById(relatedId);
        if (Objects.isNull(orderChargeInfo) || StringUtils.isBlank(orderChargeInfo.getTxid())) {
            webHookService.dingTalkSend(order.getOrderNo() + "提现异常，不存在链信息，或者不存在txid" + order.getOrderNo());
            return;
        }

        Long reviewerId = order.getReviewerId();
        OrderReview orderReview = orderReviewService.getById(reviewerId);
        if (Objects.isNull(orderReview)) {
            webHookService.dingTalkSend(order.getOrderNo() + "提现异常，未找到审核记录:" + order.getOrderNo());
            return;
        }

        // 防止整点审核导致异常
        if (orderReview.getCreateTime().until(LocalDateTime.now(), ChronoUnit.MINUTES) < 15) {
            return;
        }

        String txid = orderChargeInfo.getTxid();
        TransactionStatus transactionStatus = contractAdapter.getOne(orderChargeInfo.getNetwork()).successByHash(txid);

        String key = RedisConstants.WITHDRAW_ORDER_TASK + order.getOrderNo();
        String value = stringRedisTemplate.opsForValue().get(key);
        LocalDateTime now = LocalDateTime.now();
        String nowTime = TimeTool.getDateTimeDisplayString(now);
        if (StringUtils.isNotBlank(value)) {
            return;
        }

        stringRedisTemplate.opsForValue().set(key, nowTime, 24, TimeUnit.HOURS);

        if (TransactionStatus.SUCCESS.equals(transactionStatus)) {
            webHookService.dingTalkSend(txid + " 提现异常，此订单交易成功，但是订单状态未修改，请及时排除问题", WebHookToken.PRO_BUG_PUSH);

        }

        if (TransactionStatus.PENDING.equals(transactionStatus)) {
            webHookService.dingTalkSend(txid + " 提现状态PENDING，请校验是否正常", WebHookToken.PRO_BUG_PUSH);

        }

        if (TransactionStatus.FAIL.equals(transactionStatus)) {
            String urlPre = configService.getOrDefault(SYSTEM_URL_PATH_PREFIX, "https://www.assureadd.com");
            String timestamp = System.currentTimeMillis() + "";
            String sign = Crypto.hmacToString(DigestFactory.createSHA256(), "VxaVdCoah9kZSCMdxAgMBAAE", timestamp);
            String url = urlPre + "/api/management/financial/wallet/chainFail/confirm?" +
                    "orderNo=" + order.getOrderNo()
                    + "&sign=" + sign
                    + "&timestamp=" + timestamp;
            webHookService.dingTalkSend(txid + " 提现失败，确认失败后，请在24小时内通过此链接确认失败：" + url, WebHookToken.PRO_BUG_PUSH);

        }

        if (transactionStatus == null) {
            throw ErrorCodeEnum.SYSTEM_ERROR.generalException();
        }
    }


}
