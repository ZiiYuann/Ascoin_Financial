package com.tianli.charge.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-21
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderRedeemDetailsVO extends OrderBaseVO {

    /**
     * 赎回时间
     */
    private LocalDateTime redeemTime;

    /**
     * 赎回到账时间
     */
    private LocalDateTime redeemEndTime;

}
