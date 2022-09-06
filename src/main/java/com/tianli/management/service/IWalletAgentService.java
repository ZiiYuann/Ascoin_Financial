package com.tianli.management.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.common.PageQuery;
import com.tianli.management.vo.WalletAgentVO;
import com.tianli.management.bo.WalletAgentBO;
import com.tianli.management.entity.WalletAgent;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.management.query.WalletAgentQuery;

/**
 * <p>
 * 云钱包代理人 服务类
 * </p>
 *
 * @author xianeng
 * @since 2022-08-30
 */
public interface IWalletAgentService extends IService<WalletAgent> {

    void saveAgent(WalletAgentBO walletAgentBO);

    void updateAgent(WalletAgentBO walletAgentBO);

    void delAgent(Long id);

    WalletAgentVO getById(Long id);

    IPage<WalletAgentVO> getPage(PageQuery<WalletAgent> pageQuery, WalletAgentQuery query);

    WalletAgent getByAgentName(String agentName);


}
