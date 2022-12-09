package com.tianli.charge.service;

import com.tianli.chain.entity.CoinReviewConfig;
import com.tianli.chain.service.CoinReviewConfigService;
import com.tianli.charge.entity.Order;
import com.tianli.charge.entity.OrderChargeInfo;
import com.tianli.charge.enums.OrderReviewStrategy;
import com.tianli.currency.service.CurrencyService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;

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

    /**
     * 获取提现审核策略
     *
     * @param order           订单信息
     * @param orderChargeInfo 订单详情信息
     * @return 策略
     */
    public OrderReviewStrategy getStrategy(Order order, OrderChargeInfo orderChargeInfo) {
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

        return OrderReviewStrategy.AUTO_REVIEW_AUTO_TRANSFER;
    }

}
