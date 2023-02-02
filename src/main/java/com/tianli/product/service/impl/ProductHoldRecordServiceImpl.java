package com.tianli.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.product.entity.ProductHoldRecord;
import com.tianli.product.mapper.ProductHoldRecordMapper;
import com.tianli.product.service.ProductHoldRecordService;
import org.springframework.stereotype.Service;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-02
 **/
@Service
public class ProductHoldRecordServiceImpl extends ServiceImpl<ProductHoldRecordMapper, ProductHoldRecord>
        implements ProductHoldRecordService {


    @Override
    public boolean save(ProductHoldRecord entity) {
        return saveOrUpdate(entity, new LambdaQueryWrapper<ProductHoldRecord>()
                .eq(ProductHoldRecord::getUid, entity.getUid())
                .eq(ProductHoldRecord::getProductId, entity.getProductId())
                .eq(ProductHoldRecord::getRecordId, entity.getRecordId()));
    }

    @Override
    public void delete(Long uid, Long productId, Long recordId) {
        this.remove(new LambdaQueryWrapper<ProductHoldRecord>()
                .eq(ProductHoldRecord::getUid, uid)
                .eq(ProductHoldRecord::getProductId, productId)
                .eq(ProductHoldRecord::getRecordId, recordId));
    }
}
