package com.tianli.robot.dtos;

import com.tianli.robot.mapper.RobotCoupon;
import com.tianli.robot.mapper.RobotCouponAchievement;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
public class RobotCouponAchievementDTO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 创建时间
     */
    private LocalDateTime create_time;
    private Long create_time_ms;

    /**
     * 激活码
     */
    private String activation_code;

    /**
     * 交易对
     */
    private String symbol;
    private String symbol_name;

    /**
     * 总数量
     */
    private Double total_amount;

    /**
     * 自动押注的次数
     */
    private Integer auto_count;

    /**
     * 押注的金额
     */
    private Double auto_amount;

    /**
     * 胜率
     */
    private Double win_rate;

    /**
     * 利润率
     */
    private Double profit_rate;

    /**
     * 0 未使用  1 正在使用  2 已使用
     */
    private Integer status;

    /**
     * 已经使用的次数
     */
    private Integer used_count;

    /**
     * 使用时间
     */
    private LocalDateTime used_time;

    /**
     * 赢得次数
     */
    private Integer win_count;

    /**
     * 输的次数
     */
    private Integer lose_count;

    /**
     * 总的押注金额
     */
    private Double real_total_amount;

    /**
     * 盈利金额
     */
    private Double real_profit_amount;

    /**
     * 盈利率
     */
    private Double real_profit_rate;

    public static RobotCouponAchievementDTO convert(RobotCoupon robotCoupon, RobotCouponAchievement robotCouponAchievement) {
        LocalDateTime create_time = robotCoupon.getCreate_time();
        Instant create_instant = create_time.atZone(ZoneId.systemDefault()).toInstant();
        RobotCouponAchievementDTO dto = new RobotCouponAchievementDTO();
        dto.setId(robotCoupon.getId());
        dto.setCreate_time(robotCoupon.getCreate_time());
        dto.setCreate_time_ms(create_instant.toEpochMilli());
        dto.setActivation_code(robotCoupon.getActivation_code());
        dto.setSymbol(robotCouponAchievement.getSymbol());
        dto.setTotal_amount(robotCouponAchievement.getTotal_amount());
        dto.setAuto_count(robotCoupon.getAuto_count());
        dto.setAuto_amount(robotCoupon.getAuto_amount());
        dto.setWin_rate(robotCoupon.getWin_rate());
        dto.setProfit_rate(robotCoupon.getProfit_rate());
        dto.setStatus(robotCoupon.getStatus());
        dto.setUsed_count(robotCoupon.getUsed_count());
        dto.setUsed_time(robotCoupon.getUsed_time());
        dto.setWin_count(robotCouponAchievement.getWin_count());
        dto.setLose_count(robotCouponAchievement.getLose_count());
        dto.setReal_total_amount(robotCouponAchievement.getTotal_amount());
        dto.setReal_profit_amount(robotCouponAchievement.getProfit_amount());
        dto.setReal_profit_rate(robotCouponAchievement.getProfit_rate());
        return dto;
    }
}
