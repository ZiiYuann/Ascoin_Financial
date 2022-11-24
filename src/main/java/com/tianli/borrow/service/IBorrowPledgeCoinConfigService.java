package com.tianli.borrow.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.borrow.bo.BorrowPledgeCoinConfigBO;
import com.tianli.borrow.entity.BorrowPledgeCoinConfig;
import com.tianli.borrow.vo.BorrowPledgeCoinConfigVO;
import com.tianli.common.PageQuery;

/**
 * <p>
 * 质押币种配置 服务类
 * </p>
 *
 * @author xianeng
 * @since 2022-07-28
 */
public interface IBorrowPledgeCoinConfigService extends IService<BorrowPledgeCoinConfig> {

    void saveConfig(BorrowPledgeCoinConfigBO bo);

    void updateConfig(BorrowPledgeCoinConfigBO bo);

    void delConfig(Long[] ids);

    IPage<BorrowPledgeCoinConfigVO> pageList(PageQuery<BorrowPledgeCoinConfig> pageQuery, String coin);

    BorrowPledgeCoinConfig getByCoin(String coin);
}
