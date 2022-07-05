package com.tianli.management.financial.dto;

import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author lzy
 * @date 2022/4/1 3:53 下午
 */
@Data
public class FinancialProductEditDto {


    private Long id;

    /**
     * 图片
     */
    @NotBlank(message = "请上传图片!")
    private String logo;

    /**
     * 产品名称
     */
    @NotBlank(message = "理财类型不能为空")
    private String name;

    /**
     * 英文产品名称
     */
    private String name_en;

    /**
     * 日利率
     */
    @NotNull(message = "日利率不能为空")
    @DecimalMin(value = "0.000000001", message = "日利率填写有误")
    private double rate;

    /**
     * 锁仓天数
     */
    @NotNull(message = "锁仓天数不能为空")
    @Min(value = 0, message = "锁仓天数不能小于0")
    private Long period;

    /**
     * 累计投资
     */
    @DecimalMin(value = "0", message = "质押量不能小于0")
    private double all_invest;

    /**
     * 描述
     */
    @NotBlank(message = "文案描述不能为空")
    private String description;

    /**
     * 英文描述
     */
    private String description_en;

    /**
     * status
     */
    private String status;
}
