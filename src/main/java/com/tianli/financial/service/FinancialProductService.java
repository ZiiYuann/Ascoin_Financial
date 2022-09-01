package com.tianli.financial.service;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tianli.common.CommonFunction;
import com.tianli.common.RedisLockConstants;
import com.tianli.common.lock.RedisLock;
import com.tianli.exception.ErrorCodeEnum;
import com.tianli.financial.convert.FinancialConverter;
import com.tianli.financial.dto.ProductRateDTO;
import com.tianli.financial.entity.FinancialProduct;
import com.tianli.financial.entity.FinancialRecord;
import com.tianli.financial.enums.ProductStatus;
import com.tianli.financial.enums.RecordStatus;
import com.tianli.financial.mapper.FinancialProductMapper;
import com.tianli.management.converter.ManagementConverter;
import com.tianli.management.dto.ProductSummaryDataDto;
import com.tianli.management.query.FinancialProductEditQuery;
import com.tianli.management.query.FinancialProductEditStatusQuery;
import com.tianli.management.query.FinancialProductsQuery;
import com.tianli.management.vo.MFinancialProductVO;
import com.tianli.mconfig.ConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.tianli.common.ConfigConstants.SYSTEM_PURCHASE_MIN_AMOUNT;

@Slf4j
@Service
public class FinancialProductService extends ServiceImpl<FinancialProductMapper, FinancialProduct> {

    @Resource
    private FinancialConverter financialConverter;
    @Resource
    private ManagementConverter managementConverter;
    @Resource
    private ManagementConverter managementConverter;
    @Resource
    private FinancialProductMapper financialProductMapper;
    @Resource
    private FinancialRecordService financialRecordService;
    @Resource
    private ConfigService configService;
    @Resource
    private RedisLock redisLock;
    @Resource
    private FinancialProductLadderRateService financialProductLadderRateService;

    /**
     * 删除产品
     */
    @Transactional
    public boolean delete(Long productId) {
        List<FinancialRecord> financialRecords = financialRecordService.selectByProductId(productId);
        Optional<FinancialRecord> match = financialRecords.stream()
                .filter(financialRecord -> RecordStatus.PROCESS.equals(financialRecord.getStatus())).findAny();
        if (match.isPresent()) {
            log.info("productId ：{} , 用户持有中不允许删除 ", productId);
            ErrorCodeEnum.PRODUCT_USER_HOLD.throwException();
        }
        return financialProductMapper.deleteById(productId) > 0;
    }

    /**
     * 保存或者更新产品信息
     */
    @Transactional
    public void saveOrUpdate(FinancialProductEditQuery financialProductQuery) {
        FinancialProduct productDO = financialConverter.toDO(financialProductQuery);
        if (Objects.isNull(financialProductQuery.getLimitPurchaseQuota())) {
            String sysPurchaseMinAmount = configService.get(SYSTEM_PURCHASE_MIN_AMOUNT);
            productDO.setLimitPurchaseQuota(BigDecimal.valueOf(Double.parseDouble(sysPurchaseMinAmount)));
        }

        if (ObjectUtil.isNull(productDO.getId())) {
            productDO.setCreateTime(LocalDateTime.now());
            productDO.setId(CommonFunction.generalId());
            productDO.setUseQuota(BigDecimal.ZERO);
            super.saveOrUpdate(productDO);
            return;
        }

        if (Objects.nonNull(productDO.getId())){
            FinancialProduct product = super.getById(productDO.getId());
            if (ProductStatus.open.equals(product.getStatus())) {
                ErrorCodeEnum.PRODUCT_CAN_NOT_EDIT.throwException();
            }
            // 如果年化利率修改，需要更新持有记录表
            if (!product.getRate().equals(productDO.getRate())) {
                financialRecordService.updateRateByProductId(product.getId(), productDO.getRate());
            }

            product.setUpdateTime(LocalDateTime.now());
            super.saveOrUpdate(productDO);
        }

        if(productDO.getRateType() == 1){
            financialProductLadderRateService.insert(productDO.getId(),financialProductQuery.getLadderRates());
        }
    }

    /**
     * 修改产品状态
     */
    @Transactional
    public void editProductStatus(FinancialProductEditStatusQuery query) {
        try {

            FinancialProduct product = financialProductMapper.selectById(query.getProductId());
            product = Optional.ofNullable(product).orElseThrow(ErrorCodeEnum.ARGUEMENT_ERROR::generalException);

            if (ProductStatus.close.equals(query.getStatus())) {
                redisLock.lock(RedisLockConstants.PRODUCT_CLOSE_LOCK_PREFIX + query.getProductId(), 5L, TimeUnit.SECONDS);
            }

            product.setUpdateTime(LocalDateTime.now());
            product.setStatus(query.getStatus());
            if (financialProductMapper.updateById(product) <= 0) {
                ErrorCodeEnum.SYSTEM_ERROR.throwException();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            redisLock.unlock(RedisLockConstants.PRODUCT_CLOSE_LOCK_PREFIX + query.getProductId());
        }

    }

    /**
     * 查询产品列表数据
     */
    public IPage<MFinancialProductVO> mSelectListByQuery(IPage<FinancialProduct> page, FinancialProductsQuery query) {
        LambdaQueryWrapper<FinancialProduct> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper = queryWrapper.eq(FinancialProduct::isDeleted, false);
        if (StringUtils.isNotBlank(query.getName())) {
            queryWrapper = queryWrapper.like(FinancialProduct::getName, query.getName());
        }
        if (Objects.nonNull(query.getType())) {
            queryWrapper = queryWrapper.eq(FinancialProduct::getType, query.getType());
        }
        if (Objects.nonNull(query.getStatus())) {
            queryWrapper = queryWrapper.eq(FinancialProduct::getStatus, query.getStatus());
        }

        if (Objects.nonNull(query.getCoin())) {
            queryWrapper = queryWrapper.eq(FinancialProduct::getCoin, query.getCoin());
        }

        queryWrapper = queryWrapper.orderByDesc(FinancialProduct::getCreateTime);

        IPage<FinancialProduct> financialProductIPage = financialProductMapper.selectPage(page, queryWrapper);

        List<Long> productIds = financialProductIPage.getRecords().stream().map(FinancialProduct::getId).collect(Collectors.toList());
        var productSummaryDataDtoMap = financialRecordService.getProductSummaryDataDtoMap(productIds);
        return financialProductIPage.convert(index -> {
            var mFinancialProductVO = managementConverter.toMFinancialProductVO(index);
            var productSummaryDataDto = productSummaryDataDtoMap.getOrDefault(index.getId(), new ProductSummaryDataDto());
            mFinancialProductVO.setUseQuota(Optional.ofNullable(productSummaryDataDto.getUseQuota()).orElse(BigDecimal.ZERO));
            mFinancialProductVO.setHoldUserCount(Optional.ofNullable(productSummaryDataDto.getHoldUserCount()).orElse(BigInteger.ZERO));
            return mFinancialProductVO;
        });
    }

    public List<ProductRateDTO> listProductRateDTO() {
        return financialProductMapper.listProductRateDTO();
    }

    /**
     * 增加额度
     */
    @Transactional
    public void increaseUseQuota(Long productId, BigDecimal increaseAmount, BigDecimal expectAmount) {
        int i = financialProductMapper.increaseUseQuota(productId, increaseAmount, expectAmount);
        if (i <= 0) {
            ErrorCodeEnum.throwException("申购额度发生变化，请重新申购");
        }
    }
}
