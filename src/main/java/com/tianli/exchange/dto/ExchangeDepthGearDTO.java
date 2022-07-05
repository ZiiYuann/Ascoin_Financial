package com.tianli.exchange.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigInteger;

/**
 * @author lzy
 * @date 2022/6/23 14:58
 */
@Builder
@Data
public class ExchangeDepthGearDTO {

    private BigInteger price;

    private BigInteger number;
}
