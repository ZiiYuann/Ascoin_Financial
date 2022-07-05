package com.tianli.management.financial.vo;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.convert.Convert;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.financial.mapper.FinancialProductType;
import com.tianli.financial.mapper.UserFinancialLogStatus;
import com.tianli.management.financial.dto.FinancialUserRecordListDto;
import lombok.Data;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author lzy
 * @date 2022/4/1 7:51 下午
 */
@Data
public class FinancialUserRecordListVo {

    /**
     * id
     */
    private String id;
    /**
     * 邮箱
     */
    private String username;
    /**
     * 昵称
     */
    private String nick;
    /**
     * 理财类型
     */
    private String financialProductName;
    /**
     * 存入金额
     */
    private double amount;
    /**
     * 存入时间
     */
    private LocalDateTime depositDate;
    /**
     * 赎回时间
     */
    private LocalDateTime finish_time;
    /**
     * 存入天数
     */
    private Long depositDays;
    /**
     * 剩余天数
     */
    private Long remainingDays;
    /**
     * 可赎回金额
     */
    private double redeemableAmount;
    /**
     * 盈利
     */
    private double profitAmount;


    public static FinancialUserRecordListVo getFinancialUserRecordListVo(FinancialUserRecordListDto financialUserRecordListDto) {
        long nowDay = LocalDate.now().toEpochDay();
        FinancialUserRecordListVo financialUserRecordListVo = BeanUtil.copyProperties(financialUserRecordListDto, FinancialUserRecordListVo.class, "amount");
        LocalDate startDate = financialUserRecordListDto.getStart_date();
        long depositDays = Math.max(nowDay - startDate.toEpochDay(), 0L);
        //存入天数
        financialUserRecordListVo.setDepositDays(depositDays);
        financialUserRecordListVo.setAmount(TokenCurrencyType.usdt_omni.money(financialUserRecordListDto.getAmount()));
        if (financialUserRecordListDto.getFinancial_product_type().equals(FinancialProductType.fixed.name()) && UserFinancialLogStatus.created.name().equals(financialUserRecordListDto.getStatus())) {
            long remainingDays = Math.max(financialUserRecordListDto.getEnd_date().toEpochDay() - nowDay, 0L);
            //剩余天数
            financialUserRecordListVo.setRemainingDays(remainingDays);
        }
        BigDecimal decimal = Convert.toBigDecimal(financialUserRecordListDto.getAmount());
        //盈利金额
        BigInteger profitAmount = decimal.multiply(Convert.toBigDecimal(financialUserRecordListDto.getRate())).multiply(Convert.toBigDecimal(depositDays)).toBigInteger();
        if (financialUserRecordListDto.getFinancial_product_type().equals(FinancialProductType.current.name()) && UserFinancialLogStatus.created.name().equals(financialUserRecordListDto.getStatus())) {
            //可赎回金额
            BigInteger redeemableAmount = financialUserRecordListDto.getAmount().add(profitAmount);
            financialUserRecordListVo.setRedeemableAmount(TokenCurrencyType.usdt_omni.money(redeemableAmount));
        }
        financialUserRecordListVo.setProfitAmount(TokenCurrencyType.usdt_omni.money(profitAmount));
        return financialUserRecordListVo;
    }


}
