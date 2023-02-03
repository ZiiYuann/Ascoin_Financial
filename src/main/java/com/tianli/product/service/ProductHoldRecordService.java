package com.tianli.product.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tianli.product.dto.UserHoldRecordDto;
import com.tianli.product.entity.ProductHoldRecord;
import com.tianli.product.afinancial.query.ProductHoldQuery;

import java.util.List;

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

    /**
     * 持仓用户分页
     */
    IPage<UserHoldRecordDto> userHoldRecordPage(ProductHoldQuery query, Page<ProductHoldRecord> page);

    List<UserHoldRecordDto> userHoldRecordData(ProductHoldQuery query);
}
