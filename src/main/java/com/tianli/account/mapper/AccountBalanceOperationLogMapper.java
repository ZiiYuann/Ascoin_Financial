package com.tianli.account.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.account.entity.AccountBalanceOperationLog;
import com.tianli.account.query.AccountDetailsNewQuery;
import com.tianli.account.vo.WalletChargeFlowVo;
import com.tianli.management.query.WalletChargeFlowQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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

    IPage<WalletChargeFlowVo> list(@Param("page") IPage<AccountBalanceOperationLog> logIPage,
                                   @Param("param") WalletChargeFlowQuery walletChargeFlowQuery);


    Page<AccountBalanceOperationLog> pageList(@Param("page") IPage<AccountBalanceOperationLog> page,
                                              @Param("uid") Long uid,
                                              @Param("param") AccountDetailsNewQuery query);
}
