package com.tianli.sso.admin;

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
     * 手机号
     */
    private String phone;
}
