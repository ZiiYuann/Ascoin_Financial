package com.tianli.product.aborrow.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * @author chenb
 * @apiNote
 * @since 2023-02-14
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowInterest {

    @TableId
    private Long id;

    private Long bid;

    private Long uid;

    private String coin;

    private BigDecimal amount;

}
