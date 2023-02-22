package com.tianli.accountred.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.accountred.entity.RedEnvelopeConfig;
import com.tianli.accountred.enums.RedEnvelopeChannel;
import com.tianli.accountred.mapper.RedEnvelopeConfigMapper;
import com.tianli.accountred.service.RedEnvelopeConfigService;
import com.tianli.accountred.vo.RedEnvelopeConfigVO;
import com.tianli.other.query.RedEnvelopeConfigIoUQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public void saveOrUpdate(String nickName, RedEnvelopeConfigIoUQuery query) {
        RedEnvelopeConfig redEnvelopeConfig = baseMapper.selectByName(query.getCoin());
        RedEnvelopeConfig config = RedEnvelopeConfig.builder()
                .coin(query.getCoin())
                .channel(query.getChannel())
                .num(query.getNum() == null ? 1000 : query.getNum())
                .limitAmount(query.getLimitAmount() == null ? new BigDecimal(100) : query.getLimitAmount())
                .minAmount(query.getMinAmount() == null ? new BigDecimal("0.000001") : query.getMinAmount()).build();
        if (Objects.isNull(redEnvelopeConfig)) {
            config.setCreateBy(nickName);
            config.setCreateTime(LocalDateTime.now());
            baseMapper.insert(config);
        } else {
            config.setUpdateBy(nickName);
            config.setUpdateTime(LocalDateTime.now());
            baseMapper.update(config, new LambdaQueryWrapper<RedEnvelopeConfig>().eq(RedEnvelopeConfig::getCoin, query.getCoin()));
        }
    }

    @Override
    public RedEnvelopeConfig getOne(String coin, RedEnvelopeChannel channel) {
        return Optional.ofNullable(this.getOne(new LambdaQueryWrapper<RedEnvelopeConfig>()
                        .eq(RedEnvelopeConfig::getCoin, coin)
                        .eq(RedEnvelopeConfig::getChannel, channel)))
                .orElse(RedEnvelopeConfig.externDefaultConfig());
    }

    @Override
    public RedEnvelopeConfig getDetails(String coin, RedEnvelopeChannel channel) {
        RedEnvelopeConfig one = this.getOne(new LambdaQueryWrapper<RedEnvelopeConfig>()
                .eq(RedEnvelopeConfig::getCoin, coin)
                .eq(RedEnvelopeConfig::getChannel, channel));
        if (Objects.isNull(one)) {
            one = new RedEnvelopeConfig() {{
                setCoin(coin);
                setLimitAmount(BigDecimal.valueOf(100));
                setMinAmount(BigDecimal.valueOf(0.000001));
                setChannel(RedEnvelopeChannel.EXTERN);
                setNum(1000);
            }};
        }
        return one;
    }

    @Override
    public List<RedEnvelopeConfigVO> getList(String channel, String coin) {
        LambdaQueryWrapper<RedEnvelopeConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RedEnvelopeConfig::getChannel, channel);
        if (Objects.nonNull(coin)) {
            wrapper = wrapper.eq(RedEnvelopeConfig::getCoin, coin);
        }
        return this.list(wrapper).stream()
                .map(config -> RedEnvelopeConfigVO.builder()
                        .coin(config.getCoin())
                        .limitAmount(config.getLimitAmount())
                        .num(config.getNum())
                        .minAmount(config.getMinAmount())
                        .scale(config.getScale()).build()).collect(Collectors.toList());
    }
}
