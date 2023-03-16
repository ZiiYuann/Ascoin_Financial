package com.tianli.account.vo;

import com.tianli.account.enums.AccountOperationType;
import com.tianli.charge.enums.NewChargeStatus;
import com.tianli.charge.enums.NewChargeType;
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
public class AccountBalanceOperationLogVo  {
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
    private NewChargeType newChargeType;

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
     * 余额变动描述
     */
    private String des;

    /**
     * 交易状态
     */
    private NewChargeStatus status;

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
     * 是否可查看详情 0否 1：是
     */
    private int isSeeDetails;

}
