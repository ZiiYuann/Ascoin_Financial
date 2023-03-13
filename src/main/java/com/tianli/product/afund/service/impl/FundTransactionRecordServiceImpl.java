package com.tianli.product.afund.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
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
import com.tianli.common.webhook.WebHookService;
import com.tianli.common.webhook.WebHookTemplate;
import com.tianli.currency.service.CurrencyService;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.management.dto.AmountDto;
import com.tianli.management.entity.WalletAgentProduct;
import com.tianli.management.service.IWalletAgentProductService;
import com.tianli.management.service.IWalletAgentService;
import com.tianli.management.vo.FundTransactionAmountVO;
import com.tianli.management.vo.WalletAgentVO;
import com.tianli.product.afund.contant.FundTransactionStatus;
import com.tianli.product.afund.convert.FundRecordConvert;
import com.tianli.product.afund.dao.FundTransactionRecordMapper;
import com.tianli.product.afund.dto.FundTransactionAmountDTO;
import com.tianli.product.afund.entity.FundIncomeRecord;
import com.tianli.product.afund.entity.FundRecord;
import com.tianli.product.afund.entity.FundReview;
import com.tianli.product.afund.entity.FundTransactionRecord;
import com.tianli.product.afund.enums.FundReviewStatus;
import com.tianli.product.afund.enums.FundReviewType;
import com.tianli.product.afund.enums.FundTransactionType;
import com.tianli.product.afund.query.FundTransactionQuery;
import com.tianli.product.afund.service.IFundIncomeRecordService;
import com.tianli.product.afund.service.IFundRecordService;
import com.tianli.product.afund.service.IFundReviewService;
import com.tianli.product.afund.service.IFundTransactionRecordService;
import com.tianli.product.afund.vo.FundTransactionRecordVO;
import com.tianli.product.service.FinancialProductService;
import com.tianli.product.service.ProductHoldRecordService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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

    @Resource
    private FundTransactionRecordMapper fundTransactionRecordMapper;
    @Resource
    private OrderService orderService;
    @Resource
    private AccountBalanceService accountBalanceService;
    @Resource
    private IFundRecordService fundRecordService;
    @Resource
    private IFundReviewService fundReviewService;
    @Resource
    private FundRecordConvert fundRecordConvert;
    @Resource
    private FinancialProductService financialProductService;
    @Resource
    private IWalletAgentService walletAgentService;
    @Resource
    private IWalletAgentProductService walletAgentProductService;
    @Resource
    private IFundIncomeRecordService fundIncomeRecordService;
    @Resource
    private WebHookService webHookService;
    @Resource
    private CurrencyService currencyService;
    @Resource
    private ProductHoldRecordService productHoldRecordService;

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
                .purchaseAmount(currencyService.calDollarAmount(purchaseAmount))
                .redemptionAmount(currencyService.calDollarAmount(redemptionAmount))
                .build();
    }

    @Override
    @Transactional
    public void redemptionAudit(FundAuditBO bo) {
        Long agentId = AgentContent.getAgentId();
        WalletAgentVO agentVO = walletAgentService.getById(agentId);

        FundReviewStatus status = bo.getStatus();
        List<Long> ids = bo.getIds();
        ids.forEach(id -> {
            // 判断交易记录是否存在
            FundTransactionRecord fundTransactionRecord = fundTransactionRecordMapper.selectById(id);
            fundTransactionRecord = Optional.ofNullable(fundTransactionRecord).orElseThrow(ErrorCodeEnum.TRANSACTION_NOT_EXIST::generalException);
            // 判断交易记录是否为待审核
            if (!fundTransactionRecord.getStatus().equals(FundTransactionStatus.wait_audit)) {
                ErrorCodeEnum.STATUS_NOT_WAIT.throwException();
            }
            // 判断持有记录是否存在
            Long fundId = fundTransactionRecord.getFundId();
            FundRecord fundRecord = fundRecordService.getById(fundId);
            fundRecord = Optional.ofNullable(fundRecord).orElseThrow(ErrorCodeEnum.FUND_RECORD_NOT_EXIST::generalException);
            // 判断产品和代理人绑定关系是否存在
            Long productId = fundRecord.getProductId();
            WalletAgentProduct walletAgentProduct = walletAgentProductService.getByProductId(productId);
            walletAgentProduct = Optional.ofNullable(walletAgentProduct).orElseThrow(ErrorCodeEnum.AGENT_PRODUCT_NOT_EXIST::generalException);
            // 判断产品是否是此代理人关联产品
            if (!walletAgentProduct.getAgentId().equals(agentId)) {
                ErrorCodeEnum.NOT_CURRENT_AGENT.throwException();
            }


            Long uid = fundTransactionRecord.getUid();
            if (status == FundReviewStatus.success) {
                redeemExaminePass(agentVO, uid, fundTransactionRecord, bo);
            }

            if (status != FundReviewStatus.success) {
                redeemExamineFail(fundTransactionRecord, bo);
            }

            redeemFinishOperation(fundId);

        });

    }

    /**
     * 赎回完毕操作,如果赎回金额到0，且全部审核通过，则修改利息发放状态为待发放
     */
    private void redeemFinishOperation(Long fundId) {

        FundRecord fundRecord = fundRecordService.getById(fundId);
        if (BigDecimal.ZERO.compareTo(fundRecord.getHoldAmount()) != 0) {
            return;
        }

        List<FundTransactionRecord> fundTransactionRecords =
                fundTransactionRecordMapper.selectList(new LambdaQueryWrapper<FundTransactionRecord>()
                        .eq(FundTransactionRecord::getFundId, fundRecord.getId())
                        .eq(FundTransactionRecord::getType, FundTransactionType.redemption)
                        .eq(FundTransactionRecord::getStatus, FundTransactionStatus.wait_audit));
        if (CollectionUtils.isNotEmpty(fundTransactionRecords)) {
            return;
        }

        List<FundIncomeRecord> fundIncomeRecords = fundIncomeRecordService.list(new LambdaQueryWrapper<FundIncomeRecord>()
                .eq(FundIncomeRecord::getFundId, fundRecord.getId())
                .eq(FundIncomeRecord::getStatus, 1));

        fundIncomeRecords.forEach(incomeRecord -> incomeRecord.setStatus(2));
        fundIncomeRecordService.updateBatchById(fundIncomeRecords);
        productHoldRecordService.delete(fundRecord.getId(), fundRecord.getProductId(), fundRecord.getId());
    }

    /**
     * 赎回审核未通过
     */
    private void redeemExamineFail(FundTransactionRecord fundTransactionRecord, FundAuditBO fundAuditBO) {
        FundReview fundReview = FundReview.builder()
                .rId(fundTransactionRecord.getId())
                .type(FundReviewType.redemption)
                .status(FundReviewStatus.fail)
                .remark(fundAuditBO.getRemark())
                .createTime(LocalDateTime.now())
                .build();
        fundReviewService.save(fundReview);

        fundTransactionRecord.setStatus(FundTransactionStatus.fail);
        fundTransactionRecordMapper.updateById(fundTransactionRecord);
        fundRecordService.increaseAmount(fundTransactionRecord.getFundId(), fundTransactionRecord.getTransactionAmount());
    }

    /**
     * 赎回审核通过
     */
    private void redeemExaminePass(WalletAgentVO agentVO, Long uid, FundTransactionRecord fundTransactionRecord, FundAuditBO fundAuditBO) {
        //生成一笔订单
        Order agentOrder = Order.builder()
                .uid(agentVO.getUid())
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
        accountBalanceService.decrease(agentVO.getUid(), ChargeType.agent_fund_redeem, fundTransactionRecord.getCoin()
                , fundTransactionRecord.getTransactionAmount(), agentOrder.getOrderNo());

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
        accountBalanceService.increase(uid, ChargeType.fund_redeem, fundTransactionRecord.getCoin()
                , fundTransactionRecord.getTransactionAmount(), order.getOrderNo());

        FundReview fundReview = FundReview.builder()
                .rId(fundTransactionRecord.getId())
                .type(FundReviewType.redemption)
                .status(FundReviewStatus.success)
                .remark(fundAuditBO.getRemark())
                .createTime(LocalDateTime.now())
                .build();
        fundReviewService.save(fundReview);

        fundTransactionRecord.setStatus(FundTransactionStatus.success);
        fundTransactionRecordMapper.updateById(fundTransactionRecord);

        financialProductService.reduceUseQuota(fundTransactionRecord.getProductId(), fundTransactionRecord.getTransactionAmount());

        // 发送消息
        String msg = WebHookTemplate.fundExamine(uid, fundTransactionRecord.getTransactionAmount()
                , fundTransactionRecord.getCoin());
        webHookService.fundSend(msg);
    }

    @Override
    public List<FundReviewVO> getRedemptionAuditRecord(Long id) {
        FundTransactionRecord fundTransactionRecord = fundTransactionRecordMapper.selectById(id);
        if (Objects.isNull(fundTransactionRecord)) ErrorCodeEnum.TRANSACTION_NOT_EXIST.throwException();
        List<FundReview> fundReviews = fundReviewService.getListByRid(id);
        return fundReviews.stream().map(fundReview -> fundRecordConvert.toReviewVO(fundReview)).collect(Collectors.toList());
    }


    @Override
    public Integer getWaitRedemptionCount(Long productId) {
        return fundTransactionRecordMapper.selectCount(new QueryWrapper<FundTransactionRecord>().lambda()
                .eq(FundTransactionRecord::getProductId, productId)
                .eq(FundTransactionRecord::getType, FundTransactionType.redemption)
                .eq(FundTransactionRecord::getStatus, FundTransactionStatus.wait_audit));
    }
}
