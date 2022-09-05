package com.tianli.fund.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.account.enums.AccountChangeType;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.agent.management.auth.AgentContent;
import com.tianli.agent.management.bo.FundAuditBO;
import com.tianli.agent.management.vo.FundAuditRecordVO;
import com.tianli.charge.entity.Order;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.service.OrderService;
import com.tianli.common.CommonFunction;
import com.tianli.common.PageQuery;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.fund.contant.FundIncomeStatus;
import com.tianli.fund.contant.FundTransactionStatus;
import com.tianli.fund.dao.FundTransactionRecordMapper;
import com.tianli.fund.dto.FundTransactionAmountDTO;
import com.tianli.fund.entity.FundIncomeRecord;
import com.tianli.fund.entity.FundTransactionRecord;
import com.tianli.fund.query.FundTransactionQuery;
import com.tianli.fund.service.IFundIncomeRecordService;
import com.tianli.fund.service.IFundRecordService;
import com.tianli.fund.service.IFundTransactionRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.fund.vo.FundTransactionRecordVO;
import com.tianli.management.dto.AmountDto;
import com.tianli.management.vo.FundTransactionAmountVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 基金交易记录 服务实现类
 * </p>
 *
 * @author xianeng
 * @since 2022-08-30
 */
@Service
@Transactional
public class FundTransactionRecordServiceImpl extends ServiceImpl<FundTransactionRecordMapper, FundTransactionRecord> implements IFundTransactionRecordService {

    @Autowired
    private FundTransactionRecordMapper fundTransactionRecordMapper;

    @Autowired
    private OrderService orderService;

    @Autowired
    private AccountBalanceService accountBalanceService;

    @Autowired
    private IFundRecordService fundRecordService;

    @Autowired
    private IFundIncomeRecordService fundIncomeRecordService;

    @Override
    public IPage<FundTransactionRecordVO> getTransactionPage(PageQuery<FundTransactionRecord> page, FundTransactionQuery query) {
        return fundTransactionRecordMapper.selectTransactionPage(page.page(), query);
    }

    @Override
    public List<FundTransactionAmountDTO> getFundTransactionAmountDTO(FundTransactionQuery query) {
        return fundTransactionRecordMapper.selectTransactionAmount(query);
    }

    @Override
    public FundTransactionAmountVO getTransactionAmount(FundTransactionQuery query) {
        List<FundTransactionAmountDTO> fundTransactionAmountDTOS = getFundTransactionAmountDTO(query);
        List<AmountDto> purchaseAmount = fundTransactionAmountDTOS.stream().map(amountDTO ->
                new AmountDto(amountDTO.getPurchaseAmount(), amountDTO.getCoin())).collect(Collectors.toList());
        List<AmountDto> redemptionAmount = fundTransactionAmountDTOS.stream().map(amountDTO ->
                new AmountDto(amountDTO.getRedemptionAmount(), amountDTO.getCoin())).collect(Collectors.toList());
        return FundTransactionAmountVO.builder()
                .purchaseAmount(orderService.calDollarAmount(purchaseAmount))
                .redemptionAmount(orderService.calDollarAmount(redemptionAmount))
                .build();
    }

    @Override
    public void redemptionAudit(FundAuditBO bo) {
        Long agentId = AgentContent.getAgentUId();
        Boolean auditResult = bo.getAuditResult();
        List<Long> ids = bo.getIds();
        ids.forEach(id ->{
            FundTransactionRecord fundTransactionRecord = fundTransactionRecordMapper.selectById(id);
            if(Objects.isNull(fundTransactionRecord) ||
                    !fundTransactionRecord.getStatus().equals(FundTransactionStatus.wait_audit)) ErrorCodeEnum.TRANSACTION_NOT_EXIST.throwException();
            Long uid = fundTransactionRecord.getUid();
            if(auditResult) {
                //生成一笔订单
                Order agentOrder = Order.builder()
                        .uid(agentId)
                        .coin(fundTransactionRecord.getCoin())
                        .relatedId(fundTransactionRecord.getId())
                        .orderNo(AccountChangeType.agent_fund_redeem.getPrefix() + CommonFunction.generalSn(CommonFunction.generalId()))
                        .amount(fundTransactionRecord.getTransactionAmount())
                        .type(ChargeType.agent_fund_redeem)
                        .status(ChargeStatus.chain_success)
                        .createTime(LocalDateTime.now())
                        .completeTime(LocalDateTime.now())
                        .build();
                orderService.save(agentOrder);
                // 减少余额
                accountBalanceService.decrease(agentId, ChargeType.agent_fund_redeem, fundTransactionRecord.getCoin(), fundTransactionRecord.getTransactionAmount(), agentOrder.getOrderNo(), CurrencyLogDes.代理基金赎回.name());

                //生成一笔订单
                Order order = Order.builder()
                        .uid(uid)
                        .coin(fundTransactionRecord.getCoin())
                        .relatedId(fundTransactionRecord.getId())
                        .orderNo(AccountChangeType.fund_redeem.getPrefix() + CommonFunction.generalSn(CommonFunction.generalId()))
                        .amount(fundTransactionRecord.getTransactionAmount())
                        .type(ChargeType.fund_redeem)
                        .status(ChargeStatus.chain_success)
                        .createTime(LocalDateTime.now())
                        .completeTime(LocalDateTime.now())
                        .build();
                orderService.save(order);
                // 增加余额
                accountBalanceService.increase(uid, ChargeType.fund_redeem, fundTransactionRecord.getCoin(), fundTransactionRecord.getTransactionAmount(), order.getOrderNo(), CurrencyLogDes.代理基金赎回.name());
                fundTransactionRecord.setAuditResult(true);
                fundTransactionRecord.setAuditTime(LocalDateTime.now());
                fundTransactionRecord.setStatus(FundTransactionStatus.success);
                fundTransactionRecord.setAuditRemark(bo.getAuditRemark());
                fundTransactionRecordMapper.updateById(fundTransactionRecord);
            }else {
                fundTransactionRecord.setAuditResult(false);
                fundTransactionRecord.setStatus(FundTransactionStatus.fail);
                fundTransactionRecord.setAuditTime(LocalDateTime.now());
                fundTransactionRecord.setAuditRemark(bo.getAuditRemark());
                fundTransactionRecordMapper.updateById(fundTransactionRecord);
                fundRecordService.increaseAmount(fundTransactionRecord.getFundId(),fundTransactionRecord.getTransactionAmount());
            }
        });

    }

    @Override
    public FundAuditRecordVO getRedemptionAuditRecord(Long id) {
        FundTransactionRecord fundTransactionRecord = fundTransactionRecordMapper.selectById(id);
        if(Objects.isNull(fundTransactionRecord)) ErrorCodeEnum.TRANSACTION_NOT_EXIST.throwException();

        return FundAuditRecordVO.builder()
                .auditResult(fundTransactionRecord.getAuditResult())
                .auditTime(fundTransactionRecord.getAuditTime())
                .auditRemark(fundTransactionRecord.getAuditRemark())
                .build();
    }

    @Override
    public boolean existWaitRedemption(Long agentUid) {
        Integer count = fundTransactionRecordMapper.selectWaitRedemptionCount(agentUid);
        return count>0;
    }

    @Override
    public void incomeAudit(FundAuditBO bo) {
        List<Long> ids = bo.getIds();
        Long agentId = AgentContent.getAgentUId();
        ids.forEach(id->{

            FundIncomeRecord fundIncomeRecord = fundIncomeRecordService.getById(id);
            if(Objects.isNull(fundIncomeRecord) ||
                    !fundIncomeRecord.getStatus().equals(FundIncomeStatus.wait_audit) ||
                    !fundIncomeRecord.getStatus().equals(FundIncomeStatus.audit_failure))ErrorCodeEnum.INCOME_NOT_EXIST.throwException();
            Long uid = fundIncomeRecord.getUid();

            Order agentOrder = Order.builder()
                    .uid(agentId)
                    .coin(fundIncomeRecord.getCoin())
                    .relatedId(fundIncomeRecord.getId())
                    .orderNo(AccountChangeType.agent_fund_interest.getPrefix() + CommonFunction.generalSn(CommonFunction.generalId()))
                    .amount(fundIncomeRecord.getInterestAmount())
                    .type(ChargeType.agent_fund_interest)
                    .status(ChargeStatus.chain_success)
                    .createTime(LocalDateTime.now())
                    .completeTime(LocalDateTime.now())
                    .build();
            orderService.save(agentOrder);
            // 减少余额
            accountBalanceService.decrease(agentId, ChargeType.agent_fund_interest, fundIncomeRecord.getCoin(), fundIncomeRecord.getInterestAmount(), agentOrder.getOrderNo(), CurrencyLogDes.代理基金利息.name());

            //生成一笔订单
            Order order = Order.builder()
                    .uid(uid)
                    .coin(fundIncomeRecord.getCoin())
                    .relatedId(fundIncomeRecord.getId())
                    .orderNo(AccountChangeType.fund_interest.getPrefix() + CommonFunction.generalSn(CommonFunction.generalId()))
                    .amount(fundIncomeRecord.getInterestAmount())
                    .type(ChargeType.fund_interest)
                    .status(ChargeStatus.chain_success)
                    .createTime(LocalDateTime.now())
                    .completeTime(LocalDateTime.now())
                    .build();
            orderService.save(order);
            // 增加余额
            accountBalanceService.increase(uid, ChargeType.fund_interest, fundIncomeRecord.getCoin(), fundIncomeRecord.getInterestAmount(), order.getOrderNo(), CurrencyLogDes.基金利息.name());


        });
    }

    @Override
    public FundAuditRecordVO getIncomeAuditRecord(Long id) {
        return null;
    }
}
