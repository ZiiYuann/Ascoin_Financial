package com.tianli.accountred.service.impl;

import com.tianli.accountred.entity.RedEnvelope;
import com.tianli.accountred.entity.RedEnvelopeSpilt;
import com.tianli.accountred.enums.RedEnvelopeChannel;
import com.tianli.accountred.enums.RedEnvelopeType;
import com.tianli.exception.ErrorCodeEnum;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

/**
 * @author chenb
 * @apiNote
 * @since 2022-12-22
 **/
public class GiveStrategyAdapter {

    private static final HashMap<String, RedEnvelopeGiveStrategy> GIVE_STRATEGY = new HashMap<>(4);

    /**
     * 策略注册（不依赖Bean的直接在 static注册即可，如果依赖其他Bean 则通过实现InitializingBean或其他方式进行注册）
     *
     * @param channel         通道
     * @param redEnvelopeType 红包类型
     * @param strategy        策略本身
     */
    public static void addStrategy(RedEnvelopeChannel channel, RedEnvelopeType redEnvelopeType, RedEnvelopeGiveStrategy strategy) {
        GIVE_STRATEGY.put(channel.name() + "-" + redEnvelopeType.name(), strategy);
    }

    /**
     * 拆分红包
     *
     * @param redEnvelope 红包信息
     * @return 拆分红包
     */
    public static List<RedEnvelopeSpilt> split(RedEnvelope redEnvelope) {
        return Optional.ofNullable(GIVE_STRATEGY.get(redEnvelope.getChannel().name() + "-" + redEnvelope.getType().name()))
                .orElseThrow(ErrorCodeEnum.RED_STRATEGY_ERROR :: generalException)
                .spiltRedEnvelope(redEnvelope);
    }
}
