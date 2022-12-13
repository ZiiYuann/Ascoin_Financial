package com.tianli.other.query;

import com.tianli.common.query.IoUQuery;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
@EqualsAndHashCode(callSuper = true)
public class BannerIoUQuery extends IoUQuery {

    private Long id;

    @NotBlank(message = "名称不允许为空")
    private String name;

    private String nameEn;

    @NotBlank(message = "url中文不允许为空")
    private String urlZh;

    @NotBlank(message = "url英文不允许为空")
    private String urlEn;

    // 跳转类型(1、聊天群2、普通链接3、内部页面)
    @Min(1)
    @Max(3)
    private byte jumpType;

    private String jumpUrl;

    @NotNull(message = "开始时间不允许为null")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不允许为null")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    // 设备类型(0、全部 1、安卓 2、ios)
    private byte deviceType;

    private byte weight;

}
