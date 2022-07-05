package com.tianli.common;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "facebook")
public class FacebookProperties {

    private String client_id;

    private String client_secret;

    private String redirect_uri;

    private String host;

    private String me_path;

    private String debug_token_path;
}
