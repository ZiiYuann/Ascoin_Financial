package com.tianli.wallet.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.account.entity.AccountActive;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-06
 **/
@Mapper
public interface AccountActiveMapper extends BaseMapper<AccountActive>{

    @Select("SELECT * FROM wallet_active WHERE uid = #{uid}")
    AccountActive selectByUid(@Param("uid") Long uid);
}
