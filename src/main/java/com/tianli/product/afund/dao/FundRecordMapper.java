package com.tianli.product.afund.dao;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tianli.product.afund.entity.FundRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.product.afund.query.FundRecordQuery;
import com.tianli.management.dto.AmountDto;
import com.tianli.management.dto.FundUserHoldDto;
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

    @Update("update fund_record set hold_amount = hold_amount - #{amount} where id = #{id}")
    int reduceAmount(Long id, BigDecimal amount);

    @Update("update fund_record set hold_amount = hold_amount + #{amount},status='PROCESS' where id = #{id}")
    int increaseAmount(Long id, BigDecimal amount);

    IPage<FundUserRecordVO> selectDistinctUidPage(@Param("page") IPage<FundRecord> page, @Param("query") FundRecordQuery query);

    List<AmountDto> selectHoldAmount(@Param("query") FundRecordQuery query);

    BigDecimal selectHoldAmountSum(@Param("productId") Long productId, @Param("uid") Long uid);

    Integer selectHoldUserCount(@Param("query") FundRecordQuery query);

    List<FundUserHoldDto> selectFundUserHoldDto(FundRecordQuery query);

    int updateRateByProductId(@Param("productId") Long productId,
                              @Param("rate") BigDecimal rate);


    @Select("SELECT `product_id` FROM fund_record WHERE `status` = 'PROCESS' ")
    List<Long> holdProductIds(@Param("uid") Long uid);
}
