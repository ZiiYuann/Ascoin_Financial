package com.tianli.common.init;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author wangqiyun
 * @Date 2020/7/14 16:56
 */
public interface RequestInitToken {
    Long currentUserId(HttpServletRequest httpServletRequest);

    /**
     * 检查谷歌人机校验结果;
     */
    void googleCheck(HttpServletRequest httpServletRequest, RequestRiskManagementInfo riskInfo);

}
