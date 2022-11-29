package com.tianli.sqs.handler;

import cn.hutool.json.JSONUtil;
import com.tianli.address.mapper.Address;
import com.tianli.chain.entity.Coin;
import com.tianli.sqs.SqsContext;
import com.tianli.sqs.SqsReceiveHandler;
import com.tianli.sqs.SqsTypeEnum;
import com.tianli.sqs.context.PushAddressContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.sqs.model.Message;

import java.util.List;
import java.util.Optional;

/**
 * @author chenb
 * @apiNote
 * @since 2022-11-28
 **/
@Component
public class PushAddressHandler implements SqsReceiveHandler {

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public void handler(Message message) {
        SqsContext<PushAddressContext> sqsContext = JSONUtil.toBean(message.body(), SqsContext.class);
        List<Address> address = sqsContext.getContext().getAddress();
        Coin coin = sqsContext.getContext().getCoin();
        return;

    }

    @Override
    public SqsTypeEnum getSqsType() {
        return SqsTypeEnum.ADD_COIN_PUSH;
    }

    @Override
    public int getMaxNumberOfMessages() {
        return 10;
    }
}
