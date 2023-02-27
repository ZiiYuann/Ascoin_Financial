package com.tianli.sqs.handler;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tianli.accountred.service.RedEnvelopeService;
import com.tianli.common.RedisConstants;
import com.tianli.common.webhook.WebHookService;
import com.tianli.sqs.*;
import com.tianli.sqs.context.RedEnvelopeContext;
import com.tianli.sqs.context.RedisDeleteContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.model.Message;

import javax.annotation.Resource;

/**
 * @author chenb
 * @apiNote
 * @since 2022-11-28
 **/
@Slf4j
@Component
public class RedEnvelopeHandler extends AbstractSqsReceiveHandler {

    @Resource
    private RedEnvelopeService redEnvelopeService;
    @Resource
    private SqsService sqsService;
    @Resource
    private WebHookService webHookService;

    @Override
    @SuppressWarnings("unchecked")
    public void handlerOperation(Message message) {
        SqsContext<JSONObject> sqsContext = JSONUtil.toBean(message.body(), SqsContext.class);
        var redEnvelopeContext = JSONUtil.toBean(sqsContext.getContext(), RedEnvelopeContext.class);
        try {
            redEnvelopeService.asynGet(redEnvelopeContext);
            // 删除红包缓存
            redEnvelopeService.deleteRedisCache(redEnvelopeContext.getRid());
        } catch (Exception e) {
            webHookService.dingTalkSend("红包异步转账异常:" + JSONUtil.toJsonStr(redEnvelopeContext));
            throw e;
        }
        // 延时间删除(通过消息队列保证消费)
        String key = RedisConstants.RED_ENVELOPE + redEnvelopeContext.getRid();
        sqsService.send(new SqsContext<>(SqsTypeEnum.RDS_DELETE, new RedisDeleteContext(key)));
    }

    @Override
    public SqsTypeEnum getSqsType() {
        return SqsTypeEnum.RED_ENVELOP;
    }
}
