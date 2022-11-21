package com.tianli.chain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-11-21
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Coin {
    private String name;
    private String contract;
    private String logo;
    private String chain;
    private String network;
    private String weight;
    private String rateUrl;
    private String rateField;
    private byte status;
    private LocalDateTime updateTime;
    private LocalDateTime createTime;
    private Long createBy;
    private Long updateBy;
}
