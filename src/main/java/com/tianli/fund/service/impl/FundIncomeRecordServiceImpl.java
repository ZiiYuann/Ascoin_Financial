package com.tianli.fund.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import com.tianli.fund.convert.FundRecordConvert;
import com.tianli.fund.dao.FundIncomeRecordMapper;
import com.tianli.fund.dto.FundIncomeAmountDTO;
import com.tianli.fund.entity.FundIncomeRecord;
import com.tianli.fund.entity.FundRecord;
import com.tianli.fund.entity.FundReview;
import com.tianli.fund.enums.FundReviewStatus;
import com.tianli.fund.enums.FundReviewType;
import com.tianli.fund.query.FundIncomeQuery;
import com.tianli.fund.service.IFundIncomeRecordService;
import com.tianli.fund.service.IFundRecordService;
import com.tianli.fund.service.IFundReviewService;
import com.tianli.fund.vo.FundIncomeRecordVO;
import com.tianli.management.dto.AmountDto;
import com.tianli.management.service.IWalletAgentService;
import com.tianli.management.vo.FundIncomeAmountVO;
import com.tianli.management.vo.WalletAgentVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
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

    @Resource
    private FundIncomeRecordMapper fundIncomeRecordMapper;
    @Resource
    private OrderService orderService;
    @Resource
    private IFundReviewService fundReviewService;
    @Resource
    private FundRecordConvert fundRecordConvert;
    @Resource
    private AccountBalanceService accountBalanceService;
    @Resource
    private IFundRecordService fundRecordService;
    @Resource
    private IWalletAgentService walletAgentService;

    @Override
    public List<AmountDto> getAmountByUidAndStatus(Long uid, Long agentId, Integer status) {
        return getAmountByUidAndStatus(uid, agentId, List.of(status));
    }

    @Override
    public List<AmountDto> getAmountByUidAndStatus(Long uid, Long agentId, List<Integer> status) {
        FundIncomeQuery query = new FundIncomeQuery();
        query.setUid(uid);
        query.setAgentId(agentId);
        query.setStatus(status);
        List<FundIncomeAmountDTO> fundIncomeAmountDTOS = getAmount(query);
        return fundIncomeAmountDTOS.stream().map(fundIncomeAmountDTO -> new AmountDto(fundIncomeAmountDTO.getTotalAmount(), fundIncomeAmountDTO.getCoin())).collect(Collectors.toList());
    }

    @Override
    public IPage<FundIncomeRecordVO> getPage(PageQuery<FundIncomeRecord> page, FundIncomeQuery query) {
        return fundIncomeRecordMapper.selectIncomePage(page.page(), query);
    }

    @Override
    public IPage<FundIncomeRecordVO> getSummaryPage(PageQuery<FundIncomeRecord> page, FundIncomeQuery query) {
        Page<FundIncomeRecord> pageQuery = page.page();
        pageQuery.setOptimizeCountSql(false);
        return fundIncomeRecordMapper.selectSummaryIncomePage(pageQuery, query);
    }

    @Override
    public List<FundIncomeAmountDTO> getAmount(FundIncomeQuery query) {
        List<FundIncomeAmountDTO> fundIncomeAmountDTOS = fundIncomeRecordMapper.selectAmount(query);
        fundIncomeAmountDTOS.remove(null);
        return fundIncomeAmountDTOS;
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
        return this.count(new QueryWrapper<FundIncomeRecord>().lambda().eq(FundIncomeRecord::getProductId, productId).eq(FundIncomeRecord::getStatus, FundIncomeStatus.wait_audit));
    }

    @Override
    @Transactional
    public void incomeAudit(FundAuditBO bo) {
        List<Long> ids = bo.getIds();
        FundReviewStatus status = bo.getStatus();
        Long agentId = AgentContent.getAgentId();
        WalletAgentVO agentVO = walletAgentService.getById(agentId);

        ids.forEach(id -> {
            FundIncomeRecord fundIncomeRecord = this.getById(id);
            validFundIncomeRecordReview(id, fundIncomeRecord);

            Long uid = fundIncomeRecord.getUid();

            // 保存审核记录信息
            FundReview fundReview = FundReview.builder().rId(id).type(FundReviewType.income).status(status == FundReviewStatus.success ? FundReviewStatus.success : FundReviewStatus.fail).remark(bo.getRemark()).createTime(LocalDateTime.now()).build();
            fundReviewService.save(fundReview);

            // 设置收益记录的状态
            fundIncomeRecord.setStatus(status == FundReviewStatus.success ? FundIncomeStatus.audit_success : FundIncomeStatus.audit_failure);

            if (status == FundReviewStatus.success) {
                // 订单【代理基金支付利息】 对于代理而言
                Order agentOrder = Order.builder()
                        .uid(agentVO.getUid())
                        .coin(fundIncomeRecord.getCoin())
                        .relatedId(fundIncomeRecord.getId())
                        .orderNo(AccountChangeType.agent_fund_interest.getPrefix() + CommonFunction.generalSn(CommonFunction.generalId()))
                        .amount(fundIncomeRecord.getInterestAmount())
                        .type(ChargeType.agent_fund_interest)
                        .status(ChargeStatus.chain_success)
                        .createTime(LocalDateTime.now()).completeTime(LocalDateTime.now()).build();
                orderService.save(agentOrder);
                // 减少余额
                accountBalanceService.decrease(agentVO.getUid(), ChargeType.agent_fund_interest, fundIncomeRecord.getCoin(), fundIncomeRecord.getInterestAmount(), agentOrder.getOrderNo(), CurrencyLogDes.代理基金利息.name());

                //订单【基金利息】对于客户而言
                Order order = Order.builder()
                        .uid(uid)
                        .coin(fundIncomeRecord.getCoin())
                        .relatedId(fundIncomeRecord.getId())
                        .orderNo(AccountChangeType.fund_interest.getPrefix() + CommonFunction.generalSn(CommonFunction.generalId()))
                        .amount(fundIncomeRecord.getInterestAmount())
                        .type(ChargeType.fund_interest)
                        .status(ChargeStatus.chain_success)
                        .createTime(LocalDateTime.now())
                        .completeTime(LocalDateTime.now()).build();
                orderService.save(order);
                // 增加余额
                accountBalanceService.increase(uid, ChargeType.fund_interest, fundIncomeRecord.getCoin(), fundIncomeRecord.getInterestAmount(), order.getOrderNo(), CurrencyLogDes.基金利息.name());

                //持仓记录待发已发修改
                FundRecord fundRecord = fundRecordService.getById(fundIncomeRecord.getFundId());
                fundRecord.setWaitIncomeAmount(fundRecord.getWaitIncomeAmount().subtract(fundIncomeRecord.getInterestAmount()));
                fundRecord.setIncomeAmount(fundRecord.getIncomeAmount().add(fundIncomeRecord.getInterestAmount()));
//                fundRecord.setCumulativeIncomeAmount(fundRecord.getCumulativeIncomeAmount().add(fundIncomeRecord.getInterestAmount()));
                fundRecordService.updateById(fundRecord);
                fundIncomeRecord.setOrderNo(order.getOrderNo());
            }

            this.updateById(fundIncomeRecord);
        });
    }

    @Override
    public List<FundReviewVO> getIncomeAuditRecord(Long id) {
        FundIncomeRecord incomeRecord = this.getById(id);
        if (Objects.isNull(incomeRecord)) ErrorCodeEnum.INCOME_NOT_EXIST.throwException();
        List<FundReview> fundReviews = fundReviewService.getListByRid(id);
        return fundReviews.stream().map(fundReview -> fundRecordConvert.toReviewVO(fundReview)).collect(Collectors.toList());
    }

    /**
     * 校验收益记录是否有效
     */
    private void validFundIncomeRecordReview(Long id, FundIncomeRecord fundIncomeRecord) {
        String msg = String.format("审核记录id：【%s】存在异常，请排查", id);
        // 判断持有记录存在与否
        if (Objects.isNull(fundIncomeRecord)) throw ErrorCodeEnum.INCOME_NOT_EXIST.generalException(msg);
        // 判断记录的状态是否有效
        if (!fundIncomeRecord.getStatus().equals(FundIncomeStatus.wait_audit) && !fundIncomeRecord.getStatus().equals(FundIncomeStatus.audit_failure))
            throw ErrorCodeEnum.INCOME_STATUS_ERROR.generalException(msg);
    }

}