package com.tianli.management.channel.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @author lzy
 * @date 2022/4/6 4:05 下午
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class Channel extends Model<Channel> {

    private static final long serialVersionUID = 1L;

    private Long id;

    private Long admin_id;

    private String admin_username;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Long p_id;

    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String remark;

    private String creator;

    private Long creator_id;

    private LocalDateTime create_time;

    private LocalDateTime update_time;

    @TableLogic
    private Boolean is_deleted;
}
