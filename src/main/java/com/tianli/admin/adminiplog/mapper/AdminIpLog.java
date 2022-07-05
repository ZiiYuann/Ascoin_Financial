package com.tianli.admin.adminiplog.mapper;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * <p>
 *      管理员登录 ip记录表
 * </P>
 *
 * @author linyifan
 * @since 5/12/21 11:32 AM
 */

@Data
@Builder
public class AdminIpLog {

    /**
     * 主键id
     */
    private Long id;

    /**
     * 管理员id
     */
    private Long admin_id;

    /**
     * 登录时间
     */
    private LocalDateTime create_time;

    /**
     * ip地址
     */
    private String last_ip;

}
