package com.tianli.exchange.consumer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.tianli.common.CommonFunction;
import com.tianli.exchange.dto.ExchangeCoreResponseDTO;
import com.tianli.exchange.dto.ExchangeDepthDTO;
import com.tianli.exchange.entity.ExchangeMsgFail;
import com.tianli.exchange.service.IExchangeMsgFailService;
import com.tianli.exchange.service.IOrderService;
import com.tianli.management.ruleconfig.ConfigConstants;
import com.tianli.mconfig.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author lzy
 * @date 2022/6/23 14:42
 */
@Component
@Slf4j
public class ExchangeTradeConsumer implements CommandLineRunner {

    @Resource
    SqsClient sqsClient;

    @Resource
    IOrderService orderService;

    @Resource
    ConfigService configService;

    @Resource
    IExchangeMsgFailService exchangeMsgFailService;

    public static final Integer MAX_RETRY_NUMBER = 2;


    @Override
    public void run(String... args) {
        ThreadUtil.execute(() -> {
            log.info("---开始监听mq---");
            while (true) {
                try {
                    exchangeMatch();
                } catch (Exception e) {
                    log.error("撮合交易异常:{}", e.toString());
                }
            }
        });
    }


    private void exchangeMatch() {
        String receiveUrl = configService.get(ConfigConstants.MQ_RESPONSE_URL);
        ReceiveMessageResponse receiveMessageResponse = sqsClient.receiveMessage(ReceiveMessageRequest.builder()
                .queueUrl(receiveUrl)
                .maxNumberOfMessages(1)
                .waitTimeSeconds(20)
                .build());
        List<Message> messages = receiveMessageResponse.messages();
        if (CollUtil.isEmpty(messages)) {
            return;
        }
        for (Message message : messages) {
            String body = message.body();
            if (body.contains("id")) {
                ExchangeCoreResponseDTO coreResponseDTO = null;
                try {
                    coreResponseDTO = JSONUtil.toBean(body, ExchangeCoreResponseDTO.class);
                    log.info("收到撮合交易信息:{}", coreResponseDTO);
                    orderService.matchOrder(coreResponseDTO);
                } catch (Exception e) {
                    log.error("撮合交易异常:{}", e.toString());
                    //存入记录表
                    if (saveRecord(coreResponseDTO, e)) continue;
                }
            } else {
                ExchangeDepthDTO exchangeDepthDTO = JSONUtil.toBean(body, ExchangeDepthDTO.class);
                log.info("收到深度信息:{}", exchangeDepthDTO);
                orderService.depth(exchangeDepthDTO);
            }
            DeleteMessageRequest deleteMessageRequest = DeleteMessageRequest.builder()
                    .queueUrl(receiveUrl)
                    .receiptHandle(message.receiptHandle())
                    .build();
            sqsClient.deleteMessage(deleteMessageRequest);
        }
    }

    private boolean saveRecord(ExchangeCoreResponseDTO coreResponseDTO, Exception e) {
        if (ObjectUtil.isNotNull(coreResponseDTO)) {
            ExchangeMsgFail exchangeMsgFail = exchangeMsgFailService.getByMsgId(coreResponseDTO.getId());
            if (ObjectUtil.isNotNull(exchangeMsgFail)) {
                Integer retry_number = exchangeMsgFail.getRetry_number();
                if (retry_number < MAX_RETRY_NUMBER) {
                    exchangeMsgFail.setRetry_number(retry_number + 1);
                    exchangeMsgFail.setUpdate_time(LocalDateTime.now());
                    exchangeMsgFailService.updateById(exchangeMsgFail);
                    return true;
                }
            } else {
                exchangeMsgFail = ExchangeMsgFail.builder()
                        .id(CommonFunction.generalId())
                        .message(JSONUtil.toJsonStr(coreResponseDTO))
                        .msg_id(coreResponseDTO.getId())
                        .fail_reason(e.toString())
                        .create_time(LocalDateTime.now())
                        .retry_number(0)
                        .build();
                exchangeMsgFailService.save(exchangeMsgFail);
                return true;
            }
        }
        return false;
    }
}
