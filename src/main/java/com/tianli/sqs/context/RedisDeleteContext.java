package com.tianli.sqs.context;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chenb
 * @apiNote
 * @since 2023-01-06
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedisDeleteContext {

    private String key;
}
