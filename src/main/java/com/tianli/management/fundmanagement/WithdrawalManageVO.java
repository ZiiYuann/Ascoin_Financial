package com.tianli.management.fundmanagement;

import com.tianli.charge.mapper.Charge;
import com.tianli.charge.mapper.ChargeStatus;
import com.tianli.currency.CurrencyTokenEnum;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.user.logs.mapper.UserIpLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @Author wangqiyun
 * @Date 2020/3/31 11:26
 */


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WithdrawalManageVO {
    private Long id;
    private LocalDateTime create_time;
    private String uid_username;
    private String uid_nick;
    private ChargeStatus status;
    private Long uid;
    private String sn;
    private CurrencyTokenEnum token;
    private TokenCurrencyType currency_type;
    private double amount;
    private double fee;
    private double real_amount;
    private String from_address;
    private String to_address;
    private String txid;
    private String note;

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

    private String user_type;
    /**
     * 审核人
     */
    private String reviewer;
    /**
     * 审核时间
     */
    private LocalDateTime reviewer_time;

    private String reason;

    private String reason_en;

    private String review_note;

    public static WithdrawalManageVO trans(Charge charge){
        double realAmount = 0.0;
        CurrencyTokenEnum token = charge.getToken();
        if(ChargeStatus.chain_success.equals(charge.getStatus())){
            if(Objects.equals(token, CurrencyTokenEnum.usdt_omni)){
                realAmount = charge.getCurrency_type().money(charge.getReal_amount());
            }else{
                realAmount = CurrencyTokenEnum.BF_bep20.money(charge.getReal_amount());
            }
        }

        return WithdrawalManageVO.builder()
                .id(charge.getId())
                .create_time(charge.getCreate_time())
                .status(charge.getStatus())
                .uid(charge.getUid())
                .sn(charge.getSn())
                .token(token)
                .uid_username(charge.getUid_username())
                .currency_type(charge.getCurrency_type())
                .uid_nick(charge.getUid_nick())
                .amount(charge.getCurrency_type().money(charge.getAmount()))
                .fee(charge.getCurrency_type().money(charge.getFee()))
                .real_amount(realAmount)
                .from_address(charge.getFrom_address())
                .to_address(charge.getTo_address())
                .txid(charge.getTxid())
                .note(charge.getNote())
                .build();

    }

    public static WithdrawalManageVO convert(WithdrawalManagePO po){
        WithdrawalManageVO manageVO = WithdrawalManageVO.builder().build();
        BeanUtils.copyProperties(po, manageVO);
        manageVO.setAmount(manageVO.getCurrency_type().money(po.getAmount()));
        manageVO.setFee(manageVO.getCurrency_type().money(po.getFee()));
        manageVO.setReal_amount(manageVO.getCurrency_type().money(po.getReal_amount()));
        return manageVO;
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
}
