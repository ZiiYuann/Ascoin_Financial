package com.tianli.financial.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.address.mapper.Address;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.financial.dto.FinancialIncomeAccrueDTO;
import com.tianli.financial.entity.FinancialIncomeDaily;
import com.tianli.financial.entity.FinancialProduct;
import com.tianli.financial.enums.ProductType;
import com.tianli.financial.vo.*;
import com.tianli.management.query.FinancialOrdersQuery;
import com.tianli.management.query.FinancialProductIncomeQuery;
import com.tianli.management.query.TimeQuery;
import com.tianli.management.vo.FinancialSummaryDataVO;
import com.tianli.management.vo.FinancialUserInfoVO;
import com.tianli.management.vo.FundProductBindDropdownVO;
import com.tianli.management.vo.UserAmountDetailsVO;
import com.tianli.management.vo.FundProductBindDropdownVO;

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
    IPage<HoldProductVo> holdProductPage(IPage<FinancialProduct> page, Long uid, ProductType financialProductType);

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
    FinancialSummaryDataVO userSummaryData(String uid);

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
    FixedProductsPurchaseVO fixedProductDetails(CurrencyCoin coin);

    /**
     * 基金需要能够绑定的产品下拉
     */
    List<FundProductBindDropdownVO> fundProductBindDropdownList(ProductType type);

    /**
     * 用户余额详情
     */
    UserAmountDetailsVO userAmountDetailsVO(Long uid);
}
