package com.tianli.financial.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.charge.enums.ChargeType;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.financial.entity.FinancialProduct;
import com.tianli.financial.enums.ProductType;
import com.tianli.financial.query.PurchaseQuery;
import com.tianli.financial.vo.*;
import com.tianli.management.query.FinancialOrdersQuery;

import java.math.BigDecimal;
import java.util.List;


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

    /**
     * 我的持有列表信息
     */
    List<HoldProductVo> myHold(Long uid, ProductType financialProductType);


    /**
     * 申购的具体每日收益
     */
    List<DailyIncomeLogVO> incomeDetails(Long uid , Long recordId);

    /**
     * 校验产品是否处于开启状态
     * @param financialProduct 产品
     */
    void validProduct(FinancialProduct financialProduct);

    /**
     * 校验账户额度
     * @param amount 申购金额
     */
    void validRemainAmount(Long uid, CurrencyCoin currencyCoin, BigDecimal amount);

    /**
     * 获取交易记录信息
     */
    IPage<OrderFinancialVO> orderPage(Long uid, Page<OrderFinancialVO> page, ProductType productType, ChargeType chargeType);

    /**
     * 获取订单记录信息
     */
    IPage<OrderFinancialVO> orderPage(Page<OrderFinancialVO> page, FinancialOrdersQuery financialOrdersQuery);

}
