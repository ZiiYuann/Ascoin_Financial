package com.tianli.management.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author chenb
 * @apiNote
 * @since 2022-11-29
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MCoinListVO {
    private Long id;

    private String name;

    private String contract;

    private String logo;

    private String chain;

    private String network;

    private int weight;

    // 状态：0未上架  1上架中 2上架完成 3下架
    private byte status;

}
