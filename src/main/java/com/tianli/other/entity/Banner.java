package com.tianli.other.entity;

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
 * @since 2022-12-12
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Banner {

    @Id
    private Long id;

    private String name;

    private String nameEn;

    private String urlZh;

    private String urlEn;

    // 跳转类型(1、聊天群2、普通链接3、内部页面)
    private byte jumpType;

    private String jumpUrl;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    // 设备类型(0、全部 1、安卓 2、ios)
    private byte deviceType;

    private byte weight;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;

    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

}
