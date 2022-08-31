package com.tianli.fund.dao;

import com.tianli.fund.entity.FundRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.management.dto.AmountDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 基金持有记录 Mapper 接口
 * </p>
 *
 * @author xianeng
 * @since 2022-08-30
 */
@Mapper
public interface FundRecordMapper extends BaseMapper<FundRecord> {
    @Select("select sum(hold_amount) amount,coin from fund_record where uid = #{uid} group by coin")
    List<AmountDto> holdAmountSumByUid(Long uid);
}
