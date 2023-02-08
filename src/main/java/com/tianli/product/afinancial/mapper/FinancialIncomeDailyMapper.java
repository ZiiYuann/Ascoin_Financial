package com.tianli.product.afinancial.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.product.afinancial.dto.FinancialIncomeDailyDTO;
import com.tianli.product.afinancial.entity.FinancialIncomeDaily;
import com.tianli.product.afinancial.enums.ProductType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-13
 **/
@Mapper
public interface FinancialIncomeDailyMapper extends BaseMapper<FinancialIncomeDaily> {

    List<FinancialIncomeDailyDTO> listByUidAndType(@Param("uid") Long uid,
                                                   @Param("type") ProductType type,
                                                   @Param("recordId") Long recordId,
                                                   @Param("yesterdayZero") LocalDateTime yesterdayZero,
                                                   @Param("todayZero") LocalDateTime todayZero);
}
