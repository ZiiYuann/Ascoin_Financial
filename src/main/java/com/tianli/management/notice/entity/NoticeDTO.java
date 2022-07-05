package com.tianli.management.notice.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 公告表
 * </p>
 *
 * @author cc
 * @since 2022-06-09
 */
@Data
public class NoticeDTO extends Model<NoticeDTO> {
    private static final long serialVersionUID=1L;
    private String startTime;//开始时间
    private String endTime;//创建时间
    private NoticeStatus status;//状态：0下线  1上线
    private String title;//标题
    private Integer page;
    private Integer size;
}
