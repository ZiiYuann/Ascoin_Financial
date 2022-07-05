package com.tianli.management.kyc;

import com.tianli.kyc.controller.KycInfoVO;
import com.tianli.kyc.mapper.Kyc;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@NoArgsConstructor
public class KycPageInfoVO {
    /**
     * 主键
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String username;

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
     * 审核状态: -1审核失败 / 0待审核 / 1审核成功
     */
    private Integer status;

    /**
     * 审核人
     */
    private String opt_admin;

    private LocalDateTime opt_time;

    /**
     * 备注
     */
    private String node;
    private String en_node;

    /**
     * 前面照
     * 背面照
     * 手持照
     */
    private String front_image;
    private String behind_image;
    private String hold_image;
    private String phone;

    private Boolean phone_check;

    public static KycInfoVO convert(Kyc kyc) {
        KycInfoVO kycInfoVO = new KycInfoVO();
        BeanUtils.copyProperties(kyc, kycInfoVO);
        return kycInfoVO;
    }
}
