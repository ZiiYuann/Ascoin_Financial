package com.tianli.config;

import com.google.gson.*;
import com.tianli.common.Constants;
import org.mountcloud.graphql.GraphqlClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.beanvalidation.BeanValidationPostProcessor;
import org.springframework.validation.beanvalidation.MethodValidationPostProcessor;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sesv2.SesV2Client;

import java.time.LocalDateTime;

/**
 * @Author wangqiyun
 * @Date 2018/12/24 4:26 PM
 */
@Configuration
public class CommonConfig {
    @Value("${subgraph.coinUrl}")
    private String subgraphServerUrl;

    @Value("${subgraph.ethCoinUrl}")
    private String subgraphETHServerUrl;

    @Bean
    BeanValidationPostProcessor beanValidationPostProcessor() {
        BeanValidationPostProcessor postProcessor = new BeanValidationPostProcessor();
        postProcessor.afterPropertiesSet();
        return postProcessor;
    }

    @Bean
    MethodValidationPostProcessor methodValidationPostProcessor() {
        MethodValidationPostProcessor postProcessor = new MethodValidationPostProcessor();
        postProcessor.setProxyTargetClass(true);
        postProcessor.afterPropertiesSet();
        return postProcessor;
    }

    @Bean
    Gson gson() {
        return new GsonBuilder().setLongSerializationPolicy(LongSerializationPolicy.STRING)
                .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (localDateTime, type, jsonSerializationContext) -> new JsonPrimitive(localDateTime.format(Constants.standardDateTimeFormatter)))
                .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (jsonElement, type, jsonDeserializationContext) -> jsonElement==null?null:LocalDateTime.parse(jsonElement.getAsString(), Constants.standardDateTimeFormatter))
                .create();
    }


    @Bean
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public GraphqlClient graphqlClient() {
        return GraphqlClient.buildGraphqlClient(subgraphServerUrl);
    }

    @Bean
    public GraphqlClient ethGraphqlClient() {
        return GraphqlClient.buildGraphqlClient(subgraphETHServerUrl);
    }


    @Value("${email.amazon.accessKeyId}")
    private String emailAmazonAccessKeyId;

    @Value("${email.amazon.secretAccessKey}")
    private String emailAmazonSecretAccessKey;

    @Bean
    public SesV2Client sesV2Client() {
        return SesV2Client.builder().credentialsProvider(() ->
                        AwsBasicCredentials.create(emailAmazonAccessKeyId, emailAmazonSecretAccessKey))
                .region(Region.AP_NORTHEAST_1).build();
    }


}
