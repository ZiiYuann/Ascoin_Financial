package com.tianli.management.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.agent.management.auth.AgentContent;
import com.tianli.agent.management.vo.FundProductStatisticsVO;
import com.tianli.common.PageQuery;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.fund.service.IFundIncomeRecordService;
import com.tianli.fund.service.IFundTransactionRecordService;
import com.tianli.management.entity.WalletAgentProduct;
import com.tianli.management.dao.WalletAgentProductMapper;
import com.tianli.management.service.IWalletAgentProductService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    @Autowired
    private WalletAgentProductMapper walletAgentProductMapper;

    @Autowired
    private IFundTransactionRecordService transactionRecordService;

    @Autowired
    private IFundIncomeRecordService fundIncomeRecordService;

    @Override
    public Integer getCount(Long productId) {
        return walletAgentProductMapper.selectCountByProjectId(productId);
    }

    @Override
    public void deleteByAgentId(Long agentId) {
        walletAgentProductMapper.deleteByAgentId(agentId);
    }

    @Override
    public void deleteByProductId(Long productId) {
        Integer redemptionCount = transactionRecordService.getWaitRedemptionCount(productId);
        if(redemptionCount > 0) ErrorCodeEnum.EXIST_WAIT_REDEMPTION.throwException();
        Integer waitPayCount = fundIncomeRecordService.getWaitPayCount(productId);
        if(waitPayCount > 0) ErrorCodeEnum.EXIST_WAIT_INTEREST.throwException();
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
                .eq(WalletAgentProduct::getAgentId,agentId));
    }

    @Override
    public IPage<FundProductStatisticsVO> getPage(PageQuery<WalletAgentProduct> pageQuery) {
        Long agentUId = AgentContent.getAgentUId();
        return walletAgentProductMapper.selectPage(pageQuery.page(), agentUId);
    }
}
