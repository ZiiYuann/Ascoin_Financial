package com.tianli.management.kyc;

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
public class KycPageVO {
    /**
     * 主键
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String username;
    private String nick;

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

    private String phone;

    private Boolean phone_check;

    public static KycPageVO convert(Kyc kyc) {
        KycPageVO kycInfoVO = new KycPageVO();
        BeanUtils.copyProperties(kyc, kycInfoVO);
        return kycInfoVO;
    }
}
