package com.tianli.currency_token.mapper;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.tianli.currency.CurrencyTypeEnum;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * <p>
 * 用户加自选表
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class TokenFavorite extends Model<TokenFavorite> {
    /**
     * 主键
     */
    private Long id;

    /**
     * 用户id
     */
    private Long uid;

    /**
     * 排序
     */
    private int sort;

    /**
     * 自选类型
     */
    private CurrencyTypeEnum type;

    /**
     * 自选法币
     */
    private CurrencyCoinEnum fiat;

    /**
     * 自选现货
     */
    private CurrencyCoinEnum stock;

    /**
     * 创建时间
     */
    private LocalDateTime create_time;

    /**
     * 更新时间
     */
    private LocalDateTime update_time;
}
