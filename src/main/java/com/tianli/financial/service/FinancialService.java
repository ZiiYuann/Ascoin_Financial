package com.tianli.financial.service;


import com.tianli.financial.query.PurchaseQuery;
import com.tianli.financial.vo.FinancialPurchaseResultVO;
import com.tianli.financial.vo.IncomeVO;


/**
 * @author chenb
 * @apiNote
 * @since 2022-07-06
 **/
public interface FinancialService {

    /**
     * 申购理财产品
     * @param purchaseQuery 申购请求
     */
    FinancialPurchaseResultVO purchase(PurchaseQuery purchaseQuery);

    /**
     * 统计利息
     */
    IncomeVO income(Long uid);
}
