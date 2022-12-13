package com.tianli.other.vo;

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
@NoArgsConstructor
public class MBannerVO {

    @Id
    private Long id;

    private String name;

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

    private byte status;

    public byte getStatus() {
        LocalDateTime now = LocalDateTime.now();

        if (now.compareTo(startTime) < 0) {
            return 0;
        }
        if (now.compareTo(endTime) > 0) {
            return 2;
        }
        return 1;
    }
}
