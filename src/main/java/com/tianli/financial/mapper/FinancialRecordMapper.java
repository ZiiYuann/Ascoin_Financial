package com.tianli.financial.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.financial.entity.FinancialRecord;
import com.tianli.financial.enums.ProductType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Mapper
public interface FinancialRecordMapper extends BaseMapper<FinancialRecord> {

    int reduce(@Param("recordId") Long recordId, @Param("amount") BigDecimal amount,@Param("now") LocalDateTime now);

    @Select("SELECT ifnull(SUM(hold_amount),0.0) FROM financial_record where product_type = #{productType}")
    BigDecimal selectTotalHoldAmount(@Param("productType") ProductType productType);

    @Select("SELECT ifnull(SUM(hold_amount),0.0) FROM financial_record where uid = #{uid} and  product_type = #{productType}")
    BigDecimal selectHoldAmountByUid(@Param("uid") Long uid,@Param("productType") ProductType productType);

    @Select("SELECT ifnull(SUM(hold_amount - pledge_amount),0.0) FROM financial_record where uid = #{uid} and coin = #{coin} and  product_type = #{productType}")
    BigDecimal selectAvailableAmountByUid(@Param("uid")Long uid,@Param("coin") CurrencyCoin coin, @Param("productType") ProductType productType);

    /**
     * 用户还持用产品的数量
     */
    @Select("SELECT COUNT(1) FROM financial_record WHERE product_type = #{productType} AND status = 'PROCESS' ")
    BigInteger countProcess(@Param("productType") ProductType productType);

    /**
     * 还持有产品的用户数量
     */
    @Select("SELECT count(DISTINCT uid) FROM financial_record WHERE status = 'PROCESS'")
    BigInteger countUid();
}
