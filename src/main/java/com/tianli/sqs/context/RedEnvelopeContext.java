package com.tianli.sqs.context;

import lombok.*;

/**
 * @author chenb
 * @apiNote
 * @since 2022-11-28
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedEnvelopeContext {

    // 红包id
    private Long rid;

    // 红包拆分id
    private String uuid;

    // 用户id
    private Long uid;

    private Long shortUid;

    private String deviceNumber;

}
