package com.tianli.agent.management.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.agent.management.auth.AgentContent;
import com.tianli.agent.management.enums.TimeQueryEnum;
import com.tianli.agent.management.query.FundStatisticsQuery;
import com.tianli.agent.management.service.FundAgentManageService;
import com.tianli.agent.management.vo.FundProductStatisticsVO;
import com.tianli.agent.management.vo.HoldDataVO;
import com.tianli.agent.management.vo.TransactionDataVO;
import com.tianli.charge.service.OrderService;
import com.tianli.common.PageQuery;
import com.tianli.fund.dto.FundIncomeAmountDTO;
import com.tianli.fund.dto.FundTransactionAmountDTO;
import com.tianli.fund.query.FundIncomeQuery;
import com.tianli.fund.query.FundRecordQuery;
import com.tianli.fund.query.FundTransactionQuery;
import com.tianli.fund.service.IFundIncomeRecordService;
import com.tianli.fund.service.IFundRecordService;
import com.tianli.fund.service.IFundTransactionRecordService;
import com.tianli.management.dto.AmountDto;
import com.tianli.management.entity.WalletAgentProduct;
import com.tianli.management.service.IWalletAgentProductService;
import com.tianli.tool.time.TimeTool;
import lombok.Data;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
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
    IWalletAgentProductService walletAgentProductService;

    @Override
    public TransactionDataVO transactionData(FundStatisticsQuery query) {
        Long agentUId = AgentContent.getAgentUId();
        LocalDateTime startTime = null;
        LocalDateTime endTime = null;
        if (Objects.nonNull(query.getTimeRange())) {
            if (query.getTimeRange() == TimeQueryEnum.day) {
                startTime = TimeTool.toLocalDateTime(DateUtil.beginOfDay(new Date()));
            } else if (query.getTimeRange() == TimeQueryEnum.week) {
                startTime = TimeTool.toLocalDateTime(DateUtil.beginOfWeek(new Date()));
            } else if (query.getTimeRange() == TimeQueryEnum.mouth) {
                startTime = TimeTool.toLocalDateTime(DateUtil.beginOfMonth(new Date()));
            }
        } else {
            startTime = query.getStartTime();
            endTime = query.getEndTime();
        }
        FundTransactionQuery transactionQuery = FundTransactionQuery
                .builder()
                .startTime(startTime)
                .endTime(endTime)
                .agentUId(agentUId).build();
        List<FundTransactionAmountDTO> fundTransactionAmountDTO = fundTransactionRecordService.getFundTransactionAmountDTO(transactionQuery);
        List<AmountDto> purchaseAmount = fundTransactionAmountDTO.stream().map(amountDTO ->
                new AmountDto(amountDTO.getPurchaseAmount(), amountDTO.getCoin())).collect(Collectors.toList());
        List<AmountDto> redemptionAmount = fundTransactionAmountDTO.stream().map(amountDTO ->
                new AmountDto(amountDTO.getRedemptionAmount(), amountDTO.getCoin())).collect(Collectors.toList());
        FundIncomeQuery incomeQuery = FundIncomeQuery.builder()
                .agentUId(agentUId)
                .build();
        List<FundIncomeAmountDTO> fundIncomeAmountDTOS = fundIncomeRecordService.getAmount(incomeQuery);
        List<AmountDto> interestAmount = fundIncomeAmountDTOS.stream().map(fundIncomeAmountDTO ->
                new AmountDto(fundIncomeAmountDTO.getTotalAmount(), fundIncomeAmountDTO.getCoin())).collect(Collectors.toList());
        return TransactionDataVO.builder()
                .purchaseAmount(orderService.calDollarAmount(purchaseAmount))
                .redemptionAmount(orderService.calDollarAmount(redemptionAmount))
                .interestAmount(orderService.calDollarAmount(interestAmount))
                .build();
    }

    @Override
    public HoldDataVO holdData() {
        Long agentUId = AgentContent.getAgentUId();
        FundIncomeQuery incomeQuery = FundIncomeQuery.builder()
                .agentUId(agentUId)
                .build();
        FundAmount fundAmount = getFundAmount(incomeQuery);
        FundRecordQuery fundRecordQuery = FundRecordQuery.builder().agentUId(agentUId).build();
        Integer holdUserCount = fundRecordService.getHoldUserCount(fundRecordQuery);
        BigDecimal holdAmount = fundRecordService.getHoldAmount(fundRecordQuery);
        return HoldDataVO.builder()
                .payInterestAmount(fundAmount.getPayInterestAmount())
                .waitPayInterestAmount(fundAmount.getWaitPayInterestAmount())
                .holdAmount(holdAmount)
                .holdCount(holdUserCount)
                .build();
    }

    @Override
    public IPage<FundProductStatisticsVO> productStatistics(PageQuery<WalletAgentProduct> pageQuery) {
        IPage<FundProductStatisticsVO> page = walletAgentProductService.getPage(pageQuery);
        return page.convert(fundProductStatisticsVO -> {
            Long productId = fundProductStatisticsVO.getProductId();
            FundRecordQuery fundRecordQuery = FundRecordQuery.builder().productId(productId).build();
            BigDecimal holdAmount = fundRecordService.getHoldAmount(fundRecordQuery);
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
     *  获取基金相关的计算金额 应付金额 和 待记息金额
     */
    private FundAmount getFundAmount(FundIncomeQuery fundIncomeQuery){
        List<FundIncomeAmountDTO> fundIncomeAmountDTOS = fundIncomeRecordService.getAmount(fundIncomeQuery);
        List<AmountDto> payInterestAmount = fundIncomeAmountDTOS.stream().map(fundIncomeAmountDTO ->
                new AmountDto(fundIncomeAmountDTO.getPayInterestAmount(), fundIncomeAmountDTO.getCoin())).collect(Collectors.toList());
        List<AmountDto> waitPayInterestAmount = fundIncomeAmountDTOS.stream().map(fundIncomeAmountDTO ->
                new AmountDto(fundIncomeAmountDTO.getWaitInterestAmount(), fundIncomeAmountDTO.getCoin())).collect(Collectors.toList());

        FundAmount fundAmount = new FundAmount();
        fundAmount.setPayInterestAmount(orderService.calDollarAmount(payInterestAmount));
        fundAmount.setWaitPayInterestAmount(orderService.calDollarAmount(waitPayInterestAmount));
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
