package com.tianli.management.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.agent.management.query.FundStatisticsQuery;
import com.tianli.agent.management.vo.FundProductStatisticsVO;
import com.tianli.common.PageQuery;
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
    Integer getCount(Long productId);

    void deleteByAgentId(Long agentId);

    void deleteByProductId(Long productId);

    WalletAgentProduct getByProductId(Long productId);

    List<WalletAgentProduct> getByAgentId(Long id);

    /**
     * 获取当前代理人之外的基金产品id
     */
    List<Long> listProductIdExcludeAgentId(Long agentId);

    IPage<FundProductStatisticsVO> getPage(PageQuery<WalletAgentProduct> pageQuery, FundStatisticsQuery query);

    /**
     * 判断基金产品是否可以删除
     */
    boolean canDelete(Long productId);
}
