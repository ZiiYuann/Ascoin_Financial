package com.tianli.management.directioanalconfig.vo;

import lombok.Builder;
import lombok.Data;

/**
 * @author chensong
 *  2021-03-04 14:36
 * @since 1.0.0
 */
@Data
@Builder
public class StatVO {
    private Long uid;
    private String username;
    private String business_name;
}
