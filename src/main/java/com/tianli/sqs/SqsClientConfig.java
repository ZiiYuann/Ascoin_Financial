package com.tianli.sqs;

import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.QueueAttributeName;

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
    }

    @PreDestroy
    public void close() {
        sqsClient.close();
    }

}
