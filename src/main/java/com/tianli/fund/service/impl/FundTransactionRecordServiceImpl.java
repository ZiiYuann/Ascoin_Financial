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
import com.tianli.common.RedisLockConstants;
import com.tianli.currency.log.CurrencyLogDes;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.fund.contant.FundIncomeStatus;
import com.tianli.fund.contant.FundTransactionStatus;
import com.tianli.fund.convert.FundRecordConvert;
import com.tianli.fund.dao.FundTransactionRecordMapper;
import com.tianli.fund.dto.FundTransactionAmountDTO;
import com.tianli.fund.entity.FundIncomeRecord;
import com.tianli.fund.entity.FundReview;
import com.tianli.fund.entity.FundTransactionRecord;
import com.tianli.fund.enums.FundReviewStatus;
import com.tianli.fund.enums.FundReviewType;
import com.tianli.fund.enums.FundTransactionType;
import com.tianli.fund.query.FundTransactionQuery;
import com.tianli.fund.service.IFundIncomeRecordService;
import com.tianli.fund.service.IFundRecordService;
import com.tianli.fund.service.IFundReviewService;
import com.tianli.fund.service.IFundTransactionRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.fund.vo.FundTransactionRecordVO;
import com.tianli.management.dto.AmountDto;
import com.tianli.management.vo.FundTransactionAmountVO;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
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
    private IFundReviewService fundReviewService;

    @Autowired
    private FundRecordConvert fundRecordConvert;


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
        FundReviewStatus status = bo.getStatus();
        List<Long> ids = bo.getIds();
        ids.forEach(id ->{
            FundTransactionRecord fundTransactionRecord = fundTransactionRecordMapper.selectById(id);
            if(Objects.isNull(fundTransactionRecord) ||
                    !fundTransactionRecord.getStatus().equals(FundTransactionStatus.wait_audit)) ErrorCodeEnum.TRANSACTION_NOT_EXIST.throwException();
            Long uid = fundTransactionRecord.getUid();
            if(status == FundReviewStatus.success) {
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

                FundReview fundReview = FundReview.builder()
                        .rId(id)
                        .type(FundReviewType.redemption)
                        .status(FundReviewStatus.success)
                        .remark(bo.getRemark())
                        .createTime(LocalDateTime.now())
                        .build();
                fundReviewService.save(fundReview);

                fundTransactionRecord.setStatus(FundTransactionStatus.success);
                fundTransactionRecordMapper.updateById(fundTransactionRecord);
            }else {

                FundReview fundReview = FundReview.builder()
                        .rId(id)
                        .type(FundReviewType.redemption)
                        .status(FundReviewStatus.fail)
                        .remark(bo.getRemark())
                        .createTime(LocalDateTime.now())
                        .build();
                fundReviewService.save(fundReview);

                fundTransactionRecord.setStatus(FundTransactionStatus.fail);
                fundTransactionRecordMapper.updateById(fundTransactionRecord);
                fundRecordService.increaseAmount(fundTransactionRecord.getFundId(),fundTransactionRecord.getTransactionAmount());
            }
        });

    }

    @Override
    public List<FundReviewVO> getRedemptionAuditRecord(Long id) {
        FundTransactionRecord fundTransactionRecord = fundTransactionRecordMapper.selectById(id);
        if(Objects.isNull(fundTransactionRecord)) ErrorCodeEnum.TRANSACTION_NOT_EXIST.throwException();
        List<FundReview> fundReviews = fundReviewService.getListByRid(id);
        return fundReviews.stream().map(fundReview -> fundRecordConvert.toReviewVO(fundReview)).collect(Collectors.toList());
    }


    @Override
    public Integer getWaitRedemptionCount(Long productId) {
        return fundTransactionRecordMapper.selectCount(new QueryWrapper<FundTransactionRecord>().lambda()
                 .eq(FundTransactionRecord::getProductId,productId)
                .eq(FundTransactionRecord::getType, FundTransactionType.redemption)
                .eq(FundTransactionRecord::getStatus,FundTransactionStatus.wait_audit));
    }
}
