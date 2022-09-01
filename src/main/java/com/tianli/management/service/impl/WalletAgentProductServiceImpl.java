package com.tianli.management.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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

    @Override
    public boolean exist(Long productId) {
        Integer count = walletAgentProductMapper.selectCountByProjectId(productId);
        return count > 0;
    }

    @Override
    public void deleteByAgentId(Long agentId) {
        walletAgentProductMapper.deleteByAgentId(agentId);
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
}
