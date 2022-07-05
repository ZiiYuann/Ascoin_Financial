package com.tianli.captcha.email.service;

import com.tianli.mconfig.ConfigService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sesv2.SesV2Client;
import software.amazon.awssdk.services.sesv2.model.*;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Service
public class EmailAmazonService {

    private static final String EMAIL_FROM_ADDRESS = "Financial@financial.pro";

    /**
     * 亚马逊邮件服务, 发送邮件
     *
     * @param emailAddress 发送地址
     * @param context 内容
     */
    public void send(String emailAddress, String title, String context) {
        if (StringUtils.isBlank(title)
                || StringUtils.isBlank(emailAddress)
                || StringUtils.isBlank(context)) return;
        String canSend = configService.getOrDefault("email_switch", "true");
        if(Objects.equals(canSend, "false")){
            return;
        }
        sesV2Client.sendEmail(SendEmailRequest.builder()
                .fromEmailAddress(EMAIL_FROM_ADDRESS)
                .destination(Destination.builder().toAddresses(emailAddress).build())
                .content(EmailContent.builder().simple(Message.builder()
                        .subject(Content.builder().charset(StandardCharsets.UTF_8.name()).data(title).build())
                        .body(Body.builder().text(Content.builder().charset(StandardCharsets.UTF_8.name()).data(context).build()).build())
                        .build()).build())
                .build());



    }

    @Resource
    private ConfigService configService;

    @Resource
    private SesV2Client sesV2Client;
}
