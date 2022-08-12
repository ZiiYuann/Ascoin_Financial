package com.tianli.borrow.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import java.time.LocalDate;
import java.io.Serializable;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 计息中订单每日统计
 * </p>
 *
 * @author xianeng
 * @since 2022-08-01
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = false)
public class BorrowOrderNumDaily extends Model<BorrowOrderNumDaily> {

    private static final long serialVersionUID=1L;

    /**
     * ID
     */
    private Long id;

    /**
     * 订单数
     */
    private Integer orderNum;

    /**
     * 统计日期
     */
    private LocalDate statisticalDate;


    @Override
    protected Serializable pkVal() {
        return null;
    }

}
