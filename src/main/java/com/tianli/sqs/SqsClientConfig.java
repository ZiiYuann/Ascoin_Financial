package com.tianli.sqs;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

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
                .region(Region.AP_NORTHEAST_1)
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .build();
    }

    @PreDestroy
    public void close() {
        sqsClient.close();
    }

    public SqsClient getSqsClient() {
        return sqsClient;
    }
}
