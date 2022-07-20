package com.tianli.management.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.address.AddressService;
import com.tianli.address.mapper.Address;
import com.tianli.management.converter.ManagementConverter;
import com.tianli.management.entity.FinancialWalletBoard;
import com.tianli.management.mapper.FinancialWalletBoardMapper;
import com.tianli.management.query.FinancialBoardQuery;
import com.tianli.management.vo.FinancialWalletBoardSummaryVO;
import com.tianli.management.vo.FinancialWalletBoardVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-20
 **/
@Service
public class FinancialWalletBoardService extends ServiceImpl<FinancialWalletBoardMapper, FinancialWalletBoard> {

    public FinancialWalletBoardSummaryVO walletBoard(FinancialBoardQuery query) {
        var addressQuery =
                new LambdaQueryWrapper<Address>().between(Address::getCreateTime, query.getStartTime(), query.getEndTime());

        var walletBoardQuery =
                new LambdaQueryWrapper<FinancialWalletBoard>().between(FinancialWalletBoard::getCreateTime, query.getStartTime(), query.getEndTime());

        List<Address> addresses = Optional.ofNullable(addressService.list(addressQuery)).orElse(new ArrayList<>());
        long newActiveWalletCount = addresses.size();
        int totalActiveWalletCount = addressService.count();

        var financialWalletBoardVOs =
                Optional.ofNullable(financialWalletBoardMapper.selectList(walletBoardQuery)).orElse(new ArrayList<>())
                        .stream().map(managementConverter :: toVO).collect(Collectors.toList());

        BigDecimal rechargeAmount = financialWalletBoardVOs.stream().map(FinancialWalletBoardVO::getRechargeAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal withdrawAmount = financialWalletBoardVOs.stream().map(FinancialWalletBoardVO::getWithdrawAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        return FinancialWalletBoardSummaryVO.builder()
                .rechargeAmount(rechargeAmount)
                .withdrawAmount(withdrawAmount)
                .newActiveWalletCount(BigInteger.valueOf(newActiveWalletCount))
                .totalActiveWalletCount(BigInteger.valueOf(totalActiveWalletCount))
                .data(financialWalletBoardVOs)
                .build();
    }

    @Resource
    private AddressService addressService;
    @Resource
    private ManagementConverter managementConverter;
    @Resource
    private FinancialWalletBoardMapper financialWalletBoardMapper;

}
