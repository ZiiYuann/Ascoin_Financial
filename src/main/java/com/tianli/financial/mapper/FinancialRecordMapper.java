package com.tianli.financial.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.financial.entity.FinancialRecord;
import com.tianli.financial.enums.ProductType;
import com.tianli.management.dto.AmountDto;
import com.tianli.management.dto.ProductSummaryDataDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface FinancialRecordMapper extends BaseMapper<FinancialRecord> {

    int reduce(@Param("recordId") Long recordId, @Param("amount") BigDecimal amount,@Param("now") LocalDateTime now);

    @Select("SELECT ifnull(SUM(hold_amount),0.0) FROM financial_record")
    BigDecimal selectTotalHoldAmount();

    /**
     * 用户还持用产品的数量
     */
    List<AmountDto> countProcess(@Param("productType") ProductType productType);

    /**
     * 还持有产品的用户数量
     */
    @Select("SELECT count(DISTINCT uid) FROM financial_record WHERE status = 'PROCESS'")
    BigInteger countUid();

    List<ProductSummaryDataDto> listProductSummaryDataDto(List<Long> productIds);
}
