package com.tianli.product.service;

import com.tianli.charge.entity.Order;
import com.tianli.charge.query.RedeemQuery;
import com.tianli.product.dto.PurchaseResultDto;
import com.tianli.product.dto.RedeemResultDto;
import com.tianli.product.afinancial.dto.IncomeDto;
import com.tianli.product.afinancial.entity.FinancialProduct;
import com.tianli.product.afinancial.mapper.FinancialProductMapper;
import com.tianli.product.afinancial.query.PurchaseQuery;
import com.tianli.product.afinancial.service.AbstractProductOperation;
import com.tianli.product.afinancial.vo.ExpectIncomeVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-02
 **/
@Service
public class ProductService extends AbstractProductOperation<FinancialProductMapper, FinancialProduct> {
    @Override
    public PurchaseResultDto purchaseOperation(Long uid, PurchaseQuery purchaseQuery, Order order) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RedeemResultDto redeemOperation(Long uid, RedeemQuery redeemQuery) {
        throw new UnsupportedOperationException();
    }

    @Override
    public IncomeDto incomeOperation(Long uid, Long productId, Long record) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ExpectIncomeVO exceptDailyIncome(Long uid, Long productId, Long recordId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BigDecimal incomeRate(Long uid, Long productId, Long recordId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void validPurchaseAmount(Long uid, FinancialProduct product, BigDecimal amount) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Long> holdProductIds(Long uid) {
        throw new UnsupportedOperationException();
    }
}
