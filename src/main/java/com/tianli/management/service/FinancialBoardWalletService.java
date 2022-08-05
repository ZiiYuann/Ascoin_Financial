package com.tianli.management.service;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.address.AddressService;
import com.tianli.address.mapper.Address;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.query.ServiceAmountQuery;
import com.tianli.charge.service.OrderService;
import com.tianli.common.RedisLockConstants;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.common.lock.RedisLock;
import com.tianli.management.converter.ManagementConverter;
import com.tianli.management.entity.FinancialBoardWallet;
import com.tianli.management.mapper.FinancialBoardWalletMapper;
import com.tianli.management.query.FinancialBoardQuery;
import com.tianli.management.vo.FinancialWalletBoardSummaryVO;
import com.tianli.management.vo.FinancialWalletBoardVO;
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
public class FinancialBoardWalletService extends ServiceImpl<FinancialBoardWalletMapper, FinancialBoardWallet> {


    public FinancialBoardWallet getFinancialBoardWallet(LocalDateTime startTime, LocalDateTime entTime , FinancialBoardWallet financialBoardWallet) {
        financialBoardWallet = Optional.ofNullable(financialBoardWallet).orElse(FinancialBoardWallet.getDefault());
        ServiceAmountQuery serviceAmountQuery = new ServiceAmountQuery();
        serviceAmountQuery.setStartTime(startTime);
        serviceAmountQuery.setEndTime(entTime);
        serviceAmountQuery.setChargeType(ChargeType.withdraw);
        BigDecimal rechargeAmount = orderService.amountSumByCompleteTime(ChargeType.recharge, startTime, entTime);
        BigDecimal withdrawAmount = orderService.amountSumByCompleteTime(ChargeType.withdraw, startTime, entTime);

        BigInteger activeWalletCount = addressService.activeCount(startTime, entTime);
        // 暂时只有提币存在手续费
        BigDecimal totalServiceAmount = orderService.serviceAmountSumByCompleteTime(serviceAmountQuery);
        serviceAmountQuery.setCoin(CurrencyCoin.usdt);
        BigDecimal usdtServiceAmount = orderService.serviceAmountSumByCompleteTime(serviceAmountQuery);

        financialBoardWallet.setRechargeAmount(rechargeAmount);
        financialBoardWallet.setWithdrawAmount(withdrawAmount);
        financialBoardWallet.setActiveWalletCount(activeWalletCount);
        financialBoardWallet.setTotalServiceAmount(totalServiceAmount);
        financialBoardWallet.setUsdtServiceAmount(usdtServiceAmount);
        return financialBoardWallet;
    }

    public FinancialWalletBoardSummaryVO walletBoard(FinancialBoardQuery query) {

        LocalDateTime todayBegin = TimeTool.minDay(LocalDateTime.now());
        LocalDateTime todayEnd = todayBegin.plusDays(1);
        // 本日数据
        FinancialBoardWallet financialBoardWalletToday = getFinancialBoardWallet(todayBegin,todayEnd , null);
        financialBoardWalletToday.setCreateTime(todayBegin.toLocalDate());

        var addressQuery =
                new LambdaQueryWrapper<Address>().between(Address::getCreateTime, query.getStartTime(), query.getEndTime());
        var walletBoardQuery =
                new LambdaQueryWrapper<FinancialBoardWallet>().between(FinancialBoardWallet::getCreateTime, query.getStartTime(), query.getEndTime());

        List<Address> addresses = Optional.ofNullable(addressService.list(addressQuery)).orElse(new ArrayList<>());


        var financialWalletBoards =
                Optional.ofNullable(financialWalletBoardMapper.selectList(walletBoardQuery)).orElse(new ArrayList<>())
                        .stream().collect(Collectors.toList());
        financialWalletBoards.add(financialBoardWalletToday);

        BigDecimal rechargeAmount = financialWalletBoards.stream().map(FinancialBoardWallet::getRechargeAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal withdrawAmount = financialWalletBoards.stream().map(FinancialBoardWallet::getWithdrawAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalServiceAmount = financialWalletBoards.stream().map(FinancialBoardWallet::getTotalServiceAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal usdtServiceAmount = financialWalletBoards.stream().map(FinancialBoardWallet::getUsdtServiceAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        long newActiveWalletCount = addresses.size();
        int totalActiveWalletCount = addressService.count();

        int offsetDay = -13;
        //获取13天前零点时间
        //构建13天的数据
        Map<String, FinancialWalletBoardVO> financialWalletBoardVOMap = new LinkedHashMap<>();
        for (int i = offsetDay; i <= 0; i++) {
            DateTime time = DateUtil.offsetDay(new Date(), i);
            String dateTimeStr = DateUtil.format(time, "yyyy-MM-dd");
            financialWalletBoardVOMap.put(dateTimeStr, FinancialWalletBoardVO.getDefault(time.toLocalDateTime().toLocalDate()));
        }

        walletBoardQuery = new LambdaQueryWrapper<FinancialBoardWallet>()
                .between(FinancialBoardWallet::getCreateTime, todayBegin.plusDays(-13), todayBegin);
        List<FinancialBoardWallet> financialBoardWallets13 =
                Optional.ofNullable(financialWalletBoardMapper.selectList(walletBoardQuery)).orElse(new ArrayList<>());
        financialBoardWallets13.add(financialBoardWalletToday);

        financialBoardWallets13.stream().forEach(financialBoardWallet -> {
                    FinancialWalletBoardVO financialWalletBoardVO = managementConverter.toVO(financialBoardWallet);
                    String dateTimeStr = financialWalletBoardVO.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                    financialWalletBoardVOMap.put(dateTimeStr, financialWalletBoardVO);
                });

        return FinancialWalletBoardSummaryVO.builder()
                .rechargeAmount(rechargeAmount)
                .withdrawAmount(withdrawAmount)
                .newActiveWalletCount(BigInteger.valueOf(newActiveWalletCount))
                .totalActiveWalletCount(BigInteger.valueOf(totalActiveWalletCount))
                .totalServiceAmount(totalServiceAmount)
                .usdtServiceAmount(usdtServiceAmount)
                .data(financialWalletBoardVOMap.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList()))
                .build();
    }

    /**
     * 获取当日的数据
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public FinancialBoardWallet getByDate(LocalDate todayBegin) {
        redisLock.waitLock(RedisLockConstants.FINANCIAL_WALLET_BOARD_GET, 1000);

        LambdaQueryWrapper<FinancialBoardWallet> query =
                new LambdaQueryWrapper<FinancialBoardWallet>().eq(FinancialBoardWallet::getCreateTime, todayBegin);

        FinancialBoardWallet financialBoardWallet = financialWalletBoardMapper.selectOne(query);
        try {
            if (Objects.isNull(financialBoardWallet)) {
                redisLock.lock(RedisLockConstants.FINANCIAL_WALLET_BOARD_GET, 5L, TimeUnit.SECONDS);
                FinancialBoardWallet boardWallet = FinancialBoardWallet.getDefault();
                boardWallet.setCreateTime(todayBegin);
                financialWalletBoardMapper.insert(boardWallet);
                return boardWallet;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            redisLock.unlock(RedisLockConstants.FINANCIAL_WALLET_BOARD_GET);
        }
        return financialBoardWallet;
    }

    @Resource
    private AddressService addressService;
    @Resource
    private ManagementConverter managementConverter;
    @Resource
    private FinancialBoardWalletMapper financialWalletBoardMapper;
    @Resource
    private RedisLock redisLock;
    @Resource
    private OrderService orderService;

}
