package com.tianli.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.product.dto.UserHoldRecordDto;
import com.tianli.product.entity.ProductHoldRecord;
import com.tianli.product.financial.query.ProductHoldQuery;
import com.tianli.product.mapper.ProductHoldRecordMapper;
import com.tianli.product.service.ProductHoldRecordService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

    @Override
    public IPage<UserHoldRecordDto> userHoldRecordPage(ProductHoldQuery query, Page<ProductHoldRecord> page) {
        IPage<Long> uidPage = this.baseMapper.holdUidPage(page, query);
        return uidPage.convert(uid -> {
            List<ProductHoldRecord> productHoldRecords = this.baseMapper.listByUid(uid);
            return UserHoldRecordDto.builder().uid(uid).records(productHoldRecords).build();
        });
    }
}
