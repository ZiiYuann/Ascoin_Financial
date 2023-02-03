package com.tianli.product.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.product.entity.ProductHoldRecord;
import com.tianli.product.afinancial.query.ProductHoldQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-02
 **/
@Mapper
public interface ProductHoldRecordMapper extends BaseMapper<ProductHoldRecord> {

    IPage<Long> holdUidPage(@Param("page") Page<ProductHoldRecord> page,
                            @Param("query") ProductHoldQuery query);

    List<Long> holdUids(@Param("query") ProductHoldQuery query);

    @Select("SELECT * FROM `product_hold_record` WHERE `uid` = #{uid}")
    List<ProductHoldRecord> listByUid(@Param("uid") Long uid);
}
