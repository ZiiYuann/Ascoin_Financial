package com.tianli.charge.entity;

import com.tianli.charge.enums.AdvanceType;
import com.tianli.common.blockchain.NetworkType;
import com.tianli.product.afinancial.enums.PurchaseTerm;
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

    private String coin;

    private PurchaseTerm term;

    private String txid;

    private BigDecimal amount;

    private boolean autoCurrent;

    private LocalDateTime createTime;

    private NetworkType network;

    // 0 未完成 1完成 2失败
    private int finish;

    private int tryTimes;

    private AdvanceType advanceType;

    private String query;

}
