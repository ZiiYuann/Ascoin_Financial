package com.tianli.sqs;

import cn.hutool.json.JSONUtil;
import com.tianli.common.webhook.WebHookService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;

/**
 * @author chenb
 * @apiNote
 * @since 2022-11-23
 **/
@Service
public class SqsService {

    @Resource
    private SqsClientConfig sqsClientConfig;
    @Value("${sqs.url}")
    private String sqsUrl;
    @Resource
    private List<SqsReceiveHandler> sqsReceiveHandlers;
    @Resource
    private WebHookService webHookService;


    public void send(SqsContext<?> sqsContext) {
        if (Objects.isNull(sqsContext.getSqsType())) {
            throw new NullPointerException();
        }
        sqsClientConfig.getSqsClient().sendMessage(builder -> {
            builder.queueUrl(sqsUrl)
                    .messageBody(JSONUtil.toJsonStr(sqsContext));
        });
    }

    public int receiveAndDelete(String url, int maxNumberOfMessages) {
        url = StringUtils.isBlank(url) ? sqsUrl : url;
        final String finalUrl = url;
        ReceiveMessageResponse receiveMessageResponse = sqsClientConfig.getSqsClient().receiveMessage(builder -> {
            builder.queueUrl(finalUrl)
                    .waitTimeSeconds(20)
                    .maxNumberOfMessages(maxNumberOfMessages);
        });


        List<Message> messages = receiveMessageResponse.messages();
        if (CollectionUtils.isNotEmpty(messages)) {
            messages.forEach(message -> {
                try {
                    SqsContext<?> sqsContext = JSONUtil.toBean(message.body(), SqsContext.class);
                    SqsReceiveHandler handler = getHandler(sqsContext.getSqsType());
                    handler.handler(message);
                    sqsClientConfig.getSqsClient().deleteMessage(builder ->
                            builder.queueUrl(sqsUrl).receiptHandle(message.receiptHandle()));
                } catch (Exception e) {
                    e.printStackTrace();
                    webHookService.dingTalkSend("推送地址消费失败", e);
                }
            });
        }
        return messages.size();
    }

    /**
     * 获取控制器
     */
    public SqsReceiveHandler getHandler(SqsTypeEnum sqsType) {
        for (SqsReceiveHandler handler : sqsReceiveHandlers) {
            if (sqsType.equals(handler.getSqsType())) {
                return handler;
            }
        }
        throw new NullPointerException();
    }


}
