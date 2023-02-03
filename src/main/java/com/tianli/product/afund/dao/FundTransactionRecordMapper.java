package com.tianli.product.afund.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.product.afund.dto.FundTransactionAmountDTO;
import com.tianli.product.afund.entity.FundTransactionRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.product.afund.query.FundTransactionQuery;
import com.tianli.product.afund.vo.FundTransactionRecordVO;
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
    List<FundTransactionAmountDTO> selectTransactionAmount(@Param("query") FundTransactionQuery query);

    IPage<FundTransactionRecordVO> selectTransactionPage(@Param("page") IPage<FundTransactionRecord> page,
                                                         @Param("query") FundTransactionQuery query);

    Integer selectWaitRedemptionCount(@Param("agentId") Long agentId);
}
