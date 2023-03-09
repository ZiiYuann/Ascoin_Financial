package com.tianli.account.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.account.entity.AccountBalanceOperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 余额变动记录表 Mapper 接口
 * </p>
 *
 * @author hd
 * @since 2020-12-04
 */
@Mapper
public interface AccountBalanceOperationLogMapper extends BaseMapper<AccountBalanceOperationLog> {

}
