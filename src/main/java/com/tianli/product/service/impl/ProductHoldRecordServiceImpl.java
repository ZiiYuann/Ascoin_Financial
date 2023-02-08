package com.tianli.product.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.product.dto.UserHoldRecordDto;
import com.tianli.product.entity.ProductHoldRecord;
import com.tianli.product.afinancial.query.ProductHoldQuery;
import com.tianli.product.mapper.ProductHoldRecordMapper;
import com.tianli.product.service.ProductHoldRecordService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-02
 **/
@Service
public class ProductHoldRecordServiceImpl extends ServiceImpl<ProductHoldRecordMapper, ProductHoldRecord>
        implements ProductHoldRecordService {


    @Override
    public boolean saveDo(ProductHoldRecord entity) {
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
            query.setUid(uid);
            List<ProductHoldRecord> productHoldRecords = this.baseMapper.list(query);
            return UserHoldRecordDto.builder().uid(uid).records(productHoldRecords).build();
        });
    }

    @Override
    public List<UserHoldRecordDto> userHoldRecordData(ProductHoldQuery query) {
        List<Long> uidPage = this.baseMapper.holdUids(query);
        return uidPage.stream().map(uid -> {
            query.setUid(uid);
            List<ProductHoldRecord> productHoldRecords = this.baseMapper.list(query);
            return UserHoldRecordDto.builder().uid(uid).records(productHoldRecords).build();
        }).collect(Collectors.toList());
    }
}
