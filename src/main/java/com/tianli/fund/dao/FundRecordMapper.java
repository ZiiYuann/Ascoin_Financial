package com.tianli.fund.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.fund.entity.FundRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.fund.query.FundRecordQuery;
import com.tianli.management.dto.AmountDto;
import com.tianli.management.vo.FundUserRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
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

    @Update("update fund_record set hold_amount = hold_amount - #{amount} where id = #{id}")
    int reduceAmount(Long id, BigDecimal amount);

    @Update("update fund_record set hold_amount = hold_amount + #{amount} where id = #{id}")
    int increaseAmount(Long id, BigDecimal amount);

    IPage<FundUserRecordVO> selectDistinctUidPage(@Param("page") IPage<FundRecord> page,@Param("query") FundRecordQuery query);

    List<AmountDto> selectHoldAmount(FundRecordQuery query);

    BigDecimal selectHoldAmountSum(@Param("productId") Long productId,@Param("uid") Long uid);

    Integer selectHoldUserCount(FundRecordQuery query);
}
