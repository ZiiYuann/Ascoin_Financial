package com.tianli.agent.management.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.agent.management.query.FundStatisticsQuery;
import com.tianli.agent.management.vo.FundProductStatisticsVO;
import com.tianli.agent.management.vo.MainPageVO;
import com.tianli.common.PageQuery;
import com.tianli.management.entity.WalletAgentProduct;

public interface FundAgentManageService {

    MainPageVO statistics(FundStatisticsQuery query);

    IPage<FundProductStatisticsVO> productStatistics(PageQuery<WalletAgentProduct> page);

}
