package com.tianli.fund.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.common.PageQuery;
import com.tianli.financial.entity.FinancialProduct;
import com.tianli.fund.bo.FundPurchaseBO;
import com.tianli.fund.bo.FundRedemptionBO;
import com.tianli.fund.entity.FundIncomeRecord;
import com.tianli.fund.entity.FundRecord;
import com.tianli.fund.vo.*;
import com.baomidou.mybatisplus.extension.service.IService;

import java.math.BigDecimal;

/**
 * <p>
 * 基金持有记录 服务类
 * </p>
 *
 * @author xianeng
 * @since 2022-08-30
 */
public interface IFundRecordService extends IService<FundRecord> {
    FundMainPageVO mainPage();

    IPage<FundProductVO> productPage(PageQuery<FinancialProduct> page);

    IPage<FundRecordVO> fundRecordPage(PageQuery<FundRecord> page);

    FundApplyPageVO applyPage(Long productId, BigDecimal purchaseAmount);

    void purchase(FundPurchaseBO bo);

    FundRecordVO detail(Long id);

    IPage<FundIncomeRecordVO> incomeRecord(PageQuery<FundIncomeRecord> page , Long fundId);

    FundRecordVO redemptionPage(Long id);

    void applyRedemption(FundRedemptionBO bo);

    BigDecimal dailyIncome(BigDecimal holdAmount,BigDecimal rate);

    FundRecord getOneByUid(Long uid,Long productId);
}
