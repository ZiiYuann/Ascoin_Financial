package com.tianli.product.afund.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.product.afund.dto.FundIncomeAmountDTO;
import com.tianli.product.afund.entity.FundIncomeRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.product.afund.query.FundIncomeQuery;
import com.tianli.product.afund.vo.FundIncomeRecordVO;
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

    IPage<FundIncomeRecordVO> selectIncomePage(@Param("page") IPage<FundIncomeRecord> page, @Param("query") FundIncomeQuery query);

    IPage<FundIncomeRecordVO> selectSummaryIncomePage(@Param("page") IPage<FundIncomeRecord> page,@Param("query") FundIncomeQuery query);

    Integer selectWaitInterestCount(Long agentId);
}
