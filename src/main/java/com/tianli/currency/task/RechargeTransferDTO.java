package com.tianli.currency.task;

import com.tianli.currency.CurrencyTokenEnum;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.currency.TransferGraphVO;
import lombok.Builder;
import lombok.Data;

import java.math.BigInteger;

@Data
@Builder
public class RechargeTransferDTO {
    private String txid;
    private String from;
    private String to;
    private BigInteger block;
    private BigInteger value;
    private BigInteger transferTime;
    private TokenCurrencyType currency_type;
    private Long uid;
    private BigInteger fee;
    private CurrencyTokenEnum token;
    private TokenCurrencyType feeType;
    private System username;
    private System nick;

    public static RechargeTransferDTO trans(TransferGraphVO transferGraphVO) {
        return RechargeTransferDTO.builder()
                .txid(transferGraphVO.getId())
                .from(transferGraphVO.getFrom())
                .to(transferGraphVO.getTo())
                .block(transferGraphVO.getBlock())
                .value(transferGraphVO.getValue())
                .transferTime(transferGraphVO.getTransferTime())
                .build();
    }
}
