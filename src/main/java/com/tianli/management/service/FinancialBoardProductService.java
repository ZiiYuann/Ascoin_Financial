package com.tianli.management.service;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.common.RedisLockConstants;
import com.tianli.common.lock.RedisLock;
import com.tianli.management.converter.ManagementConverter;
import com.tianli.management.entity.FinancialBoardProduct;
import com.tianli.management.mapper.FinancialBoardProductMapper;
import com.tianli.management.query.FinancialBoardQuery;
import com.tianli.management.vo.FinancialProductBoardSummaryVO;
import com.tianli.management.vo.FinancialProductBoardVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-20
 **/
@Service
public class FinancialBoardProductService extends ServiceImpl<FinancialBoardProductMapper, FinancialBoardProduct> {


    /**
     * 获取当日的数据
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FinancialBoardProduct getToday(){
        redisLock.waitLock(RedisLockConstants.FINANCIAL_PRODUCT_BOARD_GET,1000);
        LocalDate todayBegin =
                DateUtil.beginOfDay(new Date()).toSqlDate().toLocalDate();

        LambdaQueryWrapper<FinancialBoardProduct> query =
                new LambdaQueryWrapper<FinancialBoardProduct>().eq(FinancialBoardProduct::getCreateTime, todayBegin);

        FinancialBoardProduct financialBoardProduct = financialProductBoardMapper.selectOne(query);
        if(Objects.isNull(financialBoardProduct)){
            redisLock.lock(RedisLockConstants.FINANCIAL_PRODUCT_BOARD_GET,5L, TimeUnit.SECONDS);
            FinancialBoardProduct boardProduct = FinancialBoardProduct.getDefault();
            boardProduct.setCreateTime(todayBegin);
            financialProductBoardMapper.insert(boardProduct);
            redisLock.unlock(RedisLockConstants.FINANCIAL_PRODUCT_BOARD_GET);
            return boardProduct;
        }
        return financialBoardProduct;
    }

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
    @Resource
    private RedisLock redisLock;

}
