package com.tianli.management.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.address.service.AddressService;
import com.tianli.chain.entity.Coin;
import com.tianli.chain.service.CoinService;
import com.tianli.chain.service.contract.ContractAdapter;
import com.tianli.chain.service.contract.ContractOperation;
import com.tianli.common.CommonFunction;
import com.tianli.currency.enums.TokenAdapter;
import com.tianli.currency.service.CurrencyService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.management.converter.ManagementConverter;
import com.tianli.management.entity.HotWalletDetailed;
import com.tianli.management.enums.HotWalletOperationType;
import com.tianli.management.mapper.HotWalletDetailedMapper;
import com.tianli.management.query.HotWalletDetailedIoUQuery;
import com.tianli.management.query.HotWalletDetailedPQuery;
import com.tianli.management.service.HotWalletDetailedService;
import com.tianli.management.vo.HotWalletBalanceVO;
import com.tianli.management.vo.HotWalletDetailedSummaryDataVO;
import com.tianli.management.vo.HotWalletDetailedVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author chenb
 * @apiNote
 * @since 2022-08-17
 **/
@Service
public class HotWalletDetailedServiceImpl extends ServiceImpl<HotWalletDetailedMapper, HotWalletDetailed>
        implements HotWalletDetailedService {

    @Resource
    private ManagementConverter managementConverter;
    @Resource
    private AddressService addressService;
    @Resource
    private ContractAdapter contractAdapter;
    @Resource
    private CurrencyService currencyService;
    @Resource
    private CoinService coinService;

    @Transactional
    @Override
    public void insertOrUpdate(HotWalletDetailedIoUQuery ioUQuery) {
        if (!HotWalletOperationType.recharge.equals(ioUQuery.getType()) && !HotWalletOperationType.withdraw.equals(ioUQuery.getType())) {
            ErrorCodeEnum.throwException("类型传值错误");
        }

        HotWalletDetailed hotWalletDetailed = managementConverter.toDO(ioUQuery);
        if (Objects.isNull(hotWalletDetailed.getId())) {
            hotWalletDetailed.setId(CommonFunction.generalId());
            hotWalletDetailed.setCreateTime(LocalDateTime.now());
            baseMapper.insert(hotWalletDetailed);
        }

        if (Objects.nonNull(hotWalletDetailed.getId())) {
            baseMapper.updateById(hotWalletDetailed);
        }
    }

    @Transactional
    @Override
    public void insert(HotWalletDetailed hotWalletDetailed) {
        baseMapper.insert(hotWalletDetailed);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        baseMapper.deleteById(id);
    }

    @Override
    public List<HotWalletBalanceVO> mainBalance() {
        List<Coin> coins = coinService.pushCoinsWithCache();
        var coinMap = coins.stream().collect(Collectors.groupingBy(Coin::getChain));
        List<HotWalletBalanceVO> hotWalletBalanceVOS = new ArrayList<>();
        for (var entry : coinMap.entrySet()) {
            var coinList = entry.getValue();
            coinList.sort(Comparator.comparing(Coin::getName));
            var chainType = entry.getKey();
            // 设置主币信息
            Coin mainCoin = coinService.mainToken(chainType, chainType.getMainToken());
            ContractOperation contract = contractAdapter.getOne(mainCoin.getNetwork());
            String address = addressService.getAddress(chainType);
            BigDecimal amount = TokenAdapter.alignment(mainCoin, contract.mainBalance(address));

            HotWalletBalanceVO hotWalletBalanceVO = new HotWalletBalanceVO();
            hotWalletBalanceVO.setAmount(amount);
            hotWalletBalanceVO.setCoinName(mainCoin.getName());
            hotWalletBalanceVO.setChain(chainType);
            hotWalletBalanceVOS.add(hotWalletBalanceVO);
        }
        return hotWalletBalanceVOS;
    }

    @Override
    public IPage<HotWalletDetailedVO> pageByQuery(Page<HotWalletDetailed> page, HotWalletDetailedPQuery query) {
        return baseMapper.pageByQuery(page, query).convert(managementConverter::toHotWalletDetailedVO);
    }

    @Override
    public HotWalletDetailedSummaryDataVO summaryData(HotWalletDetailedPQuery query) {

        query.setType(HotWalletOperationType.recharge);
        BigDecimal rechargeAmountDollar = currencyService.calDollarAmount(baseMapper.summaryDataByQuery(query));

        query.setType(HotWalletOperationType.withdraw);
        BigDecimal withdrawAmountDollar = currencyService.calDollarAmount(baseMapper.summaryDataByQuery(query));

        query.setType(HotWalletOperationType.user_withdraw);
        BigDecimal userWithdrawAmountDollar = currencyService.calDollarAmount(baseMapper.summaryDataByQuery(query));

        query.setType(HotWalletOperationType.imputation);
        BigDecimal imputationAmountDollar = currencyService.calDollarAmount(baseMapper.summaryDataByQuery(query));

        return HotWalletDetailedSummaryDataVO.builder()
                .rechargeAmountDollar(rechargeAmountDollar)
                .withdrawAmountDollar(withdrawAmountDollar)
                .userWithdrawAmountDollar(userWithdrawAmountDollar)
                .imputationAmountDollar(imputationAmountDollar).build();
    }

    @Override
    public List<HotWalletBalanceVO> balance() {
        List<Coin> coins = coinService.pushCoinsWithCache();

        var coinMap = coins.stream().collect(Collectors.groupingBy(Coin::getChain));

        List<HotWalletBalanceVO> hotWalletBalanceVOS = new ArrayList<>();
        for (var entry : coinMap.entrySet()) {
            var coinList = entry.getValue();
            coinList.sort(Comparator.comparing(Coin::getName));
            var chainType = entry.getKey();
            // 设置主币信息
            Coin mainCoin = coinService.mainToken(chainType, chainType.getMainToken());
            ContractOperation contract = contractAdapter.getOne(mainCoin.getNetwork());
            String address = addressService.getAddress(chainType);
            BigDecimal amount = TokenAdapter.alignment(mainCoin, contract.mainBalance(address));

            HotWalletBalanceVO hotWalletBalanceVO = new HotWalletBalanceVO();
            hotWalletBalanceVO.setAmount(amount);
            hotWalletBalanceVO.setCoinName(mainCoin.getName());
            hotWalletBalanceVO.setChain(chainType);

            List<HotWalletBalanceVO> tokensBalances = new ArrayList<>();
            for (Coin token : coinList) {
                if (token.isMainToken()) {
                    continue;
                }

                HotWalletBalanceVO tokenVO = new HotWalletBalanceVO();
                tokenVO.setAmount(TokenAdapter.alignment(token, contract.tokenBalance(address, token)));
                tokenVO.setCoinName(token.getName());
                tokenVO.setChain(chainType);
                tokenVO.setNetwork(token.getNetwork());
                tokensBalances.add(tokenVO);
            }
            tokensBalances.sort(Comparator.comparing(HotWalletBalanceVO::getChain));
            hotWalletBalanceVO.setTokens(tokensBalances);
            hotWalletBalanceVOS.add(hotWalletBalanceVO);
        }
        return hotWalletBalanceVOS;
    }

    @Override
    public BigDecimal balanceFee() {
        List<BigDecimal> balanceFees = new ArrayList<>();
        List<HotWalletBalanceVO> balance = this.balance();

        balance.forEach(b -> {

            String mainCoin = b.getCoinName();
            BigDecimal mainCoinRate = currencyService.getDollarRate(mainCoin);
            balanceFees.add(mainCoinRate.multiply(b.getAmount()));

            b.getTokens().forEach(token -> {
                String tokenCoin = token.getCoinName();
                BigDecimal tokenCoinRate = currencyService.getDollarRate(tokenCoin);
                balanceFees.add(tokenCoinRate.multiply(token.getAmount()));
            });
        });

        return balanceFees.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
