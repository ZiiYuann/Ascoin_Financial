package com.tianli.account.vo;

import com.tianli.account.enums.AccountOperationType;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.enums.ChargeTypeGroupEnum;
import com.tianli.charge.enums.OperationTypeEnum;
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
    private ChargeType chargeType;

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
    private ChargeTypeGroupEnum operationGroup;

    /**
     * 操作类型组名： 充值;提币；转入；转出
     */
    private String operationGroupName;

    /**
     * 操作分类
     */
    private OperationTypeEnum operationType;

    private String operationTypeName;


    private LocalDateTime createTime;

    private AccountOperationType logType;

    /**
     * 二级分类名称
     */
    private String chargeTypeName;

    public String getOperationGroupName() {
        return operationGroup.getTypeGroup();
    }

    public String getOperationTypeName() {
        return operationType.getName();
    }
}
