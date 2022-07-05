package com.tianli.management.fundmanagement;

import com.tianli.currency.CurrencyTokenEnum;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class RebateManageVO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 创建时间
     */
    private LocalDateTime create_time;

    /**
     * 返佣金额
     */
    private double rebate_amount;

    /**
     * 返佣用户id
     */
    private Long rebate_uid;

    /**
     * 返佣用户手机号
     */
    private String rebate_uid_phone;

    /**
     * 反佣用户昵称
     */
    private String rebate_uid_nick;

    private CurrencyTokenEnum token;
}
