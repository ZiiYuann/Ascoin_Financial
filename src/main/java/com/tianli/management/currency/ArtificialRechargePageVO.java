package com.tianli.management.currency;

import com.tianli.currency.TokenCurrencyType;
import com.tianli.currency.mapper.ArtificialRecharge;
import com.tianli.currency.mapper.ArtificialRechargeType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;


/**
 * @author chensong
 * @date 2021-02-25 14:51
 * @since 1.0.0
 */
@Data
@Builder
public class ArtificialRechargePageVO {
    /**
     * 充值id
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nick;

    /**
     * 类型
     */
    private ArtificialRechargeType type;

    /**
     * 充值数额
     */
    private double amount;

    /**
     * 操作员
     */
    private String admin_nick;

    /**
     * 操作时间
     */
    private LocalDateTime create_time;

    public static ArtificialRechargePageVO trans(ArtificialRecharge dto){
        ArtificialRechargePageVO vo = ArtificialRechargePageVO.builder()
                .id(dto.getId())
                .username(dto.getUsername())
                .type(dto.getType())
                .amount(TokenCurrencyType.usdt_omni.money(dto.getAmount()))
                .admin_nick(dto.getRecharge_admin_nick())
                .create_time(dto.getCreate_time())
                .build();
        return vo;
    }
}
