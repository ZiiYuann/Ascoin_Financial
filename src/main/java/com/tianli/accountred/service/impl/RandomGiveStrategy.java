package com.tianli.accountred.service.impl;

import com.tianli.accountred.entity.RedEnvelopeSpilt;

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

    private static final BigDecimal LIMIT_AMOUNT = BigDecimal.valueOf(0.000001);

    @Override
    protected List<RedEnvelopeSpilt> spiltRedEnvelopeOperation(Long rid, int count, BigDecimal amount) {
        BigDecimal num = new BigDecimal(count);
        List<RedEnvelopeSpilt> redEnvelopes = new ArrayList<>();

        BigDecimal remain = amount.subtract(LIMIT_AMOUNT.multiply(num));
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
                                .divide(num.subtract(new BigDecimal(i)), 6, RoundingMode.CEILING))
                        .divide(hundred, 6, RoundingMode.FLOOR);
            }


            if (remain.compareTo(singleAmount) > 0) {
                remain = remain.subtract(singleAmount);
            } else {
                remain = BigDecimal.ZERO;
            }

            redEnvelopeSpilt.setAmount(LIMIT_AMOUNT.add(singleAmount));
            redEnvelopes.add(redEnvelopeSpilt);

        }
        return redEnvelopes;
    }

}
