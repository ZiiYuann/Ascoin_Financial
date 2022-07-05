package com.tianli.management.newcurrency.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * <p>
 * 新币管理端
 * </p>
 *
 * @author cc
 * @since 2022-06-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
//@ApiModel(value="NewCurrencyManagement对象", description="新币管理端")
public class NewCurrencyManagementDTO extends Model<NewCurrencyManagementDTO> {

    private static final long serialVersionUID=1L;

    //@ApiModelProperty(value = "币种名称")
    private String currencyName;

    //@ApiModelProperty(value = "开始时间")
    private String startTime;
    //@ApiModelProperty(value = "结束时间")
    private String endTime;

}
