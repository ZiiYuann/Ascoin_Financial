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
    @Value("${WS_ACCESS_KEY_ID}")
    private String WS_ACCESS_KEY_ID;
    @Value("${AWS_SECRET_ACCESS_KEY}")
    private String AWS_SECRET_ACCESS_KEY;

    @PostConstruct
    public void init() {
        sqsClient = SqsClient.builder().credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .region(Region.AP_NORTHEAST_1)
                .credentialsProvider(() -> new AwsCredentials() {
                    @Override
                    public String accessKeyId() {
                        return WS_ACCESS_KEY_ID;
                    }

                    @Override
                    public String secretAccessKey() {
                        return AWS_SECRET_ACCESS_KEY;
                    }
                })
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
