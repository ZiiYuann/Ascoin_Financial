package com.tianli.sqs;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.sqs.model.Message;

/**
 * @author chenb
 * @apiNote
 * @since 2023-01-06
 **/
@Slf4j
public abstract class AbstractSqsReceiveHandler implements SqsReceiveHandler {

    @Override
    public void handler(Message message) {
        log.info("sqs消费，context：" + message.body());
        // preHandler

        this.handlerOperation(message);

        // afterHandler

    }

    public abstract void handlerOperation(Message message);
}
