package com.tianli.management.channel.vo;

import cn.hutool.core.util.ObjectUtil;
import com.tianli.management.channel.entity.Channel;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author lzy
 * @date 2022/4/7 3:21 下午
 */
@Builder
@Data
public class ChannelInfoListVo {

    private Long id;

    private String username;

    private String p_name;

    private String creator;

    private LocalDateTime create_time;

    private String remark;

    public static ChannelInfoListVo collect(Channel channel, Channel p_channel) {
        String p_name = ObjectUtil.isNull(p_channel) ? null : p_channel.getAdmin_username();
        return ChannelInfoListVo.builder()
                .id(channel.getId())
                .username(channel.getAdmin_username())
                .creator(channel.getCreator())
                .p_name(p_name)
                .create_time(channel.getCreate_time())
                .remark(channel.getRemark())
                .build();
    }
}
