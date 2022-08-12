package com.tianli.borrow.dao;

import com.tianli.borrow.entity.BorrowCoinConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

/**
 * <p>
 * 借币数据配置 Mapper 接口
 * </p>
 *
 * @author xn
 * @since 2022-07-21
 */
@Mapper
public interface BorrowCoinConfigMapper extends BaseMapper<BorrowCoinConfig> {


}
