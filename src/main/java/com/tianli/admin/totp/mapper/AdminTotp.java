package com.tianli.admin.totp.mapper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author wangqiyun
 * @Date 2020/6/1 17:54
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminTotp {
    private Long id;
    private String secret;
}
