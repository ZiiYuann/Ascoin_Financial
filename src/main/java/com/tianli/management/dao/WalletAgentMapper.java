package com.tianli.management.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.common.PageQuery;
import com.tianli.fund.enums.FundTransactionType;
import com.tianli.management.dto.AmountDto;
import com.tianli.management.entity.WalletAgent;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.management.query.WalletAgentQuery;
import com.tianli.management.vo.WalletAgentVO;
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

    IPage<WalletAgentVO> selectPageByQuery(@Param("page") IPage<WalletAgentVO> page,@Param("query") WalletAgentQuery query);
    @Select("SELECT ifnull( SUM( a.hold_amount ), 0 ) amount,a.coin FROM fund_record a LEFT JOIN wallet_agent_product b " +
            "ON a.product_id = b.product_id WHERE b.uid = #{agentUId} GROUP BY a.coin")
    List<AmountDto> holdAmountSum(Long agentUId);

    @Select("SELECT ifnull( SUM( c.transaction_amount ), 0 ) amount, a.coin FROM fund_record a " +
            "LEFT JOIN wallet_agent_product b ON a.product_id = b.product_id " +
            "left JOIN fund_transaction_record c on a.id = c.fund_id " +
            "WHERE b.uid = #{agentUId} and c.type = #{type} and c.status = #{status} " +
            "GROUP BY a.coin")
    List<AmountDto> redemptionAmountSum(@Param("agentUId") Long agentUId, @Param("type") FundTransactionType type, @Param("status") Integer status);

    @Select("SELECT ifnull( SUM( c.interest_amount ), 0 ) amount, a.coin FROM fund_record a " +
            "LEFT JOIN wallet_agent_product b ON a.product_id = b.product_id " +
            "left JOIN fund_income_record c on a.id = c.fund_id\n" +
            "WHERE a.uid = #{agentUId} and c.status = #{status} GROUP BY a.coin")
    List<AmountDto> interestAmountSum(@Param("agentUId")Long agentUId,@Param("status")Integer status);
}
