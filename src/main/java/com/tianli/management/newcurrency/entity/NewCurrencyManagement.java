package com.tianli.management.newcurrency.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.tianli.currency_token.token.mapper.TokenList;
import com.tianli.currency_token.transfer.mapper.TokenContract;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
public class NewCurrencyManagement extends Model<NewCurrencyManagement> {

    private static final long serialVersionUID=1L;

    @TableId(value = "id", type = IdType.INPUT)
    private Long id;

    //@ApiModelProperty(value = "币种名称")
    private String currency_name;
    //@ApiModelProperty(value = "币种简称")
    @NotBlank(message = "请输入新币简称")
    private String currency_name_short;

    //@ApiModelProperty(value = "合约地址")
    private String contract_address;

    //@ApiModelProperty(value = "代币发行总量")
    private BigDecimal amount;

    //@ApiModelProperty(value = "初始流通量")
    private BigDecimal amount_pecsent;

    //@ApiModelProperty(value = "发售价格")
    private BigDecimal sale_price;

    //@ApiModelProperty(value = "交易初始价格")
    private BigDecimal trade_start_price;

    //@ApiModelProperty(value = "个人硬顶价格（可获得新币的上限)")
    private BigDecimal self_max_currency;

    //@ApiModelProperty(value = "单笔起投额")
    private BigDecimal amount_min;

    //@ApiModelProperty(value = "代币种类")
    private String token;

    //@ApiModelProperty(value = "持仓统计结束时间")
    private String time1;

    //@ApiModelProperty(value = "投入结束时间")
    private String time2;

    //@ApiModelProperty(value = "统计结束时间")
    private String time3;

    //@ApiModelProperty(value = "分发时间")
    private String time4;

    //@ApiModelProperty(value = "交易对上线时间")
    private String time5;

    //@ApiModelProperty(value = "项目描述")
    private String project_zh_describe;

    //@ApiModelProperty(value = "项目简介")
    private String project_zh_introduce;

    //@ApiModelProperty(value = "项目亮点")
    private String project_zh_bright;

    //@ApiModelProperty(value = "项目描述")
    private String project_en_describe;

    //@ApiModelProperty(value = "项目简介")
    private String project_en_introduce;

    //@ApiModelProperty(value = "项目亮点")
    private String project_en_bright;

    //@ApiModelProperty(value = "图片地址")
    private String pic_address;

    //@ApiModelProperty(value = "创建时间")
    private String create_time;

    private String material;//材料
    private String community;//社区
    private Integer is_delete;//删除标志

    /**
     * 是否已经上线处理
     */
    private Boolean online_processing;

    @TableField(exist = false)
    private String type;
    @TableField(exist = false)
    private String sum;//投入总数
    @TableField(exist = false)
    private String personNumber;//参与人数

    @TableField(exist = false)
    private String timestampTime1;
    @TableField(exist = false)
    private String timestampTime2;
    @TableField(exist = false)
    private String timestampTime3;
    @TableField(exist = false)
    private String timestampTime4;
    @TableField(exist = false)
    private String timestampTime5;
    @TableField(exist = false)
    private String timestampCreateTime;

    @TableField(exist = false)
    private TokenList tokenList;
    @TableField(exist = false)
    private List<TokenContract> tokenContract;

    public void transferTime(){
        this.timestampTime1=toPochMilli(this.getTime1());
        this.timestampTime2=toPochMilli(this.getTime2());
        this.timestampTime3=toPochMilli(this.getTime3());
        this.timestampTime4=toPochMilli(this.getTime4());
        this.timestampTime5=toPochMilli(this.getTime5());
        this.timestampCreateTime=toPochMilli(this.getCreate_time());
    }

    public String toPochMilli(String time){
        if(time=="" ||time==null){
            return "";
        }
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.parse(time, df);
        ZoneId zone = ZoneId.systemDefault();
        long timestamp = localDateTime.atZone(zone).toInstant().toEpochMilli();
        return String.valueOf(timestamp);
    }
    @Override
    protected Serializable pkVal() {
        return this.id;
    }

}
