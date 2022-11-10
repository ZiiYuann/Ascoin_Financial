package com.tianli.accountred.service.impl;

import cn.hutool.core.lang.UUID;
import com.tianli.accountred.entity.RedEnvelopeSpilt;
import com.tianli.exception.ErrorCodeEnum;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * @author chenb
 * @apiNote
 * @since 2022-10-17
 **/
public abstract class RedEnvelopeGiveStrategy {

    public List<RedEnvelopeSpilt> spiltRedEnvelope(Long rid, int num, BigDecimal amount,BigDecimal totalAmount) {

        if (num < 1 || BigDecimal.ZERO.compareTo(amount) >= 0 || Objects.isNull(rid)) {
            ErrorCodeEnum.ARGUEMENT_ERROR.throwException();
        }

        List<RedEnvelopeSpilt> redEnvelopes = this.spiltRedEnvelopeOperation(rid, num, amount);

        if (redEnvelopes.size() != num || redEnvelopes.stream().map(RedEnvelopeSpilt::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add).compareTo(totalAmount) != 0) {
            ErrorCodeEnum.throwException("红包生成有误");
        }

        return redEnvelopes;
    }

    protected abstract List<RedEnvelopeSpilt> spiltRedEnvelopeOperation(Long rid, int num, BigDecimal amount);

    protected RedEnvelopeSpilt generatorRedEnvelopeSpilt(Long rid) {
        return RedEnvelopeSpilt.builder()
                .rid(rid)
                .id(UUID.fastUUID().toString(true))
                .build();
    }
}

