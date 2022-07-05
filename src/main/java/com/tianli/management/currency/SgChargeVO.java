package com.tianli.management.currency;

import com.tianli.charge.ChargeType;
import com.tianli.management.spot.entity.SGCharge;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * 现货操作记录
 *
 * @author lel
 */
@Data
@Builder
public class SgChargeVO {

    /**
     * 用户名
     */
    private String username;

    /**
     * 交易类型
     * 充值 / 提现
     */
    private String des;

    /**
     * 订单金额
     */
    private BigDecimal amount;

    /**
     * 代币
     */
    private String token;


    public static SgChargeVO trans(SGCharge sgCharge, String username) {
        SgChargeVO vo = SgChargeVO.builder()
                .username(username)
                .des(Objects.equals(sgCharge.getCharge_type().name(), ChargeType.recharge.name()) ? "充值" : "提现")
                .amount(sgCharge.getAmount().setScale(4, RoundingMode.HALF_UP))
                .token(sgCharge.getToken()).build();
        return vo;
    }
}
