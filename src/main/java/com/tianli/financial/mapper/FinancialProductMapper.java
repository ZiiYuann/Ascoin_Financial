package com.tianli.financial.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.financial.dto.ProductRateDTO;
import com.tianli.financial.entity.FinancialProduct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

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

    @Select("SELECT max( rate ) AS max_rate,min( rate ) AS min_rate,coin,count( 1 ) AS product_count,id FROM financial_product WHERE `status` = 'open'  GROUP BY coin order by max_rate desc ")
    IPage<ProductRateDTO> listProductRateDTO(Page<FinancialProduct> page);
}
