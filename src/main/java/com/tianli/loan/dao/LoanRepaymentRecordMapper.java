package com.tianli.loan.dao;

import com.tianli.loan.entity.LoanRepaymentRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 贷款-还款记录表 Mapper 接口
 * </p>
 *
 * @author lzy
 * @since 2022-06-06
 */
@Mapper
public interface LoanRepaymentRecordMapper extends BaseMapper<LoanRepaymentRecord> {

}
