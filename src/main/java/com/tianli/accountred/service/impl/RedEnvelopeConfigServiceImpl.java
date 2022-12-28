package com.tianli.accountred.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.accountred.entity.RedEnvelopeConfig;
import com.tianli.accountred.enums.RedEnvelopeChannel;
import com.tianli.accountred.mapper.RedEnvelopeConfigMapper;
import com.tianli.accountred.service.RedEnvelopeConfigService;
import com.tianli.other.query.RedEnvelopeConfigIoUQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author chenb
 * @apiNote
 * @since 2022-10-17
 **/
@Slf4j
@Service
public class RedEnvelopeConfigServiceImpl extends ServiceImpl<RedEnvelopeConfigMapper, RedEnvelopeConfig>
        implements RedEnvelopeConfigService {

    @Override
    public void saveOrUpdate(RedEnvelopeConfigIoUQuery query) {

    }

    @Override
    public RedEnvelopeConfig getOne(String coin, RedEnvelopeChannel channel) {
        return this.getOne(new LambdaQueryWrapper<RedEnvelopeConfig>()
                .eq(RedEnvelopeConfig::getCoin, coin)
                .eq(RedEnvelopeConfig::getChannel, channel));
    }
}
