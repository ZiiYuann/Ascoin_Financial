package com.tianli.common.init;

import com.google.common.collect.Lists;
import com.tianli.role.annotation.GrcCheckModular;
import lombok.Builder;
import lombok.Data;

import java.util.List;


@Data
public class RequestRiskManagementInfo {

    private String hash_key;
    private String username;
    private String nick;
    /**
     * 谷歌人机校验结果
     * true: 表示校验成功
     * false: 接口不需要校验或者是校验失败
     */
    private boolean grc;

    /**
     * 分数
     */
    private double grcScore;

    private String country = "";
    private String region = "";
    private String city = "";

    private String rooMethodName = "";

    private List<IpLogsContext> ipLogsContext = Lists.newArrayList();

    @Data
    @Builder
    public static class IpLogsContext{
        private GrcCheckModular modular;
        private Long logId;
    }
}
