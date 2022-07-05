package com.tianli.management.fundmanagement;

import com.tianli.charge.mapper.Charge;
import com.tianli.currency.CurrencyTokenEnum;
import com.tianli.currency.TokenCurrencyType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @Author wangqiyun
 * @Date 2020/3/31 11:26
 */


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RechargeManageVO {
    private Long id;
    private LocalDateTime create_time;
    private String uid_username;
    private String uid_nick;
    private double amount;
    private CurrencyTokenEnum token;
    private TokenCurrencyType currency_type;
    private String from_address;
    private String to_address;
    private String txid;
    public static RechargeManageVO trans(Charge charge){
        BigInteger amount = charge.getAmount();
        CurrencyTokenEnum token = charge.getToken();
        double amount_;
//        if(Objects.equals(token, CurrencyTokenEnum.usdt_omni)){
            amount_ = charge.getCurrency_type().money(Objects.isNull(amount) ? BigInteger.ZERO : amount);
//        }else{
//            amount_ = CurrencyTokenEnum.BF_bep20.money(amount);
//        }
        return RechargeManageVO.builder()
                .id(charge.getId())
                .create_time(charge.getCreate_time())
                .uid_username(charge.getUid_username())
                .uid_nick(charge.getUid_nick())
                .token(token)
                .currency_type(charge.getCurrency_type())
                .amount(amount_)
                .from_address(charge.getFrom_address())
                .to_address(charge.getTo_address())
                .txid(charge.getTxid())
                .build();

    }
}
