package com.tianli.fund.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.fund.dto.FundIncomeAmountDTO;
import com.tianli.fund.entity.FundIncomeRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.fund.query.FundIncomeQuery;
import com.tianli.fund.vo.FundIncomeRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 基金收益记录 Mapper 接口
 * </p>
 *
 * @author xianeng
 * @since 2022-08-30
 */
@Mapper
public interface FundIncomeRecordMapper extends BaseMapper<FundIncomeRecord> {

    List<FundIncomeAmountDTO> selectAmount(@Param("query") FundIncomeQuery query);

    IPage<FundIncomeRecordVO> selectIncomePage(@Param("page") IPage<FundIncomeRecord> page,@Param("query") FundIncomeQuery query);

    Integer selectWaitInterestCount(Long agentId);
}
