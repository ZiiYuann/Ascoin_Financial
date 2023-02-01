package com.tianli.product.fund.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.common.PageQuery;
import com.tianli.management.dto.AmountDto;
import com.tianli.management.vo.FundUserRecordVO;
import com.tianli.management.vo.HoldUserAmount;
import com.tianli.product.financial.entity.FinancialProduct;
import com.tianli.product.fund.bo.FundPurchaseBO;
import com.tianli.product.fund.bo.FundRedemptionBO;
import com.tianli.product.fund.entity.FundIncomeRecord;
import com.tianli.product.fund.entity.FundRecord;
import com.tianli.product.fund.entity.FundTransactionRecord;
import com.tianli.product.fund.query.FundIncomeQuery;
import com.tianli.product.fund.query.FundRecordQuery;
import com.tianli.product.fund.query.FundTransactionQuery;
import com.tianli.product.fund.vo.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 基金持有记录 服务类
 * </p>
 *
 * @author xianeng
 * @since 2022-08-30
 */
public interface IFundRecordService extends IService<FundRecord> {

    FundMainPageVO mainPage(Long uid);

    IPage<FundProductVO> productPage(PageQuery<FinancialProduct> page);

    IPage<FundRecordVO> fundRecordPage(PageQuery<FundRecord> page);

    FundApplyPageVO applyPage(Long productId, BigDecimal purchaseAmount);

    FundRecordVO detail(Long uid, Long id);

    IPage<FundIncomeRecordVO> incomeRecord(PageQuery<FundIncomeRecord> page, FundIncomeQuery query);

    IPage<FundIncomeRecordVO> incomeSummary(PageQuery<FundIncomeRecord> page, FundIncomeQuery query);

    FundRecordVO redemptionPage(Long id);

    IPage<FundTransactionRecordVO> transactionRecord(PageQuery<FundTransactionRecord> page, FundTransactionQuery query);

    FundTransactionRecordVO transactionDetail(Long transactionId);

    IPage<FundUserRecordVO> fundUserRecordPage(PageQuery<FundRecord> pageQuery, FundRecordQuery query);

    HoldUserAmount fundUserAmount(FundRecordQuery query);

    FundTransactionRecordVO applyRedemption(FundRedemptionBO bo);

    /**
     * 获取持用金额( u )
     */
    BigDecimal dollarHold(FundRecordQuery query);

    List<AmountDto> hold(FundRecordQuery query);

    /**
     * 获取持用单币种金额
     */
    BigDecimal holdSingleCoin(Long uid, String coin, Long agentId);


    Integer getHoldUserCount(FundRecordQuery query);

    void increaseAmount(Long id, BigDecimal amount);

    void updateRateByProductId(Long id, BigDecimal rate);

    List<FundRecord> listByUidAndProductId(Long uid, Long productId);

    /**
     * 获取用户累计收益
     *
     * @param uids 用户id列表
     * @return 累计收益
     */
    Map<Long, BigDecimal> accrueIncomeAmount(List<Long> uids);


}
