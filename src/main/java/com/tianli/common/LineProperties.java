package com.tianli.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "line")
public class LineProperties {

    private String client_id;

    private String client_secret;

    private String redirect_uri;

    private String host;

    private String token_path;

    private String verify_path;

    private String user_profile_path;
}
