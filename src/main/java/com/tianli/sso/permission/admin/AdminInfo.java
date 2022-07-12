package com.tianli.sso.permission.admin;

import lombok.*;
import lombok.experimental.Accessors;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class AdminInfo {

    /**
     * admin主键
     */
    private Long aid;

    /**
     * admin账号
     */
    private String username;

    /**
     * admin账号nick
     */
    private String nickname;

    /**
     * 手机号
     */
    private String phone;
}
