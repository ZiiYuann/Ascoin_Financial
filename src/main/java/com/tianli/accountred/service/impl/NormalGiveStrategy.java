package com.tianli.accountred.service.impl;

import com.tianli.accountred.entity.RedEnvelope;
import com.tianli.accountred.entity.RedEnvelopeConfig;
import com.tianli.accountred.entity.RedEnvelopeSpilt;
import com.tianli.accountred.enums.RedEnvelopeChannel;
import com.tianli.accountred.enums.RedEnvelopeType;
import com.tianli.accountred.service.RedEnvelopeConfigService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2022-10-17
 **/
@Component
public class NormalGiveStrategy extends RedEnvelopeGiveStrategy implements InitializingBean {

    @Override
    protected List<RedEnvelopeSpilt> spiltRedEnvelopeOperation(Long rid, int num, BigDecimal amount) {

        List<RedEnvelopeSpilt> spiltRedEnvelopes = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            RedEnvelopeSpilt redEnvelopeSpilt = super.generatorRedEnvelopeSpilt(rid);
            redEnvelopeSpilt.setAmount(amount);
            spiltRedEnvelopes.add(redEnvelopeSpilt);
        }
        return spiltRedEnvelopes;
    }

    @Override
    protected List<RedEnvelopeSpilt> spiltRedEnvelopeOperation(RedEnvelope redEnvelope) {
        return this.spiltRedEnvelopeOperation(redEnvelope.getId(), redEnvelope.getNum(), redEnvelope.getAmount());
    }

    @Override
    public void afterPropertiesSet() {
        GiveStrategyAdapter.addStrategy(RedEnvelopeChannel.CHAT, RedEnvelopeType.NORMAL, this);
        GiveStrategyAdapter.addStrategy(RedEnvelopeChannel.CHAT, RedEnvelopeType.PRIVATE, this);
    }
}
