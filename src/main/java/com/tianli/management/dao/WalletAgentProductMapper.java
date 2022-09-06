package com.tianli.management.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.agent.management.vo.FundProductStatisticsVO;
import com.tianli.management.entity.WalletAgentProduct;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * 云钱包代理人和产品关联 Mapper 接口
 * </p>
 *
 * @author xianeng
 * @since 2022-08-30
 */
@Mapper
public interface WalletAgentProductMapper extends BaseMapper<WalletAgentProduct> {

    @Select("select count(*) from wallet_agent_product where product_id = #{productId}" )
    Integer selectCountByProjectId(Long productId);

    @Delete("delete from wallet_agent_product where agent_id = #{agentId}")
    void deleteByAgentId(Long agentId);

    @Delete("delete from wallet_agent_product where product_id = #{productId}")
    void deleteByProductId(Long productId);

    IPage<FundProductStatisticsVO> selectPage(@Param("page") IPage<WalletAgentProduct> pageQuery,@Param("agentId") Long agentId);

}
