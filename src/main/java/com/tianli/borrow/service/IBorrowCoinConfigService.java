package com.tianli.borrow.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.borrow.bo.BorrowOrderConfigBO;
import com.tianli.borrow.entity.BorrowCoinConfig;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.borrow.vo.BorrowApplePageVO;
import com.tianli.common.PageQuery;
import com.tianli.common.blockchain.CurrencyCoin;

/**
 * <p>
 * 借币数据配置 服务类
 * </p>
 *
 * @author xn
 * @since 2022-07-21
 */
public interface IBorrowCoinConfigService extends IService<BorrowCoinConfig> {

    void saveConfig(BorrowOrderConfigBO bo);

    void updateConfig(BorrowOrderConfigBO bo);

    void delConfig(Long[] id);

    IPage<BorrowApplePageVO> pageList(PageQuery<BorrowCoinConfig> pageQuery, CurrencyCoin coin);

    BorrowCoinConfig getByCoin(CurrencyCoin coin);
}
