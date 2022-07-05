package com.tianli.exchange.dto;

import lombok.Data;


/**
 * @author lzy
 * @date 2022/5/24 15:15
 */
@Data
public class RevokeOrderDTO {

    private String coin;

    private Long orderId;
}
