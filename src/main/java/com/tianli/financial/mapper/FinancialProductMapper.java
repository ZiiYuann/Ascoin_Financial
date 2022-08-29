package com.tianli.financial.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.financial.dto.ProductRateDTO;
import com.tianli.financial.entity.FinancialProduct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface FinancialProductMapper extends BaseMapper<FinancialProduct> {

    @Update("UPDATE  financial_product SET deleted = true ,update_time = now() WHERE id = #{productId}")
    int deleteById(@Param("productId") Long productId);


    @Select("SELECT max( rate ) AS max_rate,min( rate ) AS min_rate,coin,count( 1 ) AS product_count,id FROM financial_product WHERE `status` = 'open' GROUP BY coin order by max_rate desc ")
    List<ProductRateDTO> listProductRateDTO();
}
