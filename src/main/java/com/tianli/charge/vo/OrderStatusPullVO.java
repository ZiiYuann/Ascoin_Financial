package com.tianli.charge.vo;

import com.tianli.charge.enums.ChargeStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author chenb
 * @apiNote
 * @since 2022-08-01
 **/
@Data
@AllArgsConstructor
public class OrderStatusPullVO {

    private ChargeStatus key;

    private String des;
}
