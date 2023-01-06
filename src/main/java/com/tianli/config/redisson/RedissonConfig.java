package com.tianli.config.redisson;

import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Autowired
    private RedissonProperties RedissonProperties;

    @Bean
    public RedissonClient redissonClient(){
        // 默认连接地址 127.0.0.1:6379
        Config config = new Config();
        SingleServerConfig singleServerConfig = config.useSingleServer();
        singleServerConfig
                .setAddress("redis://"+RedissonProperties.getHost()+":"+RedissonProperties.getPort())
                .setDatabase(RedissonProperties.getDatabase())
                .setConnectionMinimumIdleSize(1)
                .setConnectionPoolSize(1)
                .setConnectTimeout(100000)
                .setTimeout(30000)
                .setRetryAttempts(30)
                .setRetryInterval(15000);
        if(StringUtils.isNotEmpty(RedissonProperties.getPassword())){
            singleServerConfig.setPassword(RedissonProperties.getPassword());
        }
        return Redisson.create(config);
    }

}
