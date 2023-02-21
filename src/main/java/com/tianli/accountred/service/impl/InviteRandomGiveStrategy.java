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
import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2022-12-22
 **/
@Component
public class InviteRandomGiveStrategy extends RandomGiveStrategy implements InitializingBean {


    @Resource
    private RedEnvelopeConfigService redEnvelopeConfigService;

    @Override
    protected List<RedEnvelopeSpilt> spiltRedEnvelopeOperation(RedEnvelope redEnvelope) {
        RedEnvelopeConfig config = redEnvelopeConfigService.getOne(redEnvelope.getCoin(), redEnvelope.getChannel());
        int scale = config.getScale();
        BigDecimal averageValue70Percent =
                RedEnvelopeGiveStrategy.getAverageValue70Percent(redEnvelope.getTotalAmount(), redEnvelope.getNum(), scale);
        return super.spiltRedEnvelopeOperation(redEnvelope.getId(), redEnvelope.getNum()
                , redEnvelope.getTotalAmount(), averageValue70Percent, scale);
    }

    @Override
    public void afterPropertiesSet() {
        GiveStrategyAdapter.addStrategy(RedEnvelopeChannel.EXTERN, RedEnvelopeType.RANDOM, this);
    }
}
