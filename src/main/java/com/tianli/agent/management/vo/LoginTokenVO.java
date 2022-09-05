package com.tianli.agent.management.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginTokenVO {

    private String token;

    private Long userId;

    private String username;
}
