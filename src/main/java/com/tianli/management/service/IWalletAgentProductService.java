package com.tianli.management.service;

import com.tianli.management.entity.WalletAgentProduct;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 云钱包代理人和产品关联 服务类
 * </p>
 *
 * @author xianeng
 * @since 2022-08-30
 */
public interface IWalletAgentProductService extends IService<WalletAgentProduct> {
    boolean exist(Long productId);

    void deleteByAgentId(Long agentId);

    WalletAgentProduct getByProductId(Long productId);

    List<WalletAgentProduct> getByAgentId(Long id);
}
