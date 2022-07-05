package com.tianli.kline.vo;

import cn.hutool.core.bean.BeanUtil;
import com.tianli.kline.mapper.FollowCurrency;
import com.tianli.tool.Bian24HrInfo;
import lombok.Data;

/**
 * @author lzy
 * @date 2022/4/29 10:47
 */
@Data
public class KlineTypeListVo {
    /**
     * 币种名 ETH/USDT BTC/USDT...
     */
    private String name;

    /**
     * k线参数符号
     */
    private String symbol;

    private Bian24HrInfo bian24HrInfo;


    /**
     * 是否选中 0:未选中  1:选中
     */
    private Boolean selected;

    /**
     * icon 图片地址
     */
    private String img;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 赢钱占比
     */
    private Double win_rate;

    public static KlineTypeListVo getKlineTypeListVo(FollowCurrency followCurrency, Bian24HrInfo bian24HrInfo) {
        KlineTypeListVo klineTypeListVo = BeanUtil.copyProperties(followCurrency, KlineTypeListVo.class);
        klineTypeListVo.setBian24HrInfo(bian24HrInfo);
        return klineTypeListVo;
    }
}
