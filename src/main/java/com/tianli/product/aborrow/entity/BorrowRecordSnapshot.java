package com.tianli.product.aborrow.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-06
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowRecordSnapshot {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long uid;

    // JSON 数据
    private String data;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

}
