package com.tianli.charge.query;

import com.tianli.charge.enums.AdvanceType;
import com.tianli.common.blockchain.NetworkType;
import com.tianli.product.aborrow.query.BorrowCoinQuery;
import com.tianli.product.aborrow.query.ModifyPledgeContextQuery;
import com.tianli.product.aborrow.query.RepayCoinQuery;
import com.tianli.product.afinancial.enums.PurchaseTerm;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2022-08-31
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateOrderAdvanceQuery {

    private BigDecimal amount;

    private Long productId;

    private Long id;

    private String txid;

    private boolean autoCurrent;

    private String coin;

    private PurchaseTerm term;

    private NetworkType network;

    private String referralCode;

    private AdvanceType advanceType = AdvanceType.PURCHASE;

    // 借币
    private BorrowCoinQuery borrowCoinQuery;
    // 还币
    private RepayCoinQuery repayCoinQuery;
    // 调整质押物 增加质押物
    private ModifyPledgeContextQuery modifyPledgeContextQuery;




}
