package com.tianli.management.newcurrency.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 新币用户表
 * </p>
 *
 * @author cc
 * @since 2022-06-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class NewCurrencySumDTO extends Model<NewCurrencySumDTO> {

    private static final long serialVersionUID=1L;
    //@ApiModelProperty(value = "投入总额")
    private String sum;
    //@ApiModelProperty(value = "人数")
    private String personNumber;
    //新币名称
    private String currencyName;
    private String currencyId;

}
