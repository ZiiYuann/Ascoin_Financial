package com.tianli.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

/**
 * @author lzy
 * @date 2022/6/22 15:25
 */
@Configuration
public class SqsMqConfig {


    @Bean
    public SqsClient initSqsClient() {
        return SqsClient.builder().credentialsProvider(EnvironmentVariableCredentialsProvider.create()).region(Region.AP_NORTHEAST_1).build();
    }
}
