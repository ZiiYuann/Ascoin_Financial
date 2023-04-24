package com.tianli.management.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @autoor xianeng
 * @data 2023/4/24 10:14
 */
@Data
public class CoinBaseVO {

    private String name;

    private String logo;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private boolean mainToken;

    private boolean display;

    /**
     * assureId转账配置
     */
    private int withdrawDecimals;

    private BigDecimal withdrawMin;
}
