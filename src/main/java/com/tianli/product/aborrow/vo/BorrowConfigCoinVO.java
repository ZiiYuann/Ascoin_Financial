package com.tianli.product.aborrow.vo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-10
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowConfigCoinVO {

    private String coin;

    private String logo;

    private BigDecimal minAmount;

    private BigDecimal maxAmount;

    private BigDecimal hourRate;

}
