package com.tianli.charge.dto;

import com.tianli.charge.ChargeType;
import com.tianli.charge.mapper.Charge;
import com.tianli.charge.mapper.ChargeStatus;
import com.tianli.currency.TokenCurrencyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @Author wangqiyun
 * @Date 2020/3/31 11:26
 */


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChargeListDTO {
    private Long id;
    private LocalDateTime create_time;
    private LocalDateTime complete_time;
    private ChargeStatus status;
    private Long uid;
    private String sn;
    private TokenCurrencyType currency_type;
    private ChargeType charge_type;
    private double amount;
    private double fee;
    private double real_amount;
    private String from_address;
    private String to_address;
    private String txid;
    private String node;

    public static ChargeListDTO trans(Charge charge) {
        TokenCurrencyType currency_type = charge.getCurrencyType();
        return ChargeListDTO.builder()
                .id(charge.getId())
                .create_time(charge.getCreateTime())
                .complete_time(charge.getCompleteTime())
                .status(charge.getStatus())
                .uid(charge.getUid())
                .sn(charge.getSn())
                .currency_type(currency_type)
                .charge_type(charge.getChargeType())
                .amount(currency_type.money(charge.getAmount()))
                .fee(currency_type.money(charge.getFee()))
                .real_amount(currency_type.money(charge.getRealAmount()))
                .from_address(charge.getFromAddress())
                .to_address(charge.getToAddress())
                .txid(charge.getTxid())
                .node(charge.getNote())
                .build();
    }
}
