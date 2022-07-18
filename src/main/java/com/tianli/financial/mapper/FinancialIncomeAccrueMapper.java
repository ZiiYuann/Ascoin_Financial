package com.tianli.financial.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tianli.financial.dto.FinancialIncomeAccrueDTO;
import com.tianli.financial.entity.FinancialIncomeAccrue;
import com.tianli.financial.enums.ProductType;
import com.tianli.management.query.FinancialProductIncomeQuery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FinancialIncomeAccrueMapper extends BaseMapper<FinancialIncomeAccrue> {

    List<FinancialIncomeAccrueDTO> listByUidAndType(@Param("uid") Long uid, @Param("type") ProductType type);

    IPage<FinancialIncomeAccrueDTO> pageByQuery(@Param("page") Page<FinancialIncomeAccrueDTO> page
            , @Param("query") FinancialProductIncomeQuery query);
}
