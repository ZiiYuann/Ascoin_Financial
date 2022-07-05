package com.tianli.kyc.controller;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@NoArgsConstructor
public class KycUpdateCmd {
    /**
     * 主键
     */
    private Long id;

    /**
     * 真实姓名
     */
    private String real_name;

    /**
     * 国家地区
     */
    private String country;

    /**
     * 证件类型
     */
    private String certificate_type;

    /**
     * 证件号码
     */
    private String certificate_no;

    /**
     * 前面照
     * 背面照
     * 手持照
     */
    private String front_image;
    private String behind_image;
    private String hold_image;

    private String phone;

    private String phone_code;
}
