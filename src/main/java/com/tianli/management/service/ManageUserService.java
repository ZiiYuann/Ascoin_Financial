package com.tianli.management.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.address.mapper.Address;
import com.tianli.currency.service.CurrencyService;
import com.tianli.management.query.FinancialProductIncomeQuery;
import com.tianli.management.vo.MUserHoldRecordVO;
import com.tianli.product.afinancial.entity.FinancialIncomeAccrue;
import com.tianli.product.afinancial.service.FinancialIncomeAccrueService;
import com.tianli.product.afinancial.service.FinancialRecordService;
import com.tianli.product.afund.query.FundIncomeQuery;
import com.tianli.product.afund.query.FundRecordQuery;
import com.tianli.product.afund.service.IFundIncomeRecordService;
import com.tianli.product.dto.UserHoldRecordDto;
import com.tianli.product.entity.ProductHoldRecord;
import com.tianli.product.afinancial.dto.IncomeDto;
import com.tianli.product.afinancial.enums.ProductType;
import com.tianli.product.afinancial.query.ProductHoldQuery;
import com.tianli.product.afinancial.service.FinancialService;
import com.tianli.product.afund.service.IFundRecordService;
import com.tianli.product.afund.vo.FundMainPageVO;
import com.tianli.management.converter.ManagementConverter;
import com.tianli.management.vo.FinancialUserInfoVO;
import com.tianli.management.vo.MUserListVO;
import com.tianli.product.service.ProductHoldRecordService;
import com.tianli.product.service.ProductService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author chenb
 * @apiNote
 * @since 2023-01-30
 **/
@Service
public class ManageUserService {

    @Resource
    private FinancialService financialService;
    @Resource
    private ManagementConverter managementConverter;
    @Resource
    private IFundRecordService fundRecordService;
    @Resource
    private ProductHoldRecordService productHoldRecordService;
    @Resource
    private ProductService productService;
    @Resource
    private CurrencyService currencyService;
    @Resource
    private IFundIncomeRecordService fundIncomeRecordService;
    @Resource
    private FinancialIncomeAccrueService financialIncomeAccrueService;

    /**
     * 理财用户信息y
     */
    public IPage<MUserListVO> financialUserPage(String queryUid, IPage<Address> page) {
        IPage<FinancialUserInfoVO> financialUserInfoVOIPage = financialService.financialUserPage(queryUid, page);

        return financialUserInfoVOIPage.convert(financialUserInfoVO -> {
            MUserListVO mUserListVO = managementConverter.toManagerUserListVO(financialUserInfoVO);
            Long uid = financialUserInfoVO.getUid();

            FundMainPageVO fundMainPageVO = fundRecordService.mainPage(uid);
            mUserListVO.setWaitIncomeAmount(fundMainPageVO.getWaitPayInterestAmount());
            mUserListVO.setCalIncomeAmount(fundMainPageVO.getPayInterestAmount().add(fundMainPageVO.getWaitPayInterestAmount()));
            return mUserListVO;
        });
    }

    public IPage<MUserHoldRecordVO> userHoldRecordPage(ProductHoldQuery query, Page<ProductHoldRecord> page) {
        IPage<UserHoldRecordDto> userHoldRecordDtoIPage = productHoldRecordService.userHoldRecordPage(query, page);
        return userHoldRecordDtoIPage.convert(userHoldRecordDto -> {
            Long uid = userHoldRecordDto.getUid();
            return mUserHoldRecordVO(userHoldRecordDto.getRecords(), uid);
        });
    }

    public MUserHoldRecordVO userHoldRecordData(ProductHoldQuery query) {
        var records = productHoldRecordService.userHoldRecordData(query)
                .stream()
                .flatMap(r -> r.getRecords().stream()).collect(Collectors.toList());
        return mUserHoldRecordVO(records, null);
    }

    private MUserHoldRecordVO mUserHoldRecordVO(List<ProductHoldRecord> records, Long uid) {
        BigDecimal holdFee = BigDecimal.ZERO;
        Map<ProductType, String> holdFeeMap = new HashMap<>(records.size());
        Map<ProductType, BigDecimal> holdFeeBigDecimalMap = new HashMap<>(records.size());
        BigDecimal calIncomeFee = BigDecimal.ZERO;
        BigDecimal waitIncomeFee = BigDecimal.ZERO;

        BigDecimal fundIncomeFee =
                fundIncomeRecordService.amountDollar(FundIncomeQuery.builder().uid(uid).build());
        BigDecimal financialIncomeFee =
                financialIncomeAccrueService.summaryIncomeByQuery(FinancialProductIncomeQuery.builder().uid(uid + "").build());

        for (ProductHoldRecord record : records) {
            Long productId = record.getProductId();
            ProductType productType = record.getProductType();
            Long recordId = record.getRecordId();
            IncomeDto income = productService.income(productType, record.getUid(), productId, recordId);
            BigDecimal dollarRate = currencyService.getDollarRate(income.getCoin());

            holdFee = holdFee.add(income.getHoldAmount().multiply(dollarRate));

            BigDecimal holdFeeByProductType = holdFeeBigDecimalMap.getOrDefault(productType, BigDecimal.ZERO);
            holdFeeByProductType = holdFeeByProductType.add(income.getHoldAmount().multiply(dollarRate));
            holdFeeMap.put(productType, holdFeeByProductType.setScale(2, RoundingMode.HALF_DOWN).toPlainString());
            holdFeeBigDecimalMap.put(productType, holdFeeByProductType);

            calIncomeFee = calIncomeFee.add(income.getCalIncomeAmount().multiply(dollarRate));

            waitIncomeFee = waitIncomeFee.add(income.getWaitIncomeAmount().multiply(dollarRate));

        }

        return MUserHoldRecordVO.builder()
                .uid(uid)
                .holdFee(holdFee)
                .holdFeeMap(holdFeeMap)
                .calIncomeFee(calIncomeFee)
                .waitIncomeFee(waitIncomeFee)
                .accrueIncomeFee(fundIncomeFee.add(financialIncomeFee))
                .build();
    }


}
