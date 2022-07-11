package com.tianli.chain.controller;

import com.tianli.chain.mapper.ChainTx;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.currency.enums.CurrencyAdaptType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChainTxDetailVO {
    private Long id;
    private LocalDateTime create_time;
    private LocalDateTime complete_time;
    private ChargeStatus status;
    private Long uid;
    private String sn;
    private CurrencyAdaptType currency_type;
    private Double amount;
    private Double other_amount;
    private String main_address;
    private String collect_address;
    private String txid;
    private String block;

    public static ChainTxDetailVO trans(ChainTx chainTx) {
        ChainTxDetailVO chainTxDetailVO = new ChainTxDetailVO();
        BeanUtils.copyProperties(chainTx, chainTxDetailVO);
        chainTxDetailVO.setAmount(chainTx.getCurrency_type().money(chainTx.getAmount()));
        chainTxDetailVO.setOther_amount(chainTx.getCurrency_type().money(chainTx.getOther_amount()));
        return chainTxDetailVO;
    }
}
