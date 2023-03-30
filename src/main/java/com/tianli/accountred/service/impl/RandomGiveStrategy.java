package com.tianli.accountred.service.impl;

import com.google.common.base.MoreObjects;
import com.tianli.accountred.entity.RedEnvelope;
import com.tianli.accountred.entity.RedEnvelopeConfig;
import com.tianli.accountred.entity.RedEnvelopeSpilt;
import com.tianli.accountred.enums.RedEnvelopeChannel;
import com.tianli.accountred.service.RedEnvelopeConfigService;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 随机策略
 *
 * @author chenb
 * @apiNote
 * @since 2022-10-17
 **/
public class RandomGiveStrategy extends RedEnvelopeGiveStrategy {

    @Resource
    private RedEnvelopeConfigService redEnvelopeConfigService;

    private static final BigDecimal LIMIT_AMOUNT = BigDecimal.valueOf(0.000001);

    protected List<RedEnvelopeSpilt> spiltRedEnvelopeOperation(Long rid, int count, BigDecimal amount, BigDecimal limitAmount) {
        return this.spiltRedEnvelopeOperation(rid, count, amount, limitAmount, 6);
    }

    /**
     * 随机红包拆分操作
     *
     * @param rid         红包id
     * @param count       数量
     * @param amount      总金额
     * @param limitAmount 最低红包金额
     * @return 拆分红包
     */
    protected List<RedEnvelopeSpilt> spiltRedEnvelopeOperation(Long rid, int count, BigDecimal amount, BigDecimal limitAmount, int scale) {
        BigDecimal num = new BigDecimal(count);
        List<RedEnvelopeSpilt> redEnvelopes = new ArrayList<>();
        // 最小金额
        limitAmount = MoreObjects.firstNonNull(limitAmount, LIMIT_AMOUNT);
        BigDecimal remain = amount.subtract(limitAmount.multiply(num));
        final Random random = new Random();
        final BigDecimal hundred = new BigDecimal("100");
        final BigDecimal two = new BigDecimal("2");
        BigDecimal singleAmount;
        for (int i = 0; i < num.intValue(); i++) {
            RedEnvelopeSpilt redEnvelopeSpilt = super.generatorRedEnvelopeSpilt(rid);
            final int nextInt = random.nextInt(100);
            if (i == num.intValue() - 1) {
                singleAmount = remain;
            } else {
                singleAmount = new BigDecimal(nextInt).multiply(remain.multiply(two)
                                .divide(num.subtract(new BigDecimal(i)), scale, RoundingMode.CEILING))
                        .divide(hundred, scale, RoundingMode.FLOOR);
            }


            if (remain.compareTo(singleAmount) > 0) {
                remain = remain.subtract(singleAmount);
            } else {
                remain = BigDecimal.ZERO;
            }

            redEnvelopeSpilt.setAmount(limitAmount.add(singleAmount));
            redEnvelopes.add(redEnvelopeSpilt);

        }
        return redEnvelopes;
    }

    @Override
    protected List<RedEnvelopeSpilt> spiltRedEnvelopeOperation(Long rid, int num, BigDecimal amount) {
        return this.spiltRedEnvelopeOperation(rid, num, amount, null);
    }

    @Override
    protected List<RedEnvelopeSpilt> spiltRedEnvelopeOperation(RedEnvelope redEnvelope) {
        RedEnvelopeConfig one = redEnvelopeConfigService.getOne(redEnvelope.getCoin(), RedEnvelopeChannel.CHAT);
        return this.spiltRedEnvelopeOperation(redEnvelope.getId(), redEnvelope.getNum()
                , redEnvelope.getAmount(), one.getMinAmount(), one.getScale());
    }

}
