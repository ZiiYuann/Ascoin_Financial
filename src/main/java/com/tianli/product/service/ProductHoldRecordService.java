package com.tianli.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.product.entity.ProductHoldRecord;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-02
 **/
public interface ProductHoldRecordService extends IService<ProductHoldRecord> {

    /**
     * 删除
     */
    void delete(Long uid, Long productId, Long recordId);
}
