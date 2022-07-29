package com.tianli.charge.vo;

import com.tianli.charge.enums.ChargeGroup;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.enums.ChargeType;
import com.tianli.common.blockchain.CurrencyCoin;
import com.tianli.common.blockchain.NetworkType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-07-11
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderChargeInfoVO {

    private String uid;

    private String orderNo;

    /**
     * 交易类型
     */
    private ChargeType type;

    /**
     * 交易状态
     */
    private ChargeStatus status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 完成时间
     */
    private LocalDateTime completeTime;

    /**
     * 交易hash
     */
    private String txid;

    /**
     * 发送地址
     */
    private String fromAddress;

    /**
     * 接受地址
     */
    private String toAddress;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 币别
     */
    private CurrencyCoin coin;

    /**
     * 网络
     */
    private NetworkType networkType;

    /**
     * 手续费
     */
    private BigDecimal serviceAmount = BigDecimal.ZERO;

    private String logo;

    private String typeName;

    private String typeNameEn;

    public String getTypeName() {
        return type.getNameZn();
    }

    public String getTypeNameEn() {
        return type.getNameEn();
    }

    public String getChargeGroup(){
        return ChargeGroup.getInstance(this.type).getName();
    }
}
