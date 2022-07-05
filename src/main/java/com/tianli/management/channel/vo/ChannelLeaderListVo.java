package com.tianli.management.channel.vo;

import com.tianli.management.channel.entity.Channel;
import lombok.Builder;
import lombok.Data;

/**
 * @author lzy
 * @date 2022/4/6 5:57 下午
 */
@Builder
@Data
public class ChannelLeaderListVo {

    private Long leader_id;

    private String username;

    public static ChannelLeaderListVo collect(Channel channel) {
        return ChannelLeaderListVo.builder()
                .leader_id(channel.getId())
                .username(channel.getAdmin_username())
                .build();
    }
}
