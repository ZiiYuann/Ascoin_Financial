package com.tianli.other.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

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
public class MBannerListQuery {

    private String name;

    private String jumpUrl;

    /**
     * 0：未开始 1：进行中 2：过期
     */
    private Byte status;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

}
