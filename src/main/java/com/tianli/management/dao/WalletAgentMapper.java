package com.tianli.management.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.management.dto.AmountDto;
import com.tianli.management.entity.WalletAgent;
import com.tianli.management.query.WalletAgentQuery;
import com.tianli.management.vo.WalletAgentVO;
import com.tianli.fund.enums.FundTransactionType;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;

import java.util.List;

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

    IPage<WalletAgentVO> selectPageByQuery(@Param("page") IPage<WalletAgentVO> page, @Param("query") WalletAgentQuery query);

    @Select("SELECT ifnull( SUM( a.hold_amount ), 0 ) amount,a.coin FROM fund_record a LEFT JOIN wallet_agent_product b " +
            "ON a.product_id = b.product_id WHERE b.agent_id = #{agentId} GROUP BY a.coin")
    List<AmountDto> holdAmountSum(Long agentId);

    @Select("SELECT ifnull( SUM( c.transaction_amount ), 0 ) amount, a.coin FROM fund_record a " +
            "LEFT JOIN wallet_agent_product b ON a.product_id = b.product_id " +
            "left JOIN fund_transaction_record c on a.id = c.fund_id " +
            "WHERE b.agent_id = #{agentId} and c.type = #{type} and c.status = #{status} " +
            "GROUP BY a.coin")
    List<AmountDto> redemptionAmountSum(@Param("agentId") Long agentId, @Param("type") FundTransactionType type, @Param("status") Integer status);

    List<AmountDto> interestAmountSum(@Param("agentId") Long agentId, @Param("status") List<Integer> status);
}
