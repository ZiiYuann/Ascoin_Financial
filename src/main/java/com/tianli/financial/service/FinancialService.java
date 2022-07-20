package com.tianli.financial.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.address.mapper.Address;
import com.tianli.charge.enums.ChargeType;
import com.tianli.common.PageQuery;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.financial.dto.FinancialIncomeAccrueDTO;
import com.tianli.financial.entity.FinancialIncomeDaily;
import com.tianli.financial.entity.FinancialProduct;
import com.tianli.financial.entity.FinancialRecord;
import com.tianli.financial.enums.BusinessType;
import com.tianli.financial.enums.ProductType;
import com.tianli.financial.query.PurchaseQuery;
import com.tianli.financial.vo.*;
import com.tianli.management.query.FinancialOrdersQuery;
import com.tianli.management.query.FinancialProductIncomeQuery;
import com.tianli.management.vo.FinancialSummaryDataVO;
import com.tianli.management.vo.FinancialUserInfoVO;

import java.math.BigDecimal;


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
    IPage<HoldProductVo> myHold(IPage<FinancialRecord> page,Long uid, ProductType financialProductType);


    /**
     * 申购的具体每日收益
     */
    IPage<FinancialIncomeDailyVO> incomeDetails(IPage<FinancialIncomeDaily> pageQuery, Long uid , Long recordId);

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

    /**
     * 用户理财收益记录
     */
    IPage<FinancialIncomeAccrueDTO> incomeRecord(Page<FinancialIncomeAccrueDTO> page, FinancialProductIncomeQuery query);

    /**
     *
     */
    FinancialSummaryDataVO summaryIncomeByQuery(FinancialProductIncomeQuery query);

    /**
     * 产品列表
     */
    IPage<FinancialProductVO> products(Page<FinancialProduct> page, ProductType type);

    /**
     * 产品列表
     */
    IPage<FinancialProductVO> activitiesProducts(Page<FinancialProduct> page, BusinessType type);

    /**
     * 理财用户信息
     */
    IPage<FinancialUserInfoVO> user(Long uid, PageQuery<Address> page);

    /**
     * 理财用户信息左上角信息
     */
    FinancialSummaryDataVO userData(Long uid, PageQuery<Address> page);

}
