package com.tianli.config.redisson;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.redis")
@Data
public class RedissonProperties {
    private String host;
    private int port = 6379;
    private int database;
    private String password;
}
