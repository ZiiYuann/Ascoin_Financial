package com.tianli.borrow.dao;

import com.tianli.borrow.entity.BorrowCoinConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

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
    @Update("update borrow_coin_config set is_del = 1 where id = #{id}")
    void loginDel(Long id);

}
