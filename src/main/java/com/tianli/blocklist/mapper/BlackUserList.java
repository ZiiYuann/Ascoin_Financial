package com.tianli.blocklist.mapper;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class BlackUserList {

    /**
     * 主键
     */
    private Long id;

    /**
     * 创建时间
     */
    private LocalDateTime create_time;

    /**
     * 更新时间
     */
    private LocalDateTime update_time;

    /**
     * 用户id
     */
    private Long uid;

    /**
     * 用户账号昵称
     */
    private String username;
    private String nick;

    /**
     * 操作管理员信息
     */
    private String opt_admin;
    private Long opt_admin_id;

    /**
     * 备注
     */
    private String node;

    /**
     * 输赢: 0输1赢
     */
    private Integer control;

}
