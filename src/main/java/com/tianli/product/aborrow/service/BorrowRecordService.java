package com.tianli.product.aborrow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.product.aborrow.entity.BorrowRecord;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-09
 **/
public interface BorrowRecordService extends IService<BorrowRecord> {

    BorrowRecord getAndInit(Long uid, Boolean autoReplenishment);

}
