package com.tianli.sqs;

import cn.hutool.json.JSONUtil;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;

/**
 * @author chenb
 * @apiNote
 * @since 2022-11-22
 **/
@Component
public class SqsClientConfig {

    private SqsClient sqsClient;

    @PostConstruct
    public void init() {
        sqsClient = SqsClient.builder().credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .region(Region.AP_NORTHEAST_1).build();
//        sqsClient.sendMessage(builder -> {
//            builder.queueUrl("https://sqs.ap-northeast-1.amazonaws.com/089758303572/financial_mq_test")
//                    .messageBody(JSONUtil.toJsonStr("test"));
//        });

        ReceiveMessageResponse receiveMessageResponse = sqsClient.receiveMessage(builder -> {
            builder.queueUrl("https://sqs.ap-northeast-1.amazonaws.com/089758303572/financial_mq_test").waitTimeSeconds(20).maxNumberOfMessages(1);
        });
        String s = receiveMessageResponse.messages().get(0).receiptHandle();
    }

    @PreDestroy
    public void close() {
        sqsClient.close();
    }

    public SqsClient getSqsClient() {
        return sqsClient;
    }
}
