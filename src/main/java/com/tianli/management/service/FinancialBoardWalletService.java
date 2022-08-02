package com.tianli.management.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.address.AddressService;
import com.tianli.address.mapper.Address;
import com.tianli.common.RedisLockConstants;
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

    public FinancialWalletBoardSummaryVO walletBoard(FinancialBoardQuery query) {
        var addressQuery =
                new LambdaQueryWrapper<Address>().between(Address::getCreateTime, query.getStartTime(), query.getEndTime());

        var walletBoardQuery =
                new LambdaQueryWrapper<FinancialBoardWallet>().between(FinancialBoardWallet::getCreateTime, query.getStartTime(), query.getEndTime());

        List<Address> addresses = Optional.ofNullable(addressService.list(addressQuery)).orElse(new ArrayList<>());
        long newActiveWalletCount = addresses.size();
        int totalActiveWalletCount = addressService.count();

        var financialWalletBoardVOs =
                Optional.ofNullable(financialWalletBoardMapper.selectList(walletBoardQuery)).orElse(new ArrayList<>())
                        .stream().map(managementConverter::toVO).collect(Collectors.toList());

        BigDecimal rechargeAmount = financialWalletBoardVOs.stream().map(FinancialWalletBoardVO::getRechargeAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal withdrawAmount = financialWalletBoardVOs.stream().map(FinancialWalletBoardVO::getWithdrawAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalServiceAmount = financialWalletBoardVOs.stream().map(FinancialWalletBoardVO::getTotalServiceAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal usdtServiceAmount = financialWalletBoardVOs.stream().map(FinancialWalletBoardVO::getUsdtServiceAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        LocalDateTime dateTime = TimeTool.minDay(LocalDateTime.now());
        walletBoardQuery = new LambdaQueryWrapper<FinancialBoardWallet>()
                .between(FinancialBoardWallet::getCreateTime, dateTime.plusDays(-14), dateTime);

        var financialWalletBoardVOs14 =
                Optional.ofNullable(financialWalletBoardMapper.selectList(walletBoardQuery)).orElse(new ArrayList<>())
                        .stream().map(managementConverter::toVO).collect(Collectors.toList());


        return FinancialWalletBoardSummaryVO.builder()
                .rechargeAmount(rechargeAmount)
                .withdrawAmount(withdrawAmount)
                .newActiveWalletCount(BigInteger.valueOf(newActiveWalletCount))
                .totalActiveWalletCount(BigInteger.valueOf(totalActiveWalletCount))
                .totalServiceAmount(totalServiceAmount)
                .usdtServiceAmount(usdtServiceAmount)
                .data(financialWalletBoardVOs14)
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

}
