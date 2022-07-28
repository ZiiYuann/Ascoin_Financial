package com.tianli.borrow.dao;

import com.tianli.borrow.entity.BorrowPledgeCoinConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 质押币种配置 Mapper 接口
 * </p>
 *
 * @author xianeng
 * @since 2022-07-28
 */
@Mapper
public interface BorrowPledgeCoinConfigMapper extends BaseMapper<BorrowPledgeCoinConfig> {

    @Update("update borrow_pledge_coin_config set  is_del = 1 where id = #{id}")
    void loginDel(Long id);
}
