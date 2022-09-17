package com.tianli.management.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.agent.management.auth.AgentContent;
import com.tianli.agent.management.query.FundStatisticsQuery;
import com.tianli.agent.management.vo.FundProductStatisticsVO;
import com.tianli.common.PageQuery;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.fund.entity.FundRecord;
import com.tianli.fund.service.IFundIncomeRecordService;
import com.tianli.fund.service.IFundRecordService;
import com.tianli.fund.service.IFundTransactionRecordService;
import com.tianli.management.dao.WalletAgentProductMapper;
import com.tianli.management.entity.WalletAgentProduct;
import com.tianli.management.service.IWalletAgentProductService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <p>
 * 云钱包代理人和产品关联 服务实现类
 * </p>
 *
 * @author xianeng
 * @since 2022-08-30
 */
@Service
public class WalletAgentProductServiceImpl extends ServiceImpl<WalletAgentProductMapper, WalletAgentProduct> implements IWalletAgentProductService {

    @Resource
    private WalletAgentProductMapper walletAgentProductMapper;

    @Resource
    private IFundTransactionRecordService transactionRecordService;

    @Resource
    private IFundIncomeRecordService fundIncomeRecordService;
    @Resource
    private IFundRecordService fundRecordService;

    @Override
    public Integer getCount(Long productId) {
        return walletAgentProductMapper.selectCountByProjectId(productId);
    }

    @Override
    public void deleteByProductId(Long productId) {
        Integer redemptionCount = transactionRecordService.getWaitRedemptionCount(productId);
        if (redemptionCount > 0) ErrorCodeEnum.EXIST_WAIT_REDEMPTION.throwException();
        Integer waitPayCount = fundIncomeRecordService.getWaitPayCount(productId);
        if (waitPayCount > 0) ErrorCodeEnum.EXIST_WAIT_INTEREST.throwException();
        walletAgentProductMapper.deleteByProductId(productId);
    }

    @Override
    public WalletAgentProduct getByProductId(Long productId) {
        return walletAgentProductMapper.selectOne(new QueryWrapper<WalletAgentProduct>().lambda()
                .eq(WalletAgentProduct::getProductId, productId));
    }

    @Override
    public List<WalletAgentProduct> getByAgentId(Long agentId) {
        return walletAgentProductMapper.selectList(new QueryWrapper<WalletAgentProduct>().lambda()
                .eq(WalletAgentProduct::getAgentId, agentId));
    }

    @Override
    public List<Long> listProductIdExcludeAgentId(Long agentId) {
        LambdaQueryWrapper<WalletAgentProduct> queryWrapper = new LambdaQueryWrapper<>();
        if (Objects.nonNull(agentId)) {
            queryWrapper = queryWrapper.eq(false, WalletAgentProduct::getAgentId, agentId);
        }
        return Optional.ofNullable(walletAgentProductMapper.selectList(queryWrapper)).orElse(new ArrayList<>())
                .stream().map(WalletAgentProduct::getProductId).collect(Collectors.toList());
    }

    @Override
    public IPage<FundProductStatisticsVO> getPage(PageQuery<WalletAgentProduct> pageQuery, FundStatisticsQuery query) {
        Long agentId = AgentContent.getAgentId();
        return walletAgentProductMapper.selectPage(pageQuery.page(), agentId, query);
    }

    @Override
    public boolean canDelete(Long productId, boolean throwE) {
        // 该子产品被有持仓金额or待赎回金额or待发利息时，不允许删除
        LambdaQueryWrapper<FundRecord> queryWrapper = new LambdaQueryWrapper<FundRecord>()
                .select(FundRecord::getProductId);
        List<FundRecord> fundRecords = fundRecordService.list(queryWrapper);
        Optional<FundRecord> any = fundRecords.stream()
                .filter(fundRecord -> fundRecord.getHoldAmount().compareTo(BigDecimal.ZERO) > 0
                        || fundRecord.getWaitIncomeAmount().compareTo(BigDecimal.ZERO) > 0).findAny();

        if (throwE && any.isPresent()) {
            ErrorCodeEnum.PRODUCT_USER_HOLD.throwException();
        }
        return any.isEmpty();
    }

    @Override
    public boolean canDelete(Long productId) {
        return canDelete(productId, false);
    }
}
