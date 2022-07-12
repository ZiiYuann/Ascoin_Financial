package com.tianli.financial.service.impl;

import com.tianli.common.CommonFunction;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.account.enums.AccountChangeType;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.account.entity.AccountBalance;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.financial.convert.FinancialConverter;
import com.tianli.financial.enums.FinancialProductStatus;
import com.tianli.financial.query.PurchaseQuery;
import com.tianli.financial.service.FinancialLogService;
import com.tianli.financial.entity.FinancialLog;
import com.tianli.financial.entity.FinancialProduct;
import com.tianli.financial.enums.FinancialLogStatus;
import com.tianli.financial.service.FinancialProductService;
import com.tianli.financial.service.FinancialService;
import com.tianli.financial.vo.FinancialPurchaseResultVO;
import com.tianli.sso.RequestInitService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Service
public class FinancialServiceImpl implements FinancialService {

    @Resource
    private AccountBalanceService accountBalanceService;
    @Resource
    private RequestInitService requestInitService;
    @Resource
    private FinancialLogService userFinancialLogService;
    @Resource
    private FinancialProductService financialProductService;
    @Resource
    private FinancialConverter financialConverter;

    @Override
    @Transactional
    public FinancialPurchaseResultVO purchase(PurchaseQuery purchaseQuery) {
        Long uid = requestInitService.uid();

        FinancialProduct product = financialProductService.getById(purchaseQuery.getProductId());
        validProduct(product);

        BigDecimal amount = purchaseQuery.getAmount();
        validRemainAmount(purchaseQuery.getProductId(),purchaseQuery.getCurrencyCoin(),amount);

        Long id = CommonFunction.generalId();
        accountBalanceService.freeze(uid, AccountChangeType.financial, amount, id.toString(), CurrencyLogDes.申购.name());
        LocalDate startDate = requestInitService.now().toLocalDate().plusDays(1L);
        FinancialLog log = FinancialLog.builder()
                .financialProductId(product.getId())
                .userId(uid).financialProductType(product.getType())
                .amount(amount)
                .createTime(requestInitService.now())
                .startDate(startDate)
                .endDate(startDate.plusDays(product.getPurchaseTerm().getDay()))
                .id(id).rate(product.getRate()).status(FinancialLogStatus.PURCHASE_PROCESSING.getType())
                .build();
        userFinancialLogService.save(log);

        FinancialPurchaseResultVO financialPurchaseResultVO = financialConverter.toVO(log);
        financialPurchaseResultVO.setName(product.getName());
        financialPurchaseResultVO.setName(product.getNameEn());
        financialPurchaseResultVO.setStatusDes(FinancialLogStatus.getByType(log.getStatus()).getDesc());
        return financialPurchaseResultVO;
    }

    /**
     * 校验产品是否处于开启状态
     * @param financialProduct 产品
     */
    private void validProduct(FinancialProduct financialProduct){
        if( Objects.isNull(financialProduct) || FinancialProductStatus.enable != financialProduct.getStatus()){
            ErrorCodeEnum.NOT_OPEN.throwException();
        }
    }

    /**
     * 校验账户额度
     * @param amount 申购金额
     */
    private void validRemainAmount(Long uid, CurrencyCoin currencyCoin, BigDecimal amount){
        AccountBalance accountBalanceBalance = accountBalanceService.getAndInit(uid,currencyCoin);
        if(accountBalanceBalance.getRemain().compareTo(amount) < 0){
            ErrorCodeEnum.CREDIT_LACK.throwException();
        }
    }
}
