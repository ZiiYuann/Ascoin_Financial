package com.tianli.financial.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.financial.controller.UserFinancialPage;
import com.tianli.financial.entity.FinancialLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FinancialLogMapper extends BaseMapper<FinancialLog> {

    @Select("select a.id, b.logo, b.period, a.`start_date`, a.`end_date`, a.`rate`, a.`amount`, a.`status`, b.`name`, b.`name_en`, b.`type` from `user_financial_log` a inner join `financial_product` b on a.`financial_product_id` = b.`id` where a.`user_id` = #{uid}")
    List<UserFinancialPage> getUserFinancialPage(Long uid);

}
