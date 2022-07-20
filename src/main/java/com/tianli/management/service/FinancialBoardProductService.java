package com.tianli.management.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.management.converter.ManagementConverter;
import com.tianli.management.entity.FinancialBoardProduct;
import com.tianli.management.mapper.FinancialBoardProductMapper;
import com.tianli.management.query.FinancialBoardQuery;
import com.tianli.management.vo.FinancialProductBoardSummaryVO;
import com.tianli.management.vo.FinancialProductBoardVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-20
 **/
@Service
public class FinancialBoardProductService extends ServiceImpl<FinancialBoardProductMapper, FinancialBoardProduct> {

    public FinancialProductBoardSummaryVO productBoard(FinancialBoardQuery query) {

        var boardQuery = new LambdaQueryWrapper<FinancialBoardProduct>()
                .between(FinancialBoardProduct::getCreateTime, query.getStartTime(), query.getEndTime());

        var financialProductBoards = Optional.ofNullable(financialProductBoardMapper.selectList(boardQuery))
                .orElse(new ArrayList<>())
                .stream().map(managementConverter::toVO).collect(Collectors.toList());

        BigDecimal purchaseAmount = financialProductBoards.stream().map(FinancialProductBoardVO::getPurchaseAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal redeemAmount = financialProductBoards.stream().map(FinancialProductBoardVO::getRedeemAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal settleAmount = financialProductBoards.stream().map(FinancialProductBoardVO::getSettleAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal transferAmount = financialProductBoards.stream().map(FinancialProductBoardVO::getTransferAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigInteger holdUserCount = financialProductBoards.stream().map(FinancialProductBoardVO::getHoldUserCount).reduce(BigInteger.ZERO, BigInteger::add);
        BigDecimal income = financialProductBoards.stream().map(FinancialProductBoardVO::getIncome).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigInteger fixedProductCount = financialProductBoards.stream().map(FinancialProductBoardVO::getFixedProductCount).reduce(BigInteger.ZERO, BigInteger::add);
        BigInteger currentProductCount = financialProductBoards.stream().map(FinancialProductBoardVO::getCurrentProductCount).reduce(BigInteger.ZERO, BigInteger::add);

        return FinancialProductBoardSummaryVO.builder()
                .transferAmount(transferAmount)
                .purchaseAmount(purchaseAmount)
                .redeemAmount(redeemAmount)
                .settleAmount(settleAmount)
                .data(financialProductBoards)
                .holdUserCount(holdUserCount)
                .income(income)
                .fixedProductCount(fixedProductCount)
                .currentProductCount(currentProductCount)
                .build();
    }


    @Resource
    private FinancialBoardProductMapper financialProductBoardMapper;
    @Resource
    private ManagementConverter managementConverter;


}
