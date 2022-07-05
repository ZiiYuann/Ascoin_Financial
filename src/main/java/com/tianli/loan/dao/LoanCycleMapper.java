package com.tianli.loan.dao;

import com.tianli.loan.entity.LoanCycle;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 贷款周期表 Mapper 接口
 * </p>
 *
 * @author lzy
 * @since 2022-05-26
 */
@Mapper
public interface LoanCycleMapper extends BaseMapper<LoanCycle> {

}
