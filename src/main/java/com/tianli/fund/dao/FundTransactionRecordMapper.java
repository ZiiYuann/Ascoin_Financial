package com.tianli.fund.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.fund.dto.FundTransactionAmountDTO;
import com.tianli.fund.entity.FundTransactionRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.fund.query.FundTransactionQuery;
import com.tianli.fund.vo.FundTransactionRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 基金交易记录 Mapper 接口
 * </p>
 *
 * @author xianeng
 * @since 2022-08-30
 */
@Mapper
public interface FundTransactionRecordMapper extends BaseMapper<FundTransactionRecord> {
    List<FundTransactionAmountDTO> selectTransactionAmount(FundTransactionQuery query);

    IPage<FundTransactionRecordVO> selectTransactionPage(@Param("page") IPage<FundTransactionRecord> page,
                                                         @Param("query") FundTransactionQuery query);

    Integer selectWaitRedemptionCount(@Param("agentId")Long agentId);
}
