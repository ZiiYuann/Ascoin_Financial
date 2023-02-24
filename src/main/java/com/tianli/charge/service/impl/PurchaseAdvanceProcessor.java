package com.tianli.charge.service.impl;

import com.tianli.charge.entity.OrderAdvance;
import com.tianli.charge.enums.AdvanceType;
import com.tianli.charge.query.GenerateOrderAdvanceQuery;
import com.tianli.charge.service.OrderAdvanceProcessor;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.management.entity.WalletAgentProduct;
import com.tianli.management.service.IWalletAgentProductService;
import com.tianli.product.afinancial.entity.FinancialProduct;
import com.tianli.product.afinancial.enums.ProductType;
import com.tianli.product.service.FinancialProductService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-24
 **/
@Service
public class PurchaseAdvanceProcessor implements OrderAdvanceProcessor {

    @Resource
    private FinancialProductService financialProductService;
    @Resource
    private IWalletAgentProductService walletAgentProductService;

    @Override
    public AdvanceType getType() {
        return AdvanceType.PURCHASE;
    }

    @Override
    public void verifier(GenerateOrderAdvanceQuery query) {

        FinancialProduct product = financialProductService.getById(query.getProductId());
        if (!ProductType.fund.equals(product.getType())) {
            return;
        }

        WalletAgentProduct walletAgentProduct = walletAgentProductService.getByProductId(product.getId());
        if (!walletAgentProduct.getReferralCode().equals(query.getReferralCode())) {
            ErrorCodeEnum.REFERRAL_CODE_ERROR.throwException();
        }

    }

    @Override
    public void preInsertProcess(GenerateOrderAdvanceQuery query, OrderAdvance orderAdvance) {
        orderAdvance.setProductId(query.getProductId());
        orderAdvance.setCoin(query.getCoin());
        orderAdvance.setTerm(query.getTerm());
        orderAdvance.setTerm(query.getTerm());
    }
}
