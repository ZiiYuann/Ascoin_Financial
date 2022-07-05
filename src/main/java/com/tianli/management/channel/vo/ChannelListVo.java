package com.tianli.management.channel.vo;

import cn.hutool.core.collection.CollUtil;
import com.tianli.management.channel.entity.Channel;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lzy
 * @date 2022/4/6 6:20 下午
 */
@Builder
@Data
public class ChannelListVo {
    private Long id;

    private String name;

    private String remark;

    private List<ChannelListVo> channelListVoList;


    public static ChannelListVo collect(Channel channel, List<Channel> teamMemberChannelList) {
        ChannelListVo salesmanListVo = collect(channel);
        if (CollUtil.isNotEmpty(teamMemberChannelList)) {
            List<ChannelListVo> salesmanListVos = teamMemberChannelList.stream().map(ChannelListVo::collect).collect(Collectors.toList());
            salesmanListVo.setChannelListVoList(salesmanListVos);
        }
        return salesmanListVo;
    }


    public static ChannelListVo collect(Channel channel) {
        return ChannelListVo.builder()
                .id(channel.getId())
                .name(channel.getAdmin_username())
                .remark(channel.getRemark())
                .build();
    }
}
