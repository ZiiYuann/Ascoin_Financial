package com.tianli.financial.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.financial.entity.FinancialProduct;
import com.tianli.financial.enums.ProductType;
import com.tianli.financial.vo.HoldProductVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-20
 **/
@Mapper
public interface ProductMapper {


    /**
     * 获取用户持用产品信息
     *
     * @param uid 用户id
     * @return 持有列表page
     */
    IPage<HoldProductVo> holdProductPage(@Param("page") IPage<FinancialProduct> page,
                                         @Param("uid") Long uid,
                                         @Param("type") String type);
}
