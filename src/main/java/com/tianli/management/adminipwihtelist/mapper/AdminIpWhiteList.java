package com.tianli.management.adminipwihtelist.mapper;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class AdminIpWhiteList {
    private Long id;

    private String ip;

    private String ip_address;

    private String note;

    private Long admin_id;

    private String admin_username;

    private String admin_nickname;

    private Integer is_deleted;

    private LocalDateTime create_time;
}
