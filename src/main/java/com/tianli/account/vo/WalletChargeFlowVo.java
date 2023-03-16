package com.tianli.account.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author:yangkang
 * @create: 2023-03-14 20:37
 * @Description: 资金流水VO  后台管理
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletChargeFlowVo {

    private Long id;

    private Long uid;

    /**
     * 二级分类
     */
    private String chargeType;

    /**
     * 订单id
     */
    private String orderNo;

    /**
     * 币别
     */
    private String coin;

    /**
     * 总数额
     */
    private BigDecimal balance;

    /**
     * 操作金额
     */
    private BigDecimal amount;

    /**
     * 剩余余额
     */
    private BigDecimal remain;

    /**
     * 操作类型组:recharge;withdraw;receive;pay
     */
    private String operationGroup;

    /**
     * 操作类型组名： 充值;提币；转入；转出
     */
    private String operationGroupName;

    /**
     * 操作分类
     */
    private String operationType;


    private LocalDateTime createTime;

    private String logType;

    /**
     * 二级分类名称
     */
    private String chargeTypeName;


}
