package com.tianli.chain.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.chain.entity.CoinReviewConfig;
import com.tianli.chain.query.CoinReviewConfigIoUQuery;

/**
 * @author chenb
 * @apiNote
 * @since 2022-12-09
 **/

public interface CoinReviewConfigService extends IService<CoinReviewConfig> {

    /**
     * 审核配置
     *
     * @param nickname 操作人
     * @param query    请求参数
     */
    void reviewConfig(String nickname, CoinReviewConfigIoUQuery query);

    /**
     * 获取审核配置
     *
     * @return 配置信息
     */
    CoinReviewConfig reviewConfig();
}
