package com.tianli.product.aborrow.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.account.query.IdsQuery;
import com.tianli.management.query.BorrowHedgeEntrustIoUQuery;
import com.tianli.product.aborrow.entity.BorrowHedgeEntrust;
import com.tianli.product.aborrow.query.MBorrowHedgeQuery;
import com.tianli.product.aborrow.vo.MBorrowHedgeEntrustVO;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-24
 **/
public interface BorrowHedgeEntrustService extends IService<BorrowHedgeEntrust> {

    void manual(BorrowHedgeEntrustIoUQuery query);

    IPage<MBorrowHedgeEntrustVO> vos(Page<BorrowHedgeEntrust> page, MBorrowHedgeQuery query);

    void cancel(IdsQuery query);


}
