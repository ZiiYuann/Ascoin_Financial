package com.tianli.management.fundmanagement;

import com.tianli.bet.KlineDirectionEnum;
import com.tianli.bet.mapper.Bet;
import com.tianli.bet.mapper.BetPO;
import com.tianli.bet.mapper.BetResultEnum;
import com.tianli.bet.mapper.BetTypeEnum;
import com.tianli.currency.CurrencyTokenEnum;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.user.logs.mapper.UserIpLog;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.util.Objects;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class BetManageVO {

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
    private String order_type;

    private String channel_name;


    public static BetManageVO trans(Bet bet) {
        return BetManageVO.builder()
                .id(bet.getId())
                .create_time(bet.getCreate_time())
                .phone(bet.getUid_username())
                .uid_nick(bet.getUid_nick())
                .bet_type(bet.getBet_type())
                .bet_time(bet.getBet_time())
                .amount(TokenCurrencyType.usdt_omni.money(bet.getAmount()))
                .discount_amount(TokenCurrencyType.usdt_omni.money(bet.getDiscount_amount()))
                .bet_direction(bet.getBet_direction())
                .final_direction(bet.getFinal_direction())
                .result(bet.getResult())
                .bet_exchange_rate(bet.getStart_exchange_rate())
                .draw_exchange_rate(bet.getEnd_exchange_rate())
                .bet_symbol(bet.getBet_symbol())
                .order_type(bet.getOrder_type())
                .earn(bet.getEarn() == null ? 0 : TokenCurrencyType.usdt_omni.money(bet.getEarn()))
                .fee(bet.getFee() == null ? 0 : TokenCurrencyType.usdt_omni.money(bet.getFee()))
                .profit(bet.getProfit() == null ? 0 : TokenCurrencyType.usdt_omni.money(bet.getProfit()))
                .income(bet.getIncome() == null ? 0 : TokenCurrencyType.usdt_omni.money(bet.getIncome()))
                .base_BF(bet.getBase_BF() == null ? 0 : CurrencyTokenEnum.BF_bep20.money(bet.getBase_BF()))
                .income_BF(bet.getIncome_BF() == null ? 0 : CurrencyTokenEnum.BF_bep20.money(bet.getIncome_BF()))
                .final_BF(bet.getFinal_BF() == null ? 0 : CurrencyTokenEnum.BF_bep20.money(bet.getFinal_BF()))
                .build();
    }

    public void fillOtherProperties(UserIpLog log){
        if(Objects.isNull(log)){
            return;
        }
        this.grc_score = log.getGrc_score();
        this.grc_result = log.getGrc_result();
        this.ip = log.getIp();
        this.equipment_type = log.getEquipment_type();
        this.equipment = log.getEquipment();
        this.country = log.getCountry();
        this.region = log.getRegion();
        this.city = log.getCity();
    }

    public static BetManageVO convert(BetPO betPO) {
        BetManageVO build = BetManageVO.builder().build();
        BeanUtils.copyProperties(betPO, build);
        build.setAmount(TokenCurrencyType.usdt_omni.money(betPO.getAmount()));
        build.setDiscount_amount(TokenCurrencyType.usdt_omni.money(betPO.getDiscount_amount()));
        build.setEarn(TokenCurrencyType.usdt_omni.money(betPO.getEarn()));
        build.setFee(TokenCurrencyType.usdt_omni.money(betPO.getFee()));
        build.setProfit(TokenCurrencyType.usdt_omni.money(betPO.getProfit()));
        build.setIncome(TokenCurrencyType.usdt_omni.money(betPO.getIncome()));
        build.setIncome_BF(TokenCurrencyType.BF_bep20.money(betPO.getIncome_BF()));
        build.setBase_BF(TokenCurrencyType.BF_bep20.money(betPO.getBase_BF()));
        build.setFinal_BF(TokenCurrencyType.BF_bep20.money(betPO.getFinal_BF()));
        return build;
    }
}
