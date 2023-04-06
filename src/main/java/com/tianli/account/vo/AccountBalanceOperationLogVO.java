package com.tianli.account.vo;

import com.tianli.account.enums.AccountOperationType;
import com.tianli.charge.enums.ChargeStatus;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.vo.OrderOtherInfoVo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author:yangkang
 * @create: 2023-03-13 20:10
 * @Description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountBalanceOperationLogVO {
    /**
     * 主键
     */
    private Long id;

    /**
     * 用户id
     */
    private Long uid;


    /**
     * 新变动类型
     */
    private ChargeType newChargeType;

    private String newChargeTypeName;

    private String newChargeTypeNameEn;

    /**
     * 记录类型
     */
    private AccountOperationType logType;

    /**
     * 币种类型
     */
    private String coin;


    /**
     * 订单号
     */
    private String orderNo;

    /**
     * 金额
     */
    private BigDecimal amount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 交易状态
     */
    private ChargeStatus status;

    /**
     * 交易完成时间
     */
    private LocalDateTime  completeTime;

    /**
     * 手续费
     */
    private BigDecimal serviceAmount;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 备注
     */
    private String remarks;

    /**
     * 国际化备注
     */
    private String remarksEn;

    /**
     * 关联id
     */
    private Long relatedId;

    private OrderOtherInfoVo orderOtherInfoVo;

    /**
     * 所属组
     */
    private String group;

    /**
     * 所属组英文
     */
    private String groupEn;

    /**
     * 交易哈希
     */
    private String txid;

}
