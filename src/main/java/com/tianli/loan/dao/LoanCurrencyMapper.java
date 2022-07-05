package com.tianli.loan.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tianli.loan.entity.LoanCurrency;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;

/**
 * <p>
 * 用户贷款余额表 Mapper 接口
 * </p>
 *
 * @author lzy
 * @since 2022-05-26
 */
@Mapper
public interface LoanCurrencyMapper extends BaseMapper<LoanCurrency> {

    @Update("update loan_currency set balance = balance + #{actualAmount} where uid = #{uid} and token = #{token}")
    int increase(@Param("actualAmount") BigDecimal actualAmount, @Param("uid") Long uid, @Param("token") String token);

    @Update("update loan_currency set balance = balance - #{money} where uid = #{uid} and token = #{token} and balance >= #{money}")
    int reduce(Long uid, BigDecimal money, String token);
}
