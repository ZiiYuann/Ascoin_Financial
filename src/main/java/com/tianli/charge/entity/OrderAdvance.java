package com.tianli.charge.entity;

import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.financial.enums.PurchaseTerm;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-08-31
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderAdvance {

    @Id
    private Long id;

    private Long uid;

    private Long productId;

    private CurrencyCoin coin;

    private PurchaseTerm term;

    private String txid;

    private BigDecimal amount;

    private boolean autoCurrent;

    private LocalDateTime createTime;

    private boolean isFinish;

    private int tryTimes;
}
