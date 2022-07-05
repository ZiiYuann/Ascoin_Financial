package com.tianli.currency_token.vo;

import com.tianli.currency.CurrencyTypeEnum;
import com.tianli.currency_token.mapper.CurrencyCoinEnum;
import com.tianli.exchange.vo.Mini24HrTickerVo;
import com.tianli.tool.Bian24HrInfo;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author lzy
 * @date 2022/4/29 14:58
 */
@Data
public class TokenFavoriteListVo {

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

    private Bian24HrInfo bian24HrInfo;

    /**
     * 是否是平台币
     */
    private Boolean is_platform;

    private Mini24HrTickerVo platform24HrTicker;

}
