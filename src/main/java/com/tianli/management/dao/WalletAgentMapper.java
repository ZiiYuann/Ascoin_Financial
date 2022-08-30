package com.tianli.management.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.common.PageQuery;
import com.tianli.management.entity.WalletAgent;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.management.query.WalletAgentQuery;
import com.tianli.management.vo.WalletAgentVO;
import org.apache.ibatis.annotations.*;

/**
 * <p>
 * 云钱包代理人 Mapper 接口
 * </p>
 *
 * @author xianeng
 * @since 2022-08-30
 */
@Mapper
public interface WalletAgentMapper extends BaseMapper<WalletAgent> {

    @Select("select count(*) from wallet_agent where uid = #{uid} and deleted = 0")
    Integer selectCountByUid(Long uid);

    @Update("update wallet_agent set deleted = 1 where id = #{id}")
    int logicDelById(Long id);

    IPage<WalletAgentVO> selectPageByQuery(@Param("page") IPage<WalletAgentVO> page,@Param("query") WalletAgentQuery query);
}
