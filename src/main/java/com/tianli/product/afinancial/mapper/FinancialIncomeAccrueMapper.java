package com.tianli.product.afinancial.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.product.afinancial.dto.FinancialIncomeAccrueDTO;
import com.tianli.product.afinancial.entity.FinancialIncomeAccrue;
import com.tianli.product.afinancial.enums.ProductType;
import com.tianli.management.dto.AmountDto;
import com.tianli.management.query.FinancialProductIncomeQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface FinancialIncomeAccrueMapper extends BaseMapper<FinancialIncomeAccrue> {

    List<FinancialIncomeAccrueDTO> listByUidAndType(@Param("uid") Long uid, @Param("type") ProductType type);

    IPage<FinancialIncomeAccrueDTO> pageByQuery(@Param("page") Page<FinancialIncomeAccrueDTO> page
            , @Param("query") FinancialProductIncomeQuery query);

    List<AmountDto> summaryIncomeByQuery(@Param("query") FinancialProductIncomeQuery query);

    @Select("SELECT income_amount as amount,coin FROM financial_income_accrue WHERE update_time <= #{endTime}")
    List<AmountDto> getAmountSum(@Param("endTime") LocalDateTime endTime);
}
