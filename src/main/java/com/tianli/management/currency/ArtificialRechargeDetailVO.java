package com.tianli.management.currency;

import com.tianli.currency.mapper.ArtificialRechargeType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author chensong
 * @date 2021-02-25 15:37
 * @since 1.0.0
 */
@Data
@Builder
public class ArtificialRechargeDetailVO {
    /**
     * 充值（撤回）金额
     */
    private double amount;

    /**
     * 充值操作员
     */
    private String recharge_admin_nick;

    /**
     * 充值时间
     */
    private LocalDateTime create_time;

    /**
     * 交易凭证
     */
    private String voucher_image;

    /**
     * 备注
     */
    private String remark;

   private ArtificialRechargeType type;
}
