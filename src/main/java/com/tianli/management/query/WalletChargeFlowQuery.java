package com.tianli.management.query;

import com.tianli.account.enums.AccountOperationType;
import com.tianli.charge.enums.ChargeType;
import com.tianli.charge.enums.ChargeTypeGroupEnum;
import com.tianli.charge.enums.OperationTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author:yangkang
 * @create: 2023-03-14 20:17
 * @Description: 资金流水查询-后台管理
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class WalletChargeFlowQuery implements Serializable {

    /**
     * uid
     */
    private String uid;

    /**
     * 币种
     */
    private String coin;

    /**
     * 操作组
     */
    private ChargeTypeGroupEnum operationGroup;

    /**
     * 操作分类
     */
    private OperationTypeEnum operationType;

    private ChargeType type;

    private AccountOperationType accountOperationType;

    /**
     * 开始时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
}
