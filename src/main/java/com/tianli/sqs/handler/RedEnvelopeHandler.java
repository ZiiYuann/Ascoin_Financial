package com.tianli.sqs.handler;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tianli.accountred.service.RedEnvelopeService;
import com.tianli.chain.entity.Coin;
import com.tianli.chain.service.ChainService;
import com.tianli.common.webhook.WebHookService;
import com.tianli.sqs.SqsContext;
import com.tianli.sqs.SqsReceiveHandler;
import com.tianli.sqs.SqsTypeEnum;
import com.tianli.sqs.context.PushAddressContext;
import com.tianli.sqs.context.RedEnvelopeContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.sqs.model.Message;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2022-11-28
 **/
@Slf4j
@Component
public class RedEnvelopeHandler implements SqsReceiveHandler {

    @Resource
    private RedEnvelopeService redEnvelopeService;
    @Resource
    private WebHookService webHookService;

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public void handler(Message message) {
        SqsContext<JSONObject> sqsContext = JSONUtil.toBean(message.body(), SqsContext.class);
        var redEnvelopeContext = JSONUtil.toBean(sqsContext.getContext(), RedEnvelopeContext.class);
        try {
            redEnvelopeService.asynGet(redEnvelopeContext);
        } catch (Exception e) {
            webHookService.dingTalkSend("领取红包异常", e);
            throw e;
        }
    }

    @Override
    public SqsTypeEnum getSqsType() {
        return SqsTypeEnum.RED_ENVELOP;
    }
}
