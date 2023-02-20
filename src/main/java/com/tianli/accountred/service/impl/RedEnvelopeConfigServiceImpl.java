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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

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
    @Transactional
    public void saveOrUpdate(String nickName,RedEnvelopeConfigIoUQuery query) {
        RedEnvelopeConfig redEnvelopeConfig = baseMapper.selectByName(query.getCoin());
        RedEnvelopeConfig config = RedEnvelopeConfig.builder()
                .coin(query.getCoin())
                .channel(query.getChannel())
                .num(query.getNum()==null?1000: query.getNum())
                .limitAmount(query.getLimitAmount()==null?new BigDecimal(100):query.getLimitAmount())
                .minAmount(query.getMinAmount()==null?new BigDecimal(0.000001):query.getMinAmount()).build();
        if (Objects.isNull(redEnvelopeConfig)) {
            config.setCreateBy(nickName);
            config.setCreateTime(LocalDateTime.now());
            baseMapper.insert(config);
        }else {
            config.setUpdateBy(nickName);
            config.setUpdateTime(LocalDateTime.now());
            baseMapper.update(config,new LambdaQueryWrapper<RedEnvelopeConfig>().eq(RedEnvelopeConfig::getCoin,query.getCoin()));
        }
    }

    @Override
    public RedEnvelopeConfig getOne(String coin, RedEnvelopeChannel channel) {
        return Optional.ofNullable(this.getOne(new LambdaQueryWrapper<RedEnvelopeConfig>()
                .eq(RedEnvelopeConfig::getCoin, coin)
                .eq(RedEnvelopeConfig::getChannel, channel)))
                .orElse(RedEnvelopeConfig.defaultConfig());
    }

    @Override
    public RedEnvelopeConfig getDetails(String coin, RedEnvelopeChannel channel) {
        return  this.getOne(new LambdaQueryWrapper<RedEnvelopeConfig>()
                .eq(RedEnvelopeConfig::getCoin, coin)
                .eq(RedEnvelopeConfig::getChannel, channel))
         ;
    }
}
