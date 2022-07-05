package com.tianli.exchange.production;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.exchange.dto.ExchangeCoreRequestDTO;
import com.tianli.management.ruleconfig.ConfigConstants;
import com.tianli.mconfig.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import javax.annotation.Resource;

/**
 * @author lzy
 * @date 2022/6/16 14:47
 */
@Component
@Slf4j
public class ExchangeTradeProduction {


    @Resource
    SqsClient sqsClient;

    @Resource
    ConfigService configService;



    public void submit(ExchangeCoreRequestDTO exchangeCoreRequestDTO) {
        String sendUrl = configService.get(ConfigConstants.MQ_SEND_URL);
        String messageId = sqsClient.sendMessage(SendMessageRequest.builder().queueUrl(sendUrl)
                .messageBody(JSONUtil.toJsonStr(exchangeCoreRequestDTO)).build()).messageId();
        if (StrUtil.isBlank(messageId)) {
            throw ErrorCodeEnum.SYSTEM_BUSY.generalException();
        }
        log.info("下单成功:{},messageId:{}", exchangeCoreRequestDTO, messageId);
    }
}
