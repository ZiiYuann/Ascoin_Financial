package com.tianli.borrow.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.borrow.bo.BorrowPledgeCoinConfigBO;
import com.tianli.borrow.entity.BorrowPledgeCoinConfig;
import com.tianli.borrow.dao.BorrowPledgeCoinConfigMapper;
import com.tianli.borrow.service.IBorrowPledgeCoinConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.borrow.vo.BorrowPledgeCoinConfigVO;
import com.tianli.common.PageQuery;
import com.tianli.common.blockchain.CurrencyCoin;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 质押币种配置 服务实现类
 * </p>
 *
 * @author xianeng
 * @since 2022-07-28
 */
@Service
public class BorrowPledgeCoinConfigServiceImpl extends ServiceImpl<BorrowPledgeCoinConfigMapper, BorrowPledgeCoinConfig> implements IBorrowPledgeCoinConfigService {

    @Override
    public void saveConfig(BorrowPledgeCoinConfigBO bo) {

    }

    @Override
    public void updateConfig(BorrowPledgeCoinConfigBO bo) {

    }

    @Override
    public void delConfig(Long[] ids) {

    }

    @Override
    public IPage<BorrowPledgeCoinConfigVO> pageList(PageQuery<BorrowPledgeCoinConfig> pageQuery, CurrencyCoin coin) {
        return null;
    }
}
