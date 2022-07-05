package com.tianli.channel.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.channel.dao.AfChannelMapper;
import com.tianli.channel.entity.AfChannel;
import com.tianli.common.CommonFunction;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * @author lzy
 * @date 2022/5/20 16:20
 */
@Service
public class AfChannelService extends ServiceImpl<AfChannelMapper, AfChannel> {

    public void add(String string) {
        AfChannel afChannel = AfChannel.builder()
                .id(CommonFunction.generalId())
                .data(string)
                .create_time(LocalDateTime.now())
                .build();
        this.save(afChannel);
    }
}
