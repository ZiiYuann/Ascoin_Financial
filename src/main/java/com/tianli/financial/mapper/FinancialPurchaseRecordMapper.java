package com.tianli.financial.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.financial.controller.UserFinancialPage;
import com.tianli.financial.entity.FinancialPurchaseRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FinancialPurchaseRecordMapper extends BaseMapper<FinancialPurchaseRecord> {

}
