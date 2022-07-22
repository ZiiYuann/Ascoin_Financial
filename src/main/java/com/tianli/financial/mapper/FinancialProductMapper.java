package com.tianli.financial.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.financial.entity.FinancialProduct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface FinancialProductMapper extends BaseMapper<FinancialProduct> {

    @Update("UPDATE  financial_product SET deleted = true ,update_time = now() WHERE id = #{productId}")
    int deleteById(@Param("productId") Long productId);
}
