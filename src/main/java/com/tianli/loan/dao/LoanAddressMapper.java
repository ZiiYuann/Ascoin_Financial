package com.tianli.loan.dao;

import com.tianli.loan.entity.LoanAddress;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 用户还款地址表 Mapper 接口
 * </p>
 *
 * @author lzy
 * @since 2022-05-31
 */
@Mapper
public interface LoanAddressMapper extends BaseMapper<LoanAddress> {

}
