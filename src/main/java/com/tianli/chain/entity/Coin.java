package com.tianli.chain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
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
    private Long id;

    private String name;

    private String contract;

    private String logo;

    private String chain;

    private String network;

    private int weight;

    // 状态：0未上架  1上架中 2上架完成 3下架
    private byte status;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime updateTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime createTime;

    private Long createBy;

    private Long updateBy;

    private String rateUrl;

    private String rateField;
}
