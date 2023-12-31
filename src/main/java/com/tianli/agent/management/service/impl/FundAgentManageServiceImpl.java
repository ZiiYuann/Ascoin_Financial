package com.tianli.agent.management.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.agent.management.auth.AgentContent;
import com.tianli.agent.management.query.FundStatisticsQuery;
import com.tianli.agent.management.service.FundAgentManageService;
import com.tianli.agent.management.vo.FundProductStatisticsVO;
import com.tianli.agent.management.vo.HoldDataVO;
import com.tianli.agent.management.vo.TransactionDataVO;
import com.tianli.charge.service.OrderService;
import com.tianli.common.PageQuery;
import com.tianli.currency.service.CurrencyService;
import com.tianli.product.afund.dto.FundIncomeAmountDTO;
import com.tianli.product.afund.dto.FundTransactionAmountDTO;
import com.tianli.product.afund.query.FundIncomeQuery;
import com.tianli.product.afund.query.FundRecordQuery;
import com.tianli.product.afund.query.FundTransactionQuery;
import com.tianli.product.afund.service.IFundIncomeRecordService;
import com.tianli.product.afund.service.IFundRecordService;
import com.tianli.product.afund.service.IFundTransactionRecordService;
import com.tianli.management.dto.AmountDto;
import com.tianli.management.entity.WalletAgentProduct;
import com.tianli.management.service.IWalletAgentProductService;
import lombok.Data;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
public class FundAgentManageServiceImpl implements FundAgentManageService {
    @Resource
    private OrderService orderService;

    @Resource
    private IFundTransactionRecordService fundTransactionRecordService;

    @Resource
    private IFundIncomeRecordService fundIncomeRecordService;

    @Resource
    private IFundRecordService fundRecordService;

    @Resource
    private IWalletAgentProductService walletAgentProductService;

    @Resource
    private CurrencyService currencyService;

    @Override
    public TransactionDataVO transactionData(FundStatisticsQuery query) {
        query.calTime();
        Long agentId = AgentContent.getAgentId();

        FundTransactionQuery transactionQuery = FundTransactionQuery
                .builder()
                .startTime(query.getStartTime())
                .endTime(query.getEndTime())
                .agentId(agentId).build();
        List<FundTransactionAmountDTO> fundTransactionAmountDTO = fundTransactionRecordService.getFundTransactionAmountDTO(transactionQuery);
        List<AmountDto> purchaseAmount = fundTransactionAmountDTO.stream().map(amountDTO ->
                new AmountDto(amountDTO.getPurchaseAmount(), amountDTO.getCoin())).collect(Collectors.toList());
        List<AmountDto> redemptionAmount = fundTransactionAmountDTO.stream().map(amountDTO ->
                new AmountDto(amountDTO.getRedemptionAmount(), amountDTO.getCoin())).collect(Collectors.toList());
        FundIncomeQuery incomeQuery = FundIncomeQuery.builder()
                .agentId(agentId)
                .startTime(query.getStartTime())
                .endTime(query.getEndTime())
                .build();
        List<FundIncomeAmountDTO> fundIncomeAmountDTOS = fundIncomeRecordService.getAmount(incomeQuery);
        List<AmountDto> interestAmount = fundIncomeAmountDTOS.stream()
                .filter(index -> Objects.nonNull(index.getCoin()))
                .map(fundIncomeAmountDTO ->
                        new AmountDto(fundIncomeAmountDTO.getTotalAmount(), fundIncomeAmountDTO.getCoin())).collect(Collectors.toList());
        return TransactionDataVO.builder()
                .purchaseAmount(currencyService.calDollarAmount(purchaseAmount))
                .redemptionAmount(currencyService.calDollarAmount(redemptionAmount))
                .interestAmount(currencyService.calDollarAmount(interestAmount))
                .build();
    }

    @Override
    public HoldDataVO holdData(FundStatisticsQuery query) {
        Long agentId = AgentContent.getAgentId();
        FundIncomeQuery incomeQuery = FundIncomeQuery.builder()
                .agentId(agentId)
                .build();
        FundAmount fundAmount = getFundAmount(incomeQuery);
        FundRecordQuery fundRecordQuery = FundRecordQuery.builder().agentId(agentId).build();
        Integer holdUserCount = fundRecordService.getHoldUserCount(fundRecordQuery);
        BigDecimal holdAmount = fundRecordService.dollarHold(fundRecordQuery);
        return HoldDataVO.builder()
                .payInterestAmount(fundAmount.getPayInterestAmount())
                .waitPayInterestAmount(fundAmount.getWaitPayInterestAmount())
                .holdAmount(holdAmount)
                .holdCount(holdUserCount)
                .build();
    }

    @Override
    public IPage<FundProductStatisticsVO> productStatistics(PageQuery<WalletAgentProduct> pageQuery, FundStatisticsQuery query) {
        IPage<FundProductStatisticsVO> page = walletAgentProductService.getPage(pageQuery, query);
        return page.convert(fundProductStatisticsVO -> {
            Long productId = fundProductStatisticsVO.getProductId();
            FundRecordQuery fundRecordQuery = FundRecordQuery.builder().productId(productId).build();
            BigDecimal holdAmount = fundRecordService.dollarHold(fundRecordQuery);
            Integer holdUserCount = fundRecordService.getHoldUserCount(fundRecordQuery);
            FundIncomeQuery incomeQuery = FundIncomeQuery.builder()
                    .productId(productId)
                    .build();
            FundAmount fundAmount = getFundAmount(incomeQuery);
            fundProductStatisticsVO.setHoldAmount(holdAmount);
            fundProductStatisticsVO.setHoldCount(holdUserCount);
            fundProductStatisticsVO.setPayInterestAmount(fundAmount.getPayInterestAmount());
            fundProductStatisticsVO.setWaitPayInterestAmount(fundAmount.getWaitPayInterestAmount());
            return fundProductStatisticsVO;
        });
    }

    /**
     * 获取基金相关的计算金额 应付金额 和 待记息金额
     */
    private FundAmount getFundAmount(FundIncomeQuery fundIncomeQuery) {
        List<FundIncomeAmountDTO> fundIncomeAmountDTOS = fundIncomeRecordService.getAmount(fundIncomeQuery);
        List<AmountDto> payInterestAmount = fundIncomeAmountDTOS.stream().map(fundIncomeAmountDTO ->
                new AmountDto(fundIncomeAmountDTO.getPayInterestAmount(), fundIncomeAmountDTO.getCoin())).collect(Collectors.toList());
        List<AmountDto> waitPayInterestAmount = fundIncomeAmountDTOS.stream().map(fundIncomeAmountDTO ->
                new AmountDto(fundIncomeAmountDTO.getWaitInterestAmount(), fundIncomeAmountDTO.getCoin())).collect(Collectors.toList());

        FundAmount fundAmount = new FundAmount();
        fundAmount.setPayInterestAmount(currencyService.calDollarAmount(payInterestAmount));
        fundAmount.setWaitPayInterestAmount(currencyService.calDollarAmount(waitPayInterestAmount));
        return fundAmount;
    }

    /**
     * 内部类，临时存放数据
     */
    @Data
    private class FundAmount {

        private BigDecimal payInterestAmount;

        private BigDecimal waitPayInterestAmount;
    }
}
