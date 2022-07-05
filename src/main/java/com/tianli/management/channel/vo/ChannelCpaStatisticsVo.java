package com.tianli.management.channel.vo;

import lombok.Builder;
import lombok.Data;

/**
 * @author lzy
 * @date 2022/5/7 17:40
 */
@Builder
@Data
public class ChannelCpaStatisticsVo {

    @Builder.Default
    private Long register_count = 0L;

    @Builder.Default
    private Long kyc_success_count = 0L;
}
