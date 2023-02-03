package com.tianli.product.afinancial.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.product.afinancial.entity.FinancialProduct;
import com.tianli.product.afinancial.query.ProductHoldQuery;
import com.tianli.product.afinancial.vo.HoldProductVo;
import com.tianli.product.afinancial.vo.TransactionRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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
                                         @Param("query") ProductHoldQuery query);

    /**
     * 获取用户持用产品信息
     *
     * @param uid 用户id
     * @return 持有列表page
     */
    List<HoldProductVo> holdProducts(@Param("uid") Long uid,
                                     @Param("productIds") List<Long> productIds);

    /**
     * 获取用户持用id信息
     *
     * @param uid 用户id
     * @return 持有列表page
     */
    IPage<Long> holdProductIds(@Param("page") IPage<FinancialProduct> page,
                               @Param("uid") Long uid,
                               @Param("type") String type);

    /**
     * 获取用户交易记录信息
     *
     * @param uid 用户id
     * @return 持有列表page
     */
    IPage<TransactionRecordVO> transactionRecordPage(@Param("page") IPage<FinancialProduct> page,
                                                     @Param("uid") Long uid,
                                                     @Param("type") String type);
}
