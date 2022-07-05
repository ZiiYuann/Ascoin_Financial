package com.tianli.exchange.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author lzy
 * @date 2022/6/23 14:57
 */
@Builder
@Data
public class ExchangeDepthDTO {

    private String symbol;

    private List<ExchangeDepthGearDTO> sellRank;

    private List<ExchangeDepthGearDTO> buyRank;
}
