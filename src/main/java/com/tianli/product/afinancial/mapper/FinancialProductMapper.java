package com.tianli.product.afinancial.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.product.afinancial.dto.ProductRateDTO;
import com.tianli.product.afinancial.entity.FinancialProduct;
import com.tianli.product.afinancial.enums.ProductType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

@Mapper
public interface FinancialProductMapper extends BaseMapper<FinancialProduct> {

    @Update("UPDATE  financial_product SET deleted = true ,update_time = now() WHERE id = #{productId}")
    int softDeleteById(@Param("productId") Long productId);

    @Update("UPDATE financial_product SET use_quota = use_quota + #{increaseAmount} WHERE id =#{productId} AND  use_quota =#{expectAmount}")
    int increaseUseQuota(@Param("productId") Long productId,
                         @Param("increaseAmount") BigDecimal increaseAmount,
                         @Param("expectAmount") BigDecimal expectAmount);

    @Update("UPDATE financial_product SET use_quota = use_quota - #{reduceAmount} WHERE id =#{productId}")
    int reduceUseQuota(@Param("productId") Long productId,
                       @Param("reduceAmount") BigDecimal reduceAmount);

    IPage<ProductRateDTO> listProductRateDTO(Page<FinancialProduct> page, @Param("productType") ProductType productType);

    @Update("UPDATE financial_product SET recommend = #{recommend} WHERE id = #{id}")
    int modifyRecommend(@Param("id") Long id, @Param("recommend") Boolean recommend);

    @Update("UPDATE financial_product SET recommend_weight = #{recommendWeight} WHERE id = #{id}")
    int modifyRecommendWeight(@Param("id") Long id, @Param("recommendWeight") Integer recommendWeight);
}
