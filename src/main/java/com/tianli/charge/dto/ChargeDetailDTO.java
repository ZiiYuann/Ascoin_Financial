package com.tianli.charge.dto;

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
public class ChargeDetailDTO {
    private Long id;
    private String sn;
    private LocalDateTime create_time;
    private ChargeStatus status;
    private double amount;
    private double fee;
    private double real_amount;
    private String from_address;
    private String to_address;
    private String txid;

    public static ChargeDetailDTO trans(Charge charge) {
        TokenCurrencyType currency_type = charge.getCurrency_type();
        return ChargeDetailDTO.builder()
                .id(charge.getId())
                .sn(charge.getSn())
                .create_time(charge.getCreate_time())
                .status(charge.getStatus())
                .amount(currency_type.money(charge.getAmount()))
                .fee(currency_type.money(charge.getFee()))
                .real_amount(currency_type.money(charge.getReal_amount()))
                .from_address(charge.getFrom_address())
                .to_address(charge.getTo_address())
                .txid(charge.getTxid())
                .build();
    }
}
