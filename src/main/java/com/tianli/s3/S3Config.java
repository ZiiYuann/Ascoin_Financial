package com.tianli.s3;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.tianli.mconfig.ConfigService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * @Author cs
 * @Aate 2021-10-25 10:41 上午
 */
@Configuration
public class S3Config {
    @Resource
    private ConfigService configService;

    @Bean
    public AmazonS3 s3Client() {

        String region = null;
        String accessKey = null;
        String secretKey = null;
        try {
            region = configService.getOrDefault("s3_region", "ap-northeast-1");
            accessKey = configService.getOrDefault("s3_AWSAccessKeyId", "AKIAXFCHS3YDAAYVHQWV");
            secretKey = configService.getOrDefault("s3_AWSSecretAccessKey", "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");
        } catch (Exception e) {
            region = "ap-northeast-1";
            accessKey = "AKIAXFCHS3YDAAYVHQWV";
            secretKey = "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";
        }
        AmazonS3ClientBuilder standard = AmazonS3ClientBuilder.standard().withRegion(region);
        standard.setCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)));
        return standard.build();
    }
}
