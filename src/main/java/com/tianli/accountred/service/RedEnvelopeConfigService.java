package com.tianli.accountred.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.accountred.entity.RedEnvelopeConfig;
import com.tianli.accountred.enums.RedEnvelopeChannel;
import com.tianli.other.query.RedEnvelopeConfigIoUQuery;

/**
 * @author chenb
 * @apiNote
 * @since 2022-10-17
 **/
public interface RedEnvelopeConfigService extends IService<RedEnvelopeConfig> {

    /**
     * 保存或者更新
     *
     * @param query 请求参数
     */
    void saveOrUpdate(String nickName,RedEnvelopeConfigIoUQuery query);


    /**
     * 通过币别和渠道获相对应的配置
     *
     * @param coin    币别
     * @param channel 渠道
     */
    RedEnvelopeConfig getOne(String coin, RedEnvelopeChannel channel);

    /**
     * 通过币别和渠道获相对应的配置
     * @param coin 币别
     * @param channel 渠道
     * @return
     */
    RedEnvelopeConfig getDetails(String coin,RedEnvelopeChannel channel);
}

