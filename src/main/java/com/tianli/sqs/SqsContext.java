package com.tianli.sqs;

import lombok.Data;

/**
 * @author chenb
 * @apiNote
 * @since 2022-11-24
 **/
@Data
public class SqsContext<T> {

    private SqsTypeEnum sqsType;

    private T context;

    public SqsContext(SqsTypeEnum sqsType, T context) {
        this.sqsType = sqsType;
        this.context = context;
    }
}
