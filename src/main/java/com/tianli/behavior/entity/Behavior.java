package com.tianli.behavior.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.tianli.behavior.enums.EventType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author lzy
 * @date 2022/5/10 14:16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class Behavior extends Model<Behavior> {

    private static final long serialVersionUID = 1L;


    private Long id;

    /**
     * 用户id
     */
    private String user_id;
    /**
     * 设备id
     */
    private String device_id;
    /**
     * 产品名称
     */
    private String app_name;
    /**
     * 环境
     */
    private String build_type;
    /**
     * 是否成功
     */
    private Boolean is_success;
    /**
     * 失败原因
     */
    private String fail_reason;
    /**
     * 页面停留时长
     */
    private Long event_duration;
    /**
     * 屏幕高度
     */
    private BigDecimal screen_height;
    /**
     * 屏幕宽度
     */
    private BigDecimal screen_width;
    /**
     * 是否首日访问
     */
    private Boolean is_first_day;
    /**
     * 是否首次访问
     */
    private Boolean is_first_time;
    /**
     * 平台类型
     */
    private String platform_type;
    /**
     * 前向域名
     */
    private String referrer_host;
    /**
     * 前向地址
     */
    private String referrer;

    private LocalDateTime create_time;

    private LocalDateTime update_time;

    private String page;

    /**
     * 事件类型
     */
    private EventType event_type;
    /**
     * 属性类型
     */
    private String property_type;
    /**
     * 属性值
     */
    private String property_key;

    private String property_value;

    private String property_value_desc;

}
