package com.tianli.fund.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.account.enums.AccountChangeType;
import com.tianli.account.service.AccountBalanceService;
import com.tianli.agent.management.auth.AgentContent;
import com.tianli.agent.management.bo.FundAuditBO;
import com.tianli.agent.management.vo.FundReviewVO;
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
import com.tianli.fund.convert.FundRecordConvert;
import com.tianli.fund.dao.FundIncomeRecordMapper;
import com.tianli.fund.dto.FundIncomeAmountDTO;
import com.tianli.fund.entity.FundIncomeRecord;
import com.tianli.fund.entity.FundReview;
import com.tianli.fund.entity.FundTransactionRecord;
import com.tianli.fund.enums.FundReviewStatus;
import com.tianli.fund.enums.FundReviewType;
import com.tianli.fund.query.FundIncomeQuery;
import com.tianli.fund.service.IFundIncomeRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.fund.service.IFundReviewService;
import com.tianli.fund.vo.FundIncomeRecordVO;
import com.tianli.management.dto.AmountDto;
import com.tianli.management.vo.FundIncomeAmountVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * <p>
 * 基金收益记录 服务实现类
 * </p>
 *
 * @author xianeng
 * @since 2022-08-30
 */
@Service
@Transactional
public class FundIncomeRecordServiceImpl extends ServiceImpl<FundIncomeRecordMapper, FundIncomeRecord> implements IFundIncomeRecordService {

    @Autowired
    private FundIncomeRecordMapper fundIncomeRecordMapper;

    @Autowired
    private OrderService orderService;
    @Autowired
    private IFundReviewService fundReviewService;

    @Autowired
    private FundRecordConvert fundRecordConvert;

    @Autowired
    private AccountBalanceService accountBalanceService;

    @Override
    public List<AmountDto> getAmountByUidAndStatus(Long uid, Integer status) {
        FundIncomeQuery query = new FundIncomeQuery();
        query.setUid(uid);
        query.setStatus(status);
        List<FundIncomeAmountDTO> fundIncomeAmountDTOS = getAmount(query);
        List<AmountDto> amountDtos = fundIncomeAmountDTOS.stream().map(fundIncomeAmountDTO ->
                new AmountDto(fundIncomeAmountDTO.getTotalAmount(), fundIncomeAmountDTO.getCoin())).collect(Collectors.toList());
        return amountDtos;
    }

    @Override
    public IPage<FundIncomeRecordVO> getPage(PageQuery<FundIncomeRecord> page, FundIncomeQuery query) {
        return fundIncomeRecordMapper.selectIncomePage(page.page(),query);
    }

    @Override
    public List<FundIncomeAmountDTO> getAmount(FundIncomeQuery query) {
        return fundIncomeRecordMapper.selectAmount(query);
    }

    @Override
    public FundIncomeAmountVO getIncomeAmount(FundIncomeQuery query) {
        List<FundIncomeAmountDTO> amount = getAmount(query);
        List<AmountDto> amountDtos = amount.stream().map(fundIncomeAmountDTO -> new AmountDto(fundIncomeAmountDTO.getTotalAmount(), fundIncomeAmountDTO.getCoin())).collect(Collectors.toList());
        BigDecimal dollarAmount = orderService.calDollarAmount(amountDtos);
        return FundIncomeAmountVO.builder().interestAmount(dollarAmount).build();
    }


    @Override
    public Integer getWaitPayCount(Long productId) {
        return this.count(new QueryWrapper<FundIncomeRecord>().lambda()
                .eq(FundIncomeRecord::getProductId,productId)
                .eq(FundIncomeRecord::getStatus, FundIncomeStatus.wait_audit)
        );
    }

    @Override
    public void incomeAudit(FundAuditBO bo) {
        List<Long> ids = bo.getIds();
        FundReviewStatus status = bo.getStatus();
        Long agentId = AgentContent.getAgentUId();
        ids.forEach(id->{
            FundIncomeRecord fundIncomeRecord = this.getById(id);
            if(Objects.isNull(fundIncomeRecord)) ErrorCodeEnum.INCOME_NOT_EXIST.throwException();
            if(!fundIncomeRecord.getStatus().equals(FundIncomeStatus.wait_audit)
                && !fundIncomeRecord.getStatus().equals(FundIncomeStatus.audit_failure)) ErrorCodeEnum.INCOME_STATUS_ERROR.throwException();
            Long uid = fundIncomeRecord.getUid();
            if(status == FundReviewStatus.success) {
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

                FundReview fundReview = FundReview.builder()
                        .rId(id)
                        .type(FundReviewType.income)
                        .status(FundReviewStatus.success)
                        .remark(bo.getRemark())
                        .createTime(LocalDateTime.now())
                        .build();
                fundReviewService.save(fundReview);

                fundIncomeRecord.setStatus(FundTransactionStatus.success);
                this.updateById(fundIncomeRecord);
            }else {
                FundReview fundReview = FundReview.builder()
                        .rId(id)
                        .type(FundReviewType.income)
                        .status(FundReviewStatus.fail)
                        .remark(bo.getRemark())
                        .createTime(LocalDateTime.now())
                        .build();
                fundReviewService.save(fundReview);

                fundIncomeRecord.setStatus(FundTransactionStatus.fail);
                this.updateById(fundIncomeRecord);
            }
        });
    }

    @Override
    public List<FundReviewVO> getIncomeAuditRecord(Long id) {
        FundIncomeRecord incomeRecord = this.getById(id);
        if(Objects.isNull(incomeRecord)) ErrorCodeEnum.INCOME_NOT_EXIST.throwException();
        List<FundReview> fundReviews = fundReviewService.getListByRid(id);
        return fundReviews.stream().map(fundReview -> fundRecordConvert.toReviewVO(fundReview)).collect(Collectors.toList());
    }

}
