package com.tianli.financial.service;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.common.CommonFunction;
import com.tianli.common.RedisLockConstants;
import com.tianli.common.lock.RedisLock;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.financial.convert.FinancialConverter;
import com.tianli.financial.entity.FinancialProduct;
import com.tianli.financial.entity.FinancialRecord;
import com.tianli.financial.enums.ProductStatus;
import com.tianli.financial.enums.RecordStatus;
import com.tianli.financial.mapper.FinancialProductMapper;
import com.tianli.financial.vo.FinancialProductVO;
import com.tianli.management.query.FinancialProductEditQuery;
import com.tianli.management.query.FinancialProductEditStatusQuery;
import com.tianli.management.query.FinancialProductsQuery;
import com.tianli.mconfig.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.tianli.common.ConfigConstants.SYSTEM_PURCHASE__MIN_AMOUNT;

@Slf4j
@Service
public class FinancialProductService extends ServiceImpl<FinancialProductMapper, FinancialProduct> {

    @Resource
    private FinancialConverter financialConverter;
    @Resource
    private FinancialProductMapper financialProductMapper;
    @Resource
    private FinancialRecordService financialRecordService;
    @Resource
    private ConfigService configService;
    @Resource
    private RedisLock redisLock;

    /**
     * 删除产品
     */
    @Transactional
    public boolean delete(Long productId){
        List<FinancialRecord> financialRecords = financialRecordService.selectByProductId(productId);
        Optional<FinancialRecord> match = financialRecords.stream()
                .filter(financialRecord -> RecordStatus.PROCESS.equals(financialRecord.getStatus())).findAny();
        if(match.isPresent()){
            log.info("productId ：{} , 用户持有中不允许删除 ",productId);
            ErrorCodeEnum.PRODUCT_USER_HOLD.throwException();
        }
        return financialProductMapper.deleteById(productId) > 0;
    }

    /**
     * 保存或者更新产品信息
     */
    @Transactional
    public void saveOrUpdate(FinancialProductEditQuery financialProductQuery) {
        FinancialProduct product = financialConverter.toDO(financialProductQuery);
        if(Objects.isNull(financialProductQuery.getLimitPurchaseQuota())){
            String sysPurchaseMinAmount = configService.get(SYSTEM_PURCHASE__MIN_AMOUNT);
            product.setLimitPurchaseQuota(BigDecimal.valueOf(Double.parseDouble(sysPurchaseMinAmount)));
        }

        if (ObjectUtil.isNull(product.getId())) {
            product.setCreateTime(LocalDateTime.now());
            product.setId(CommonFunction.generalId());
        } else {
            product.setUpdateTime(LocalDateTime.now());
        }
        super.saveOrUpdate(product);
    }

    /**
     * 修改产品状态
     */
    @Transactional
    public void editProductStatus(FinancialProductEditStatusQuery query){
        try {
            if(ProductStatus.close.equals(query.getStatus())){
                redisLock.lock(RedisLockConstants.PRODUCT_CLOSE_LOCK_PREFIX + query.getProductId(),5L, TimeUnit.SECONDS);
            }

            FinancialProduct product = financialProductMapper.selectById(query.getProductId());
            product = Optional.ofNullable(product).orElseThrow(ErrorCodeEnum.ARGUEMENT_ERROR::generalException);

            product.setUpdateTime(LocalDateTime.now());
            product.setStatus(query.getStatus());
            if( financialProductMapper.updateById(product) <= 0){
                ErrorCodeEnum.SYSTEM_ERROR.throwException();
            }
        }catch (Exception e){
            e.printStackTrace();
            throw e;
        }finally {
            redisLock.unlock(RedisLockConstants.PRODUCT_CLOSE_LOCK_PREFIX + query.getProductId());
        }

    }

    /**
     * 查询产品列表数据
     */
    public IPage<FinancialProductVO> selectListByQuery(IPage<FinancialProduct> page, FinancialProductsQuery query){
        LambdaQueryWrapper<FinancialProduct> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper = queryWrapper.eq(FinancialProduct :: isDeleted,false);
        if(StringUtils.isNotBlank(query.getName())){
            queryWrapper = queryWrapper.like(FinancialProduct :: getName,query.getName());
        }
        if(Objects.nonNull(query.getType())){
            queryWrapper = queryWrapper.eq(FinancialProduct :: getType,query.getType());
        }
        if(Objects.nonNull(query.getStatus())){
            queryWrapper = queryWrapper.eq(FinancialProduct :: getStatus,query.getStatus());
        }

        if(Objects.nonNull(query.getCoin())){
            queryWrapper = queryWrapper.eq(FinancialProduct :: getCoin,query.getCoin());
        }

        queryWrapper = queryWrapper.orderByDesc(FinancialProduct :: getCreateTime);

        IPage<FinancialProduct> financialProductIPage = financialProductMapper.selectPage(page, queryWrapper);
        return financialProductIPage.convert(financialConverter ::toFinancialProductVO);
    }
}
