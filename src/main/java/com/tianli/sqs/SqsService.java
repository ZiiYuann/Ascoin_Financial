package com.tianli.sqs;

import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import javax.annotation.Resource;

/**
 * @author chenb
 * @apiNote
 * @since 2022-11-23
 **/
@Service
public class SqsService {

    @Resource
    private SqsClientConfig sqsClientConfig;
    @Value("${sqs.url}")
    private String sqsUrl;

    public void send(SqsContext<?> sqsContext) {
        sqsClientConfig.getSqsClient().sendMessage(builder -> {
            builder.queueUrl(sqsUrl).messageBody(JSONUtil.toJsonStr(sqsContext));
        });
    }

    public void receiveAndDelete() {
        ReceiveMessageResponse receiveMessageResponse = sqsClientConfig.getSqsClient().receiveMessage(builder -> {
            builder.queueUrl(sqsUrl).waitTimeSeconds(20).maxNumberOfMessages(1);
        });


        receiveMessageResponse.messages()


        String s = receiveMessageResponse.messages().get(0).receiptHandle();
        sqsClientConfig.getSqsClient().deleteMessage(builder -> {
            builder.queueUrl(sqsUrl).receiptHandle("receiptHandle");
        });
    }


}
