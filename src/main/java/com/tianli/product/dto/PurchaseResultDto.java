package com.tianli.product.dto;

import com.tianli.product.financial.vo.FinancialPurchaseResultVO;
import com.tianli.product.fund.vo.FundTransactionRecordVO;
import lombok.Builder;
import lombok.Data;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-02
 **/
@Data
@Builder
public class PurchaseResultDto {

    private FinancialPurchaseResultVO financialPurchaseResultVO;

    private FundTransactionRecordVO fundTransactionRecordVO;

    private Long recordId;
}
