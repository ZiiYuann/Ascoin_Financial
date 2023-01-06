package com.tianli.sqs.handler;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tianli.sqs.AbstractSqsReceiveHandler;
import com.tianli.sqs.SqsContext;
import com.tianli.sqs.SqsReceiveHandler;
import com.tianli.sqs.SqsTypeEnum;
import com.tianli.sqs.context.RedisDeleteContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.model.Message;

import javax.annotation.Resource;

/**
 * @author chenb
 * @apiNote
 * @since 2023-01-06
 **/
@Slf4j
@Component
public class RedisDeleteHandler extends AbstractSqsReceiveHandler {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @SuppressWarnings("unchecked")
    public void handlerOperation(Message message) {
        SqsContext<JSONObject> sqsContext = JSONUtil.toBean(message.body(), SqsContext.class);
        var redisDeleteContext = JSONUtil.toBean(sqsContext.getContext(), RedisDeleteContext.class);
        String key = redisDeleteContext.getKey();
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            stringRedisTemplate.delete(key);
        }
    }

    @Override
    public SqsTypeEnum getSqsType() {
        return SqsTypeEnum.RDS_DELETE;
    }
}
