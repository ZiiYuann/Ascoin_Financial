package com.tianli.product.aborrow.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.product.aborrow.entity.BorrowOperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-10
 **/
@Mapper
public interface BorrowOperationLogMapper extends BaseMapper<BorrowOperationLog> {
}
