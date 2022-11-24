package com.tianli.chain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-11-30
 **/
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CoinBase {

    @Id
    private String name;

    private String logo;

    private int weight;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime updateTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime createTime;

    private Long createBy;

    private Long updateBy;

    private boolean mainToken;

    private boolean show;

}
