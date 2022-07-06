package com.tianli.user.token;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author wangqiyun
 * @Date 2019/2/26 9:11 PM
 */

@Data
public class UserToken {
    private Long id;
    private Long uid;
    private String imei;
    private String token;
    private LocalDateTime create_time;
    private LocalDateTime update_time;

}
