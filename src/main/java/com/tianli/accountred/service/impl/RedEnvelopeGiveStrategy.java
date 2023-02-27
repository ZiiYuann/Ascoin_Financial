package com.tianli.accountred.service.impl;

import cn.hutool.core.lang.UUID;
import com.tianli.accountred.entity.RedEnvelope;
import com.tianli.accountred.entity.RedEnvelopeSpilt;
import com.tianli.exception.ErrorCodeEnum;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/**
 * @author chenb
 * @apiNote
 * @since 2022-10-17
 **/
public abstract class RedEnvelopeGiveStrategy {

    /**
     * 拆分红包
     *
     * @param redEnvelope 红包信息
     * @return 拆分红包
     */
    public List<RedEnvelopeSpilt> spiltRedEnvelope(RedEnvelope redEnvelope) {
        var amount = redEnvelope.getAmount();
        var num = redEnvelope.getNum();
        var rid = redEnvelope.getId();
        var totalAmount = redEnvelope.getTotalAmount();
        if (num < 1 || BigDecimal.ZERO.compareTo(amount) >= 0 || Objects.isNull(rid)) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }

        List<RedEnvelopeSpilt> redEnvelopes = this.spiltRedEnvelopeOperation(redEnvelope);

        if (redEnvelopes.size() != num || redEnvelopes.stream().map(RedEnvelopeSpilt::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add).compareTo(totalAmount) != 0) {
            ErrorCodeEnum.throwException("红包生成有误");
        }

        return redEnvelopes;
    }

    /**
     * 具体拆分红包操作
     *
     * @param rid    红包id
     * @param num    数量
     * @param amount 总金额
     * @return 拆分红包
     */
    protected abstract List<RedEnvelopeSpilt> spiltRedEnvelopeOperation(Long rid, int num, BigDecimal amount);

    /**
     * 具体拆分红包操作
     *
     * @param redEnvelope 红包对象
     * @return 拆分红包
     */
    protected abstract List<RedEnvelopeSpilt> spiltRedEnvelopeOperation(RedEnvelope redEnvelope);

    /**
     * 生成拆分红包id
     *
     * @param rid 红包id
     * @return 拆分红包
     */
    protected RedEnvelopeSpilt generatorRedEnvelopeSpilt(Long rid) {
        return RedEnvelopeSpilt.builder()
                .rid(rid)
                .id(UUID.fastUUID().toString(true))
                .build();
    }

    /**
     * 获取平均值70% 且根据小数点
     *
     * @param totalAmount 总金额
     * @param num         数量
     * @param scale       小数点位数
     * @return 平均近似值
     */
    public static BigDecimal getAverageValue70Percent(BigDecimal totalAmount, int num, int scale) {
        return totalAmount
                .divide(new BigDecimal(num), 8, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(0.7))
                .setScale(scale, RoundingMode.HALF_UP);
    }


}

