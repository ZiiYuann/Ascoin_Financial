package com.tianli.sqs.handler;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.tianli.address.mapper.Address;
import com.tianli.chain.entity.Coin;
import com.tianli.chain.service.ChainService;
import com.tianli.sqs.SqsContext;
import com.tianli.sqs.SqsReceiveHandler;
import com.tianli.sqs.SqsTypeEnum;
import com.tianli.sqs.context.PushAddressContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.sqs.model.Message;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

/**
 * @author chenb
 * @apiNote
 * @since 2022-11-28
 **/
@Slf4j
@Component
public class PushAddressHandler implements SqsReceiveHandler {

    @Resource
    private ChainService chainService;

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public void handler(Message message) {
        SqsContext<JSONObject> sqsContext = JSONUtil.toBean(message.body(), SqsContext.class);
        PushAddressContext pushAddressContext = JSONUtil.toBean(sqsContext.getContext(), PushAddressContext.class);
        List<String> addresses = pushAddressContext.getAddresses();
        Coin coin = pushAddressContext.getCoin();
        chainService.pushConditionRecharge(addresses, coin);
    }

    @Override
    public SqsTypeEnum getSqsType() {
        return SqsTypeEnum.ADD_COIN_PUSH;
    }
}