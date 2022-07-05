package com.tianli.currency_token.token.mapper;


import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.tianli.currency_token.mapper.CurrencyCoinEnum;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * token列表
 * </p>
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class TokenList extends Model<TokenList> {
    /**
     * 主键
     */
    private Long id;

    private CurrencyCoinEnum token;

    private String name_short;

    private String name_full;

    private String logo;

    private Integer actual_enable;

    private Integer normal_enable;

    private Integer sort;

    private Boolean platform_token;

}
