package com.tianli.address.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author cs
 * @Date 2022-12-28 11:01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MainWalletAddress {
    private String btc;

    private String eth;

    private String tron;

    private String bsc;

    private String op;

    private String arbi;

    private String polygon;
}
