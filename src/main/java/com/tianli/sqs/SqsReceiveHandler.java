package com.tianli.sqs;

import software.amazon.awssdk.services.sqs.model.Message;

/**
 * @author chenb
 * @apiNote
 * @since 2022-11-24
 **/
public interface SqsReceiveHandler {

    /**
     * 具体的控制逻辑
     *
     * @param message 消息对象
     */
    void handler(Message message);

    /**
     * 控制器类型
     */
    SqsTypeEnum getSqsType();

}
