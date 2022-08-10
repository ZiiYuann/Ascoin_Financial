package com.tianli.management.service;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.service.OrderService;
import com.tianli.common.RedisLockConstants;
import com.tianli.common.lock.RedisLock;
import com.tianli.financial.enums.ProductType;
import com.tianli.financial.service.FinancialIncomeAccrueService;
import com.tianli.financial.service.FinancialRecordService;
import com.tianli.management.converter.ManagementConverter;
import com.tianli.management.entity.FinancialBoardProduct;
import com.tianli.management.mapper.FinancialBoardProductMapper;
import com.tianli.management.query.FinancialBoardQuery;
import com.tianli.management.vo.FinancialProductBoardSummaryVO;
import com.tianli.management.vo.FinancialProductBoardVO;
import com.tianli.tool.time.TimeTool;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
    public FinancialBoardProduct getByDate(LocalDate todayBegin) {
        redisLock.waitLock(RedisLockConstants.FINANCIAL_PRODUCT_BOARD_GET, 1000);

        LambdaQueryWrapper<FinancialBoardProduct> query =
                new LambdaQueryWrapper<FinancialBoardProduct>().eq(FinancialBoardProduct::getCreateTime, todayBegin);

        FinancialBoardProduct financialBoardProduct = financialProductBoardMapper.selectOne(query);

        try {
            if (Objects.isNull(financialBoardProduct)) {
                redisLock.lock(RedisLockConstants.FINANCIAL_PRODUCT_BOARD_GET, 5L, TimeUnit.SECONDS);
                FinancialBoardProduct boardProduct = FinancialBoardProduct.getDefault();
                boardProduct.setCreateTime(todayBegin);
                financialProductBoardMapper.insert(boardProduct);
                redisLock.unlock(RedisLockConstants.FINANCIAL_PRODUCT_BOARD_GET);
                return boardProduct;
            }
            return financialBoardProduct;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            redisLock.unlock(RedisLockConstants.FINANCIAL_PRODUCT_BOARD_GET);
        }
    }

    public FinancialBoardProduct getFinancialBoardProduct(LocalDateTime endTime, LocalDateTime startTime, FinancialBoardProduct today) {
        today = Optional.ofNullable(today).orElse(FinancialBoardProduct.getDefault());
        BigDecimal purchaseAmount = orderService.amountDollarSumByCompleteTime(ChargeType.purchase, startTime, endTime);
        BigDecimal redeemAmount = orderService.amountDollarSumByCompleteTime(ChargeType.redeem, startTime, endTime);
        BigDecimal settleAmount = orderService.amountDollarSumByCompleteTime(ChargeType.settle, startTime, endTime);
        BigDecimal transferAmount = orderService.amountDollarSumByCompleteTime(ChargeType.transfer, startTime, endTime);
        BigDecimal income = Optional.ofNullable(financialIncomeAccrueService.getAmountSum(endTime)).orElse(BigDecimal.ZERO);
        BigInteger fixedProductCount = financialRecordService.countProcess(ProductType.fixed);
        BigInteger currentProductCount = financialRecordService.countProcess(ProductType.current);
        BigInteger totalProductCount = currentProductCount.add(fixedProductCount);
        BigInteger holdUserCount = financialRecordService.countUid();

        today.setPurchaseAmount(purchaseAmount);
        today.setRedeemAmount(redeemAmount);
        today.setSettleAmount(settleAmount);
        today.setTransferAmount(transferAmount);
        today.setIncome(income);
        today.setCurrentProductCount(currentProductCount);
        today.setFixedProductCount(fixedProductCount);
        today.setTotalProductCount(totalProductCount);
        today.setHoldUserCount(holdUserCount);
        return today;
    }

    public FinancialProductBoardSummaryVO productBoard(FinancialBoardQuery query) {

        LocalDateTime todayBegin = TimeTool.minDay(LocalDateTime.now());
        LocalDateTime todayEnd = todayBegin.plusDays(1);

        // 本日数据 实时查询
        FinancialBoardProduct financialBoardProductToday = getFinancialBoardProduct(todayEnd, todayBegin, null);
        financialBoardProductToday.setCreateTime(todayBegin.toLocalDate());

        // 根据日期动态查询出已经持久化的数据
        var boardQuery = new LambdaQueryWrapper<FinancialBoardProduct>()
                .between(FinancialBoardProduct::getCreateTime, query.getStartTime(), query.getEndTime());
        var financialProductBoards = Optional.ofNullable(financialProductBoardMapper.selectList(boardQuery))
                .orElse(new ArrayList<>());
        // 额外加入当日数据
        financialProductBoards.add(financialBoardProductToday);

        BigDecimal purchaseAmount = financialProductBoards.stream().map(FinancialBoardProduct::getPurchaseAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal redeemAmount = financialProductBoards.stream().map(FinancialBoardProduct::getRedeemAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal settleAmount = financialProductBoards.stream().map(FinancialBoardProduct::getSettleAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal transferAmount = financialProductBoards.stream().map(FinancialBoardProduct::getTransferAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigInteger holdUserCount = financialProductBoards.stream().map(FinancialBoardProduct::getHoldUserCount).reduce(BigInteger.ZERO, BigInteger::add);
        BigDecimal income = financialProductBoards.stream().map(FinancialBoardProduct::getIncome).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigInteger fixedProductCount = financialProductBoards.stream().map(FinancialBoardProduct::getFixedProductCount).reduce(BigInteger.ZERO, BigInteger::add);
        BigInteger currentProductCount = financialProductBoards.stream().map(FinancialBoardProduct::getCurrentProductCount).reduce(BigInteger.ZERO, BigInteger::add);

        int offsetDay = -13;
        //获取14天前零点时间
        //构建十四天的数据
        Map<String, FinancialProductBoardVO> financialProductBoardVOMap = new LinkedHashMap<>();
        for (int i = offsetDay; i <= 0; i++) {
            DateTime time = DateUtil.offsetDay(new Date(), i);
            String dateTimeStr = DateUtil.format(time, "yyyy-MM-dd");
            financialProductBoardVOMap.put(dateTimeStr, FinancialProductBoardVO.getDefault(time.toLocalDateTime().toLocalDate()));
        }
        // 固定查询前13日的数据
        boardQuery = new LambdaQueryWrapper<FinancialBoardProduct>()
                .between(FinancialBoardProduct::getCreateTime, todayBegin.plusDays(-13), todayBegin);
        List<FinancialBoardProduct> financialProductBoards13 = Optional.ofNullable(financialProductBoardMapper.selectList(boardQuery))
                .orElse(new ArrayList<>());
        // 添加当日数据
        financialProductBoards13.add(financialBoardProductToday);
        financialProductBoards13.stream().forEach(financialBoardProduct -> {
                    FinancialProductBoardVO financialProductBoardVO = managementConverter.toVO(financialBoardProduct);
                    String dateTimeStr = financialProductBoardVO.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    financialProductBoardVOMap.put(dateTimeStr, financialProductBoardVO);
                });

        return FinancialProductBoardSummaryVO.builder()
                .transferAmount(transferAmount)
                .purchaseAmount(purchaseAmount)
                .redeemAmount(redeemAmount)
                .settleAmount(settleAmount)
                .data(financialProductBoardVOMap.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList()))
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
    @Resource
    private OrderService orderService;
    @Resource
    private FinancialIncomeAccrueService financialIncomeAccrueService;
    @Resource
    private FinancialRecordService financialRecordService;

}
