package com.tianli.product.afinancial.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.address.mapper.Address;
import com.tianli.management.query.FinancialOrdersQuery;
import com.tianli.management.query.FinancialProductIncomeQuery;
import com.tianli.management.query.TimeQuery;
import com.tianli.management.vo.*;
import com.tianli.product.afinancial.dto.FinancialIncomeAccrueDTO;
import com.tianli.product.afinancial.entity.FinancialIncomeDaily;
import com.tianli.product.afinancial.entity.FinancialProduct;
import com.tianli.product.afinancial.enums.ProductType;
import com.tianli.product.afinancial.query.ProductHoldQuery;
import com.tianli.product.afinancial.vo.*;

import java.util.List;


/**
 * @author chenb
 * @apiNote
 * @since 2022-07-06
 **/
public interface FinancialService {

    /**
     * 统计利息
     */
    DollarIncomeVO income(Long uid);

    /**
     * 单条记录的收益信息
     */
    RecordIncomeVO recordIncome(Long uid, Long recordId);

    /**
     * 我的持有
     */
    IPage<HoldProductVo> holdProductPage(IPage<FinancialProduct> page, ProductHoldQuery query);

    /**
     * 我的持有
     */
    IPage<?> holdProduct(IPage<FinancialProduct> page, Long uid, ProductType financialProductType);

    /**
     * 我的持有
     */
    IPage<TransactionRecordVO> transactionRecordPage(IPage<FinancialProduct> page, Long uid, ProductType financialProductType);

    /**
     * 申购的具体每日收益
     */
    IPage<FinancialIncomeDailyVO> dailyIncomePage(IPage<FinancialIncomeDaily> pageQuery, Long uid, Long recordId);

    /**
     * 获取订单记录信息
     */
    IPage<OrderFinancialVO> orderPage(Page<OrderFinancialVO> page, FinancialOrdersQuery financialOrdersQuery);

    /**
     * 用户理财收益记录
     */
    IPage<FinancialIncomeAccrueDTO> incomeRecordPage(Page<FinancialIncomeAccrueDTO> page, FinancialProductIncomeQuery query);

    /**
     * 用户理财收益记录列表累计信息
     */
    FinancialSummaryDataVO incomeSummaryData(FinancialProductIncomeQuery query);

    /**
     * 汇总产品列表
     */
    IPage<RateScopeVO> summaryProducts(Page<FinancialProduct> page, ProductType productType);

    /**
     * 产品列表
     */
    IPage<FinancialProductVO> products(Page<FinancialProduct> page, ProductType type);

    /**
     * 推荐产品列表
     */
    List<RecommendProductVO> recommendProducts();

    /**
     * 理财用户信息
     */
    IPage<FinancialUserInfoVO> financialUserPage(String uid, IPage<Address> page);

    /**
     * 理财用户信息左上角信息
     */
    MWalletUserManagerDataVO mWalletUserManagerData(String uid);

    /**
     * 手动更新数据展板
     */
    void boardManual(TimeQuery query);

    /**
     * 活期产品详情
     */
    CurrentProductPurchaseVO currentProductDetails(Long productId);

    /**
     * 定期产品详情
     */
    FixedProductsPurchaseVO fixedProductDetails(String coin);

    /**
     * 基金需要能够绑定的产品下拉
     */
    List<FundProductBindDropdownVO> fundProductBindDropdownList(ProductType type);

    /**
     * 用户余额详情
     */
    UserAmountDetailsVO userAmountDetailsVO(Long uid);

    /**
     * 产品信息（是否售罄、是否新用户等）
     *
     * @param uid       用户uid
     * @param productId 产品id
     * @return 产品信息
     */
    ProductInfoVO productExtraInfo(Long uid, Long productId);
}
