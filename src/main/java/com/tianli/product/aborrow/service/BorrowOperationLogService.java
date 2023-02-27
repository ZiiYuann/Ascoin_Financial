package com.tianli.product.aborrow.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.product.aborrow.entity.BorrowOperationLog;
import com.tianli.product.aborrow.query.BorrowOperationLogQuery;
import com.tianli.product.aborrow.vo.BorrowOperationLogVO;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-10
 **/
public interface BorrowOperationLogService extends IService<BorrowOperationLog> {

    IPage<BorrowOperationLogVO> logs(Page<BorrowOperationLog> page, BorrowOperationLogQuery query);

}
