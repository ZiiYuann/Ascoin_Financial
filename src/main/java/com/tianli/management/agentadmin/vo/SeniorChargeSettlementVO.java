package com.tianli.management.agentadmin.vo;

import com.tianli.currency.CurrencyTokenEnum;
import com.tianli.currency.TokenCurrencyType;
import com.tianli.dividends.settlement.mapper.ChargeSettlement;
import com.tianli.dividends.settlement.mapper.ChargeSettlementStatus;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigInteger;
import java.time.LocalDateTime;

/**
 * <p>
 * 充值提现表
 * </p>
 *
 * @author hd
 * @since 2020-12-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@Builder
public class SeniorChargeSettlementVO {

    /**
     * 主键
     */
    private Long id;

    /**
     * 完成时间
     */
    private LocalDateTime complete_time;

    /**
     * 订单金额
     */
    private double amount;

    /**
     * 手续费
     */
    private double fee;

    /**
     * 真实金额
     */
    private double real_amount;

    /**
     * 发送地址
     */
    private String from_address;

    /**
     * 接受地址
     */
    private String to_address;

    /**
     * 订单状态
     */
    private ChargeSettlementStatus status;

    /**
     * 交易哈希
     */
    private String txid;

    /**
     * 备注
     */
    private String note;

    /**
     * 审核记录备注
     */
    private String review_note;

    private CurrencyTokenEnum token;

    public static SeniorChargeSettlementVO trans(ChargeSettlement chargeSettlement) {
        TokenCurrencyType currency_type = chargeSettlement.getCurrency_type();
        BigInteger real_amount = BigInteger.ZERO;
        if(ChargeSettlementStatus.transaction_success.equals(chargeSettlement.getStatus())){
            real_amount = chargeSettlement.getReal_amount();
        }
        return SeniorChargeSettlementVO.builder()
                .id(chargeSettlement.getId())
                .complete_time(chargeSettlement.getCreate_time())
                .amount(currency_type.money(chargeSettlement.getAmount()))
                .fee(currency_type.money(chargeSettlement.getFee()))
                .real_amount(currency_type.money(real_amount))
                .from_address(chargeSettlement.getFrom_address())
                .to_address(chargeSettlement.getTo_address())
                .status(chargeSettlement.getStatus())
                .txid(chargeSettlement.getTxid())
                .note(chargeSettlement.getNote())
                .review_note(chargeSettlement.getReview_note())
                .token(chargeSettlement.getToken())
                .build();

    }
}
