package com.tianli.management.tutorial.mapper;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author chensong
 * @date 2021-02-24 14:34
 * @since 1.0.0
 */
@Data
@Builder
public class Tutorial {
    /**
     * 主键id
     */
    private Long id;

    /**
     * 创建时间
     */
    private LocalDateTime create_time;

    /**
     * 标题
     */
    private String title;

    /**
     * 正文
     */
    private String text;

    /**
     * 标题 en
     */
    private String en_title;

    /**
     * 正文 en
     */
    private String en_text;

    /**
     * 标题 泰文
     */
    private String th_title;

    /**
     * 正文 泰文
     */
    private String th_text;

    /**
     * 状态（是否上线）
     */
    private TutorialStatus status;
}
