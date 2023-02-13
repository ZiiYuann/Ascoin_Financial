package com.tianli.sqs;

import lombok.Data;

/**
 * @author chenb
 * @apiNote
 * @since 2022-11-24
 **/
@Data
public class SqsContext<T> {

    // 现在只有一个queue，可能一个queue会处理多个类型的消息
    private SqsTypeEnum sqsType;

    private T context;

    public SqsContext(SqsTypeEnum sqsType, T context) {
        this.sqsType = sqsType;
        this.context = context;
    }
}
