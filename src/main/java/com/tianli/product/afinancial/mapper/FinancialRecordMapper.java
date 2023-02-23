package com.tianli.product.afinancial.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.product.afinancial.entity.FinancialRecord;
import com.tianli.product.afinancial.query.FinancialRecordQuery;
import com.tianli.management.dto.AmountDto;
import com.tianli.management.dto.ProductSummaryDataDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@Mapper
public interface FinancialRecordMapper extends BaseMapper<FinancialRecord> {

    /**
     * 减少持有的金额
     */
    int reduce(@Param("recordId") Long recordId,
               @Param("amount") BigDecimal amount,
               @Param("originalHoldAmount") BigDecimal originalHoldAmount);

    /**
     * 减少持有的金额
     */
    int reduce2(@Param("recordId") Long recordId,
                @Param("incomeAmount") BigDecimal incomeAmount,
                @Param("waitAmount") BigDecimal waitAmount,
                @Param("originalHoldAmount") BigDecimal originalHoldAmount);

    /**
     * 增加持有金额
     */
    @Deprecated
    int increase(@Param("recordId") Long recordId,
                 @Param("amount") BigDecimal amount,
                 @Param("originalAmount") BigDecimal originalAmount);

    int increaseWaitAmount(@Param("recordId") Long recordId,
                           @Param("amount") BigDecimal amount,
                           @Param("originalAmount") BigDecimal originalAmount);

    int increaseIncomeAmount(@Param("recordId") Long recordId,
                             @Param("amount") BigDecimal amount,
                             @Param("originalAmount") BigDecimal originalAmount);

    int updateRateByProductId(@Param("productId") Long productId,
                              @Param("rate") BigDecimal rate);

    @Update("UPDATE financial_record SET  pledge = #{pledge} WHERE uid = #{uid} AND id =#{id} and hold_amount = #{originalAmount}")
    int updatePledge(@Param("uid") Long uid,
                     @Param("id") Long id,
                     @Param("originalAmount") BigDecimal originalAmount,
                     @Param("pledge") boolean pledge);

    @Update("UPDATE financial_record SET pledge = #{pledge},hold_amount= hold_amount -#{reduceAmount} WHERE uid = #{uid} AND id =#{id} and hold_amount = #{originalAmount}")
    int updatePledgeAndReduce(@Param("uid") Long uid,
                              @Param("id") Long id,
                              @Param("originalAmount") BigDecimal originalAmount,
                              @Param("pledge") boolean pledge,
                              @Param("reduceAmount") BigDecimal reduceAmount);

    /**
     * 用户还持用产品的数量
     */
    List<AmountDto> holdAmount(FinancialRecordQuery query);

    /**
     * 还持有产品的用户数量
     */
    @Select("SELECT count(DISTINCT uid) FROM financial_record WHERE status = 'PROCESS'")
    BigInteger countUid();

    List<ProductSummaryDataDto> listProductSummaryDataDto(@Param("productIds") List<Long> productIds);

    List<Map<String, Object>> firstProcessRecordMap(@Param("productIds") List<Long> productIds, @Param("uid") Long uid);

    @Select("SELECT `product_id` FROM financial_record WHERE `status` = 'PROCESS' ")
    List<Long> holdProductIds(@Param("uid") Long uid);
}
