package com.tianli.financial.service.impl;

import com.tianli.common.CommonFunction;
import com.tianli.common.init.RequestInitService;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.account.enums.ProductType;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.account.entity.AccountBalance;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.financial.enums.FinancialProductStatus;
import com.tianli.financial.query.PurchaseQuery;
import com.tianli.financial.service.FinancialLogService;
import com.tianli.financial.entity.FinancialLog;
import com.tianli.financial.entity.FinancialProduct;
import com.tianli.financial.enums.FinancialLogStatus;
import com.tianli.financial.service.FinancialProductService;
import com.tianli.financial.service.FinancialService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigInteger;
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

    @Override
    @Transactional
    public void purchase(PurchaseQuery purchaseQuery) {
        Long uid = requestInitService.uid();
        FinancialProduct product = financialProductService.getById(purchaseQuery.getProductId());

        validProduct(product);
        // TODO 转换额度 amount
        BigInteger amount = BigInteger.ZERO;
        validRemainAmount(purchaseQuery.getProductId(),amount);

        Long id = CommonFunction.generalId();
        accountBalanceService.freeze(uid, ProductType.financial, amount, id.toString(), CurrencyLogDes.买入.name());
        LocalDate start_date = requestInitService.now().toLocalDate().plusDays(1L);
        FinancialLog userFinancialLog = FinancialLog.builder()
                .financialProductId(product.getId())
                .userId(uid).financialProductType(product.getType())
                .amount(amount)
                .createTime(requestInitService.now())
                .startDate(start_date)
                .endDate(start_date.plusDays(product.getPeriod()))
                .id(id).rate(product.getRate()).status(FinancialLogStatus.PURCHASE_PROCESSING.getType())
                .build();
        userFinancialLogService.save(userFinancialLog);
    }

    /**
     * 校验产品是否处于开启状态
     * @param financialProduct 产品
     */
    private void validProduct(FinancialProduct financialProduct){
        if( Objects.isNull(financialProduct) || FinancialProductStatus.enable.getType() != financialProduct.getStatus()){
            ErrorCodeEnum.NOT_OPEN.throwException();
        }
    }

    /**
     * 校验账户额度
     * @param amount 申购金额
     */
    private void validRemainAmount(Long uid,BigInteger amount){
        AccountBalance accountBalanceBalance = accountBalanceService.getAndInit(uid, ProductType.financial);
        if(accountBalanceBalance.getRemain().compareTo(amount) < 0){
            ErrorCodeEnum.CREDIT_LACK.throwException();
        }
    }
}
