package com.tianli.accountred.entity;

import com.tianli.accountred.enums.RedEnvelopeChannel;
import com.tianli.accountred.enums.RedEnvelopeStatus;
import com.tianli.accountred.enums.RedEnvelopeType;
import com.tianli.accountred.enums.RedEnvelopeWay;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author chenb
 * @apiNote
 * @since 2022-10-12
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RedEnvelope {

    /**
     * 红包id
     */
    @Id
    private Long id;

    private RedEnvelopeChannel channel;

    /**
     * 红包持有者
     */
    private Long uid;

    private Long shortUid;

    /**
     * 红包唯一标示符号 群号、或者hash啥的
     */
    private String flag;

    /**
     * 币别
     */
    private String coin;

    /**
     * NORMAL：单个红包金额 RANDOM：红包总金额
     */
    private BigDecimal amount;

    /**
     * 红包的总金额
     */
    private BigDecimal totalAmount;

    /**
     * 红包数量
     */
    private int num;

    private int receiveNum;

    /**
     * 领取金额
     */
    private BigDecimal receiveAmount;

    /**
     * 红包文案
     */
    private String remarks;

    /**
     * 红包类型 NORMAL：普通 RANDOM：手气 PRIVATE：私聊
     */
    private RedEnvelopeType type;

    /**
     * 红包方式 WALLET：钱包 CHAIN：链上
     */
    private RedEnvelopeWay way;

    /**
     * WAIT:等待发送 PROCESS:发送中 fAIL:发送失败(上链失败) FINISH:已经完成 OVERDUE:过期
     */
    private RedEnvelopeStatus status;

    private String txid;

    /**
     * 红包创建时间
     */
    private LocalDateTime createTime;

    /**
     * 红包结束时间（正常结束，到期 其余状态没有）
     */
    private LocalDateTime finishTime;

}
