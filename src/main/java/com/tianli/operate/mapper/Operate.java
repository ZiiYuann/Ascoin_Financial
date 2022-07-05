package com.tianli.operate.mapper;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;


/**
 * 运营位
 * @author linyifan
 *  2/24/21 2:30 PM
 */

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class Operate {

    /**
     * id
     */
    private Long id;

    /**
     * 排序
     */
    private Integer sort;

    /**
     *页面
     */
    private Integer type;

    /**
     *图片
     */
    private String picture;

    /**
     *是否上线
     */
    private Boolean online;

    /**
     *有效期起始时间
     */
    private LocalDateTime start_time;

    /**
     *有效期结束时间
     */
    private LocalDateTime end_time;

    /**
     *创建时间
     */
    private LocalDateTime create_time;

    /**
     * 是否长期有效
     */
    private Boolean validity;

    /**
     *跳转链接
     */
    private String url;
}
