package com.tianli.currency_token.token.vo;

import cn.hutool.core.bean.BeanUtil;
import com.tianli.currency_token.mapper.CurrencyCoinEnum;
import com.tianli.currency_token.token.mapper.TokenList;
import com.tianli.exchange.vo.Mini24HrTickerVo;
import com.tianli.tool.Bian24HrInfo;
import lombok.Data;

/**
 * @author lzy
 * @date 2022/4/29 11:14
 */
@Data
public class TokenListVo {

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

    /**
     * 是否是平台币
     */
    private Boolean is_platform;

    private Mini24HrTickerVo platform24HrTicker;

    private Bian24HrInfo bian24HrInfo;

    public static TokenListVo getTokenListVo(TokenList tokenList, Bian24HrInfo bian24HrInfo) {
        TokenListVo tokenListVo = BeanUtil.copyProperties(tokenList, TokenListVo.class);
        tokenListVo.setBian24HrInfo(bian24HrInfo);
        return tokenListVo;
    }
}
