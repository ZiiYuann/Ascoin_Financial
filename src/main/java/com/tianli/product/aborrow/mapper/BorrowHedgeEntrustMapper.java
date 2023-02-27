package com.tianli.product.aborrow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.product.aborrow.entity.BorrowHedgeEntrust;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-24
 **/
@Mapper
public interface BorrowHedgeEntrustMapper extends BaseMapper<BorrowHedgeEntrust> {

    @Update("UPDATE  `borrow_hedge_entrusdt` set `status` = 'CANCEL' WHERE `id` = #{id}")
    int cancel(@Param("id") Long id);
}
