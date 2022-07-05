package com.tianli.bet.mapper;

import com.tianli.bet.KlineDirectionEnum;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class BetBhvPO {


    /**
     * 主键
     */
    private Long id;

    /**
     * 创建时间
     */
    private LocalDateTime create_time;

    /**
     * 用户id
     */
    private Long uid;

    /**
     * 用户username
     */
    private String phone;

    /**
     * 用户昵称
     */
    private String uid_nick;

    /**
     * 用户头像
     */
    private String uid_avatar;

    /**
     * 押注类型
     */
    private BetTypeEnum bet_type;

    /**
     * 竞猜时间,单位:分钟
     */
    private Double bet_time;

    /**
     * 押注金额
     */
    private double amount;

    /**
     * 优惠金额
     */
    private double discount_amount;

    /**
     * 押注方向
     */
    private KlineDirectionEnum bet_direction;

    /**
     * 最终方向
     */
    private KlineDirectionEnum final_direction;

    /**
     * 用户输赢结果
     */
    private BetResultEnum result;

    /**
     * 押注时汇率
     */
    private Double bet_exchange_rate;

    /**
     * 开奖时汇率
     */
    private Double draw_exchange_rate;

    /**
     * 盈利所得
     */
    private double earn;

    /**
     * 平台手续费
     */
    private double fee;

    /**
     * 净利润
     */
    private double profit;

    /**
     * 最终所得
     */
    private double income;

    /**
     * 押注奖励的BF
     */
    private double income_BF;

    /**
     * 手续费用BF抵扣时, 消耗的BF(原始的)
     */
    private double base_BF;

    /**
     * 手续费用BF抵扣时, 消耗的BF(打折后)
     */
    private double final_BF;

    /**
     * 押注币种交易对
     */
    private String bet_symbol;
    private String bet_symbol_name;

    /* 新加的IP相关信息 */

    /**
     * 谷歌校验分数
     */
    private Double grc_score;
    private Boolean grc_result;

    /**
     * 设备信息
     */
    private String ip;
    private String equipment_type;
    private String equipment;

    /**
     * 国家
     */
    private String country;

    /**
     * 地区
     */
    private String region;

    /**
     * 城市
     */
    private String city;

    /**
     * 反方向的押注单数
     */
    private int count;
    private List<BetPO> list;
}
