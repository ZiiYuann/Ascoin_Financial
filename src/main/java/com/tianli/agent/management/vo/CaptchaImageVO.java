package com.tianli.agent.management.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CaptchaImageVO {

    private String uuid;

    private String img;
}
