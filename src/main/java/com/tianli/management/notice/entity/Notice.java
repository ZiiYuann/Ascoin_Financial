package com.tianli.management.notice.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.time.LocalDateTime;
import java.io.Serializable;

import com.tianli.management.tutorial.mapper.TutorialStatus;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 公告表
 * </p>
 *
 * @author cc
 * @since 2022-06-09
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = false)
public class Notice extends Model<Notice> {

    private static final long serialVersionUID=1L;

//    @ApiModelProperty(value = "主键id")
    private Long id;

//    @ApiModelProperty(value = "创建时间")
    private LocalDateTime create_time;

//    @ApiModelProperty(value = "标题")
    private String title;

//    @ApiModelProperty(value = "正文")
    private String text;

//    @ApiModelProperty(value = "状态")
    private NoticeStatus status;

//    @ApiModelProperty(value = "英文 标题")
    private String en_title;

//    @ApiModelProperty(value = "内容 英文")
    private String en_text;

//    @ApiModelProperty(value = "泰文标题")
    private String th_title;

//    @ApiModelProperty(value = "泰文内容")
    private String th_text;


    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
