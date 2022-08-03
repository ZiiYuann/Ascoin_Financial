package com.tianli.chain.dto;

import lombok.Data;

@Data
public class EthGasAPIResponse {
    private Double fast;
    private Double fastest;
    private Double safeLow;
    private Double average;
}