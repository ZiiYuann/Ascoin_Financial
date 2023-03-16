package com.tianli.management.query;

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
     * 操作类型
     */
    private String operationGroup;

    /**
     * 操作分类
     */
    private String operationType;

    /**
     * 二级分类
     */
    private String type;

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

    /**
     * 提币类型:提币成功；提币失败；提币冻结
     */
    private String withdrawType;


}
